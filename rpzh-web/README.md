# rpzh-web · 系统管理 + 养护业务前端

基于 `rpzh-system`（用户 / 角色 / 菜单 / 部门）与 `rpzh-function`（养护业务）实现的 Vue 3 管理后台。
技术栈：**Vue 3 + Vite 5 + Element Plus 2 + Pinia + Vue Router 4 + Axios**。

## 快速开始

```bash
cd rpzh-web
npm install
npm run dev          # http://localhost:5173
npm run build        # 产物在 dist/
```

Node 18+（本机验证用的是 Node 22）。

## 后端地址怎么配

`.env.development`：

```ini
VITE_API_BASE=/api
VITE_PROXY_TARGET=http://localhost:8080    # 网关
VITE_PROXY_STRIP_PREFIX=false              # 网关会自己 StripPrefix，不用前端去
```

本机实际用的是 **`rpzh-gateway-a`，端口 7001**（`b`=7002、`c`=7003，三个网关模块代码相同、端口不同）。
在 **`.env.development.local`**（本机生效、不进 git）里覆盖：

```ini
VITE_PROXY_TARGET=http://127.0.0.1:7001
VITE_PROXY_STRIP_PREFIX=false
```

想绕开网关直连 `rpzh-system`（16883）时，改成 `VITE_PROXY_TARGET=http://127.0.0.1:16883` +
`VITE_PROXY_STRIP_PREFIX=true`（此时 `/user/login` 走不通，登录必须经网关或直连 16888）。

两种方式浏览器侧都只请求 `/api/**`，不产生跨域。生产部署交给 Nginx：

```nginx
location /api/ {
    proxy_pass http://gateway:8080/api/;
}
location / {
    root /usr/share/nginx/html;
    try_files $uri $uri/ /index.html;   # history 模式必须
}
```

## 登录

`/api/user/login` → 网关 → `rpzh-auth`（`rpzh-oauth`，16888）→ OAuth2 密码模式签发 JWT。

本机已经能完整跑通，测试账号：

| 账号 | 密码 | 角色 | 部门 |
| --- | --- | --- | --- |
| rpzh | 123456 | 超级管理员 | 福建烟草养护中心 |
| admin | 123456 | 超级管理员 | 福建烟草养护中心 |
| zhangwei | 123456 | 运维管理员 | 运维管理部 |
| lina | 123456 | 巡检员 | 巡检作业部 |
| wangqiang | 123456 | 巡检员 | 设备管理部 |
| chenjing | 123456 | 数据分析员 | 数据分析组 |

> 图形验证码后端 `OauthController` 里的校验代码被注释掉了，**填任意值都能登录**，但 `uuidCode` 不能为空。
> 登录成功后返回 `data.jwt`（形如 `bearer xxx`），原样放进 `Authorization` 头；401/403 会自动清令牌回登录页。

登录页仍保留「本地点一次进入」按钮（`VITE_ENABLE_DEMO_LOGIN`），只在后端全挂时应急用：
它只写本地假令牌、**不塞 `Authorization` 头**，靠 `ResourceServerConfig` 的 `permitAll()` 读数据，
但写接口会因为 `TokenDecode` 取不到用户而失败。

### 本地环境需要什么

```bash
# 1. 业务库：部门 / 角色 / 用户 / 菜单 / OAuth 客户端（可重复执行）
docker exec -i mysql mysql --default-character-set=utf8mb4 -uroot -proot123 rpzh-system < sql/dev-seed.sql

# 2. 登录日志库：auth 的 @LoginLogging 切面会强依赖 rpzh-log，日志表不可用会导致登录接口直接失败
docker exec -i mysql mysql --default-character-set=utf8mb4 -uroot -proot123 < sql/dev-log-db.sql
```

服务启动顺序（Nacos 8848、Redis 6379 需先就绪）：
`rpzh-system` 16883 → `rpzh-auth` 16888 → `rpzh-log` 16065 → `rpzh-function` 16900 → `rpzh-gateway-a` 7001。

> 养护业务还需要 MySQL 3306、MQTT 1883、ClickHouse 8123；`rpzh-function` 的
> `bootstrap-local.yml` 已把 MQTT 指向 `tcp://127.0.0.1`、ClickHouse 指向 8123，
> 并排除了 `DruidDataSourceAutoConfigure`（与 `dynamic-datasource` 冲突）。

> 两个容易踩的坑：
> 1. `rpzh-system` 的 `spring.application.name` 必须是 **`rpzh-system`**（原来叫 `rpzh-maintain-system`，
>    导致网关 `lb://rpzh-system` 和 `UserFeign` 全都解析不到实例）。
> 2. `oauth_client_details.resource_ids` 必须是 **`oauth2-resource`**（资源服务器默认 resourceId），
>    否则带令牌访问会报 `Invalid token does not contain resource id`。

## 路由与菜单

```
/menu/getRouters  →  Menu 树  →  buildRoutes()  →  router.addRoute('Root', leaf) + 侧边栏数据
```

- 目录（`menuType = M`）只做侧边栏分组，不生成路由；真正注册的是页面（`C`），路径是绝对路径
- `component` 字符串（如 `system/user/index`）通过 `import.meta.glob('../views/**/*.vue')` 解析成异步组件，找不到就落到占位页（不会白屏）
- 后端 `icon` 存的是若依字体图标名（`system` / `user` / `peoples`），由 `src/utils/icon.js` 映射到 Element Plus 组件名；认不出来就不渲染图标
- **兜底**：`getRouters` 返回空数组或请求失败时，用 `src/router/localMenus.js` 里的本地菜单（与 `sql/menu-seed.sql` 结构一致），顶部会显示「本地菜单」标记

> `MenuServiceImpl.getRouters()` 里用户名写死成 `"rpzh"`（源码标注为测试用例），
> 且要命中 `tb_user_role` 里 `role_id = 1` 才返回全部菜单 —— 所以 `sql/dev-seed.sql` 里的
> `rpzh` 账号是专门为它准备的（已授予角色 1）。换成别的账号登录，侧边栏仍由后端菜单驱动，
> 但 `getRouters` 结果只跟 `rpzh` 这个写死的名字有关，与当前登录人无关。

## 目录结构

```
src/
├─ api/            # 按后端模块拆：request(拦截器) / auth / user / role / menu / dept / function/*(10 个养护接口文件)
├─ layout/         # 侧边栏 + 顶栏 + 内容区，SidebarItem 递归菜单、PasswordDialog
├─ router/         # index(固定路由) / dynamic(菜单转路由) / localMenus(兜底)
├─ store/modules/  # app(折叠/主题) / user(登录态) / permission(动态路由)
├─ utils/          # auth(token) / form(表单序列化) / dict(状态字典) / icon(图标映射) / function(养护字典+降级)
├─ views/
│  ├─ login/       # 登录页（验证码 + 免登录入口）
│  ├─ dashboard/   # 首页 + 接口自检 + 环境信息
│  ├─ profile/     # 个人中心
│  ├─ system/user  # 用户管理
│  ├─ system/role  # 角色管理
│  ├─ system/menu  # 菜单管理
│  ├─ system/dept  # 部门管理
│  ├─ function/    # 养护业务 10 个页面 + components/FunctionListPage.vue 通用列表组件
│  └─ error/       # 404 / 占位页
└─ permission.js   # 全局守卫：登录态校验 + 首次进入生成动态路由
```

## 接口对照

| 页面 | 方法 | 地址 | 说明 |
| --- | --- | --- | --- |
| 登录 | POST | `/user/login` | 表单 `username/password/uuidCode/verificationCode`，返回 `data.jwt` |
| 验证码 | GET | `/user/verification?uuidCode=` | 返回 jpeg，前端转 blob |
| 用户列表 | POST | `/username/findAllUserList` | `page` 从 **1** 开始，配合 PageHelper |
| 用户新增/修改 | POST | `/username/addUser`、`/username/editUser` | `roleIds` 必填 |
| 用户删除 | POST | `/username/deleteUser` | 参数名是 **`userIds`**，逻辑删除 `del_flag` |
| 用户启停 | POST | `/username/changeUserStatus` | 不能改自己和 `user_id = 1` |
| 重置密码 | GET | `/username/adminUpdatePassWord` | `username` + `newpassWord` |
| 角色列表 | POST | `/role/findAllRole` | 分页 |
| 角色新增/修改 | POST | `/role/addRole`、`/role/editRole` | `menuIds` 必填；`roleId = 1` 只允许改菜单 |
| 角色删除 | GET | `/role/delectRole?roleId=1&roleId=2` | 注意后端拼写是 `delectRole` |
| 角色数据权限 | POST | `/role/dataScope` | 1 全部 / 2 自定义 / 3 本部门 / 4 本部门及以下 |
| 菜单树 | GET | `/menu/list` | 返回树，含按钮（F） |
| 菜单增删改 | POST/GET | `/menu/addMenu`、`/menu/editMenu`、`/menu/deleteMenu` | `editMenu` 强制校验 **`parentName`** 非空 |
| 菜单状态 | POST | `/menu/changeMenuStatus` | |
| 部门树 | POST | `/dept/findAllDept` | 返回树 |
| 部门增删改 | POST/GET | `/dept/addDept`、`/dept/editDept`、`/dept/deleteDept` | |
| 部门状态 | POST | `/dept/changeDeptStutas` | 后端拼写如此，保持一致 |

### 三个必须注意的坑

1. **数组参数**：后端用 `@RequestParam Long[] roleIds` 接收，必须序列化成重复 key
   `roleIds=1&roleIds=2`。`$.ajax`/axios 默认会序列化成 `roleIds[]=1`，Spring 绑不上。
   本项目统一用 `src/utils/form.js` 的 `toForm()` 处理。
2. **status 语义不统一**（后端注释自相矛盾）：
   - 用户：`1 = 正常，0 = 停用`（`UserDao.findById` 写死 `WHERE status = 1`）
   - 角色 / 部门：`0 = 正常，1 = 停用`
   - 菜单：`1 = 正常，0 = 停用`（`MenuDao.findAllMenuStatuTrue` 写死 `WHERE status = 1`，给 0 侧边栏就没了）
   全部集中在 `src/utils/dict.js`，后端统一后只改这一个文件。
3. **`editMenu` 要带 `parentName`**：Controller 里做了非空校验，父级为根时传 `主类目`。

## 养护业务（function）模块

`rpzh-function`（16900）是独立的养护业务服务，页面挂在侧边栏「养护业务」分组下。
菜单由 `sql/dev-function-seed.sql` 写入（menu_id 2100–2110），`src/router/localMenus.js` 有同结构兜底。

### 页面清单

| 页面 | 路由 | component | 主要接口 | 写能力 |
| --- | --- | --- | --- | --- |
| 监控中心 | `/function/monitoring` | `function/monitoring/index` | 仓库树 / 节点 / 故障节点 / 实时天气 / 预警阈值 | 只读 |
| 仓库管理 | `/function/warehouse` | `function/warehouse/index` | `warehouseTree` / `getStackPage` / `getAllWareArea` | 仓库 / 楼栋 / 楼层 / 分区 / 垛位 五级增删改 |
| 节点管理 | `/function/node` | `function/node/index` | `findNode` | 新增 / 修改 / 删除 |
| 养护计划 | `/function/plan` | `function/plan/index` | `findMaintainPlanInfo` / `insertMaintainPlan` / `updateMaintainPlan` | 新建 / 修改 / 删除 |
| 预警中心 | `/function/warning` | `function/warning/index` | `findEarlyWarningInfo` / `findHistoryWarning` | 误报处理 |
| 任务管理 | `/function/task` | `function/task/index` | `findTask` | 新建 / 处置评价 / 删除 |
| 虫害监测 | `/function/pest` | `function/pest/index` | `findPestBoard` / `findPestDataHistory` | 新建诱捕板 / 修改巡查规则 / 删除 |
| 数据报表 | `/function/report` | `function/report/index` | `/dataReport` 下 8 个报表接口 | 只读 |
| 阈值设置 | `/function/alarm-setting` | `function/alarm-setting/index` | 阈值模板 / 库区报警设置 / 颜色升级 | 模板与库区规则增删改、颜色配置 |
| 物料管理 | `/function/material` | `function/material/index` | `findMaterialInfo` / `findAllMaterialCodes` | 只读 |

### API 与通用组件

- `src/api/function/*.js`：10 个文件按 Controller 分组，写操作统一走 `toForm()` 表单序列化
- `src/views/function/components/FunctionListPage.vue`：通用列表页（搜索 / 分页 / 统计卡 / 表格 / 详情抽屉 / 降级提示），
  10 个页面里 8 个基于它，仅监控中心、数据报表、物料管理是自定义布局
- `src/utils/function.js`：字典（`NODE_TYPE` / `WARNING_GRADE` / `TASK_STATUS` …）、
  `toRows()` 统一拆包、`loadList()` 失败不抛错、`shortReason()` 把 MyBatis/SQL 长异常压缩成中文原因

### 新建 / 修改（CRUD）

养护业务原先只有列表和详情，除养护计划外都没有写入口。现在统一用
`src/views/function/components/CrudDialog.vue` 承载表单：

- `fields` 描述字段（`text | textarea | number | select | multiselect | date | datetime | switch`、
  `required`、`options` 支持函数、`span`、`hint`、`transform`），弹窗负责渲染、必填校验、失败提示、成功后刷新
- `submit(payload, mode)` 由页面提供，内部再调各自的 `add*/update*` API；`mode` 为 `create | edit`
- 只提交可见字段，避免把列表行的 VO 冗余字段（`xxxNow`、`Vo` 对象）带给后端
- `FunctionListPage` 增加了 `#actions`（顶部操作，`embedded` 时落在表格工具栏右侧）和
  `#row-actions`（行操作，自动加宽操作列）两个插槽

| 页面 | 顶部操作 | 行操作 | 对应接口 |
| --- | --- | --- | --- |
| 仓库管理 | 新建仓库 / 新建楼栋 / 新建楼层 / 新建分区 / 新建垛位 | 修改 / 删除 | `add*` / `update*` / `del` |
| 节点管理 | 新增节点 | 修改 / 删除 | `addNode` / `updateNode` / `deleteNode` |
| 养护计划 | 新建养护计划 | 修改 / 删除 | `insert` / `updateMaintainPlan` / `deleteMaintainPlanInfo` |
| 任务管理 | 新建任务 | 处置（含评价）/ 删除 | `addTask` / `taskDispose` / `taskEvaluate` / `delTask` |
| 虫害监测 | 新建诱捕板；修改巡查规则 | 删除 | `addPestBoard` / `updatePestRule` / `deletePestBoard` |
| 阈值设置 | 新建模板 / 新建规则 / 修改配置 | 修改 / 删除 | `insertAlarmSettingTemplate` / `insertWareAreaAlarmSetting` / `updateOrInsertColorAndAlertUpgradeDay` |

后端配合的三处修正：

1. 新增 `POST /maintain/maintainPlan/findOccupiedStackIds`（`select stack_id from tb_maintain_plan where is_end='0'`）。
   原先前端用 `findMaintainPlanInfo` 的列表结果判断占用，但列表 SQL 不返回 `stack_id`，`occupied` 全是 `NaN`，
   一个垛位都没筛掉；`findMaintainPlanInfo` 还带 `aftercare_date_end >= NOW()` 过滤，会漏掉阶段已过期但未结束的计划。
2. 新增 `POST /maintain/warehouse/getAllWareArea`。养护计划的垛位下拉要按库区逐个拉取，旧代码把库区 id
   写死成 `201~205`，页面里新建的库区不会出现；现在改为动态取库区，接口不可用时才回退到默认 5 个。
3. `SiteRouteUtil.route()` 原来用 `contains("04")` 这类字符串包含判断层级，纯数字 `pid_route`
   （`0,1,11,101,202`）里 `101` 含 `01` 会被误判成 `[02:xxx]`；改为解析末段真实层级
   （`[04:206]` → 下一层 `05`，纯数字则取 token 个数），`beforeUpdateMaintainPlan` 也改成把带标记的
   全路径传给 `getAlarmValueSetting`，修掉了修改弹窗必现的 `ArrayIndexOutOfBoundsException: 1`。

### 分页参数

function 列表接口用 **`page` / `size`**（不是 `pageNum` / `pageSize`），`MonitorCondition` 里定义为
`private Integer page = 1; private Integer size = 10;`。`getStackPage` 的库区参数名是 **`wareAreaId`**，
不是 `areaId` / `floorId`。数据报表页顺序调用 8 个接口并每次间隔 170ms，规避网关 10 req/s 限流。

### 接口降级机制

`*Dao.xml` 已完成 Oracle → MySQL 适配，绝大多数接口返回真实数据。降级机制仍保留，
用于兜住空数据 / 权限不足 / ClickHouse 历史表无数据等情况，避免白屏或弹窗刷屏：

- `src/api/request.js` 的 `shouldSilent(config)`：对 `/maintain|/dataReport|/issue|/node` 路径默认静默错误弹窗，
  需要弹窗时传 `silent: false`
- `loadList()` 捕获异常返回 `{ ok:false, message }`，`FunctionListPage` 用 `el-alert` 显示短原因 + 说明 + 原始信息，
  页面右上角标签从「接口正常」变「降级展示」，表格空态提示「接口暂不可用，已降级展示」
- `shortReason()` 把长异常压成可读中文原因

#### Dao.xml MySQL 适配记录

对 `rpzh-parent/**/src/main/resources/**/*Dao.xml` 统一改写（原始 Oracle 版本备份在
`C:/Users/ppo/WorkBuddy/2026-11-03-23-47-40/dao-backup-oracle/`）：

| Oracle 写法 | MySQL 写法 |
| --- | --- |
| WHERE 里的 `rownum = 1` / `rownum <= 5` | 移到语句末尾 `LIMIT 1` / `LIMIT 5`，派生表补别名 |
| `TO_DATE(x, 'SYYYY-MM-DD HH24:MI:SS')` | `STR_TO_DATE(x, '%Y-%m-%d %H:%i:%s')` |
| `TO_CHAR(x, 'YYYY-MM-DD')` | `DATE_FORMAT(x, '%Y-%m-%d')` |
| 单参数 `TO_CHAR(x)` | `CAST(x AS CHAR)` |
| `DECODE(a, b, c, NULL)` | `IF(a = b, c, NULL)` |
| `NVL(a, b)` | `IFNULL(a, b)` |
| `LISTAGG(x, s) WITHIN GROUP(ORDER BY y)` | `GROUP_CONCAT(x ORDER BY y SEPARATOR s)` |
| `INSTR(s, ',', 1, 1)` | `LOCATE(',', s)` |
| `SEQ.NEXTVAL` / `SEQ.CURRVAL` | `SELECT IFNULL(MAX(id),0)+1 FROM 表` + `#{keyProperty}` |
| 裸 `sysdate` | `NOW()` |
| 双引号别名 `AS "alias"` | 反引号别名（MySQL 8 默认无 `ANSI_QUOTES`） |
| `FROM DUAL` | 保留，MySQL 兼容 |

> `@DS("clickhouse")` 的 53 个语句**未改动**（ClickHouse 有自己的函数体系）；
> `db2` 数据源在 local 配置里不存在且 `strict:false`，实际回落 MySQL。
> 改完 `src` 必须同步到 `target/classes` 并重启 `rpzh-function` 才会生效。

#### 剩余的后端限制（与 SQL 方言无关）

| 现象 | 原因 |
| --- | --- |
| 虫害规则 `Index: 0, Size: 0` | `tb_pest_patrol` 为空配置表，业务代码取 `list.get(0)` |
| 任务进度 / 趋势 `Access is denied` | `@PreAuthorize` 需要的 authority 不在当前 JWT 里 |
| 预警统计 `message: null` | `EarlyWarningServiceImpl.findWarningCount` 对空结果空指针 |
| 汇总报表 `message: null` | ClickHouse 历史表无数据，`SiteRouteUtil.route` 空指针 |
| 监控历史曲线 | ClickHouse `tb_node_co2_history` 等历史表为空 |

改写后 E2E 的**降级页面从 2 个降为 0 个**：养护计划 3 行、预警中心 4 行、
虫害监测 12 行、阈值设置 6 行，均返回真实数据。

### 本地测试数据（纯增量，不删数据）

```bash
# function 业务数据、菜单、角色授权（可重复执行，全 INSERT IGNORE / ON DUPLICATE KEY UPDATE）
docker exec -i mysql mysql --default-character-set=utf8mb4 -uroot -proot123 rpzh-system < sql/dev-function-seed.sql

# 补充配置行、阈值规则、虫害规则、MC 兼容视图、更多业务行
docker exec -i mysql mysql --default-character-set=utf8mb4 -uroot -proot123 < sql/dev-function-seed-extra.sql

# 养护计划「新建」所需权限 + 空闲测试垛位（见下节）
docker exec -i mysql mysql --default-character-set=utf8mb4 -uroot -proot123 rpzh-system < sql/dev-plan-create-seed.sql
```

三个脚本**都不含业务数据 DELETE / 回滚**，可重复执行；`dev-function-seed-extra.sql` 还条件新增
`tb_maintain_plan_stage.content` 列并增量建 `MC.VW_MAINTAIN_INVENTORY` 视图。
ClickHouse 侧需要 19 张空表 DDL（见 `clickhouse/clickhouse-ddl.sql`）与 `tb_node_co2_history`。

### 养护计划新建

页面右上角「新建养护计划」会真实调用 `POST /api/maintain/maintainPlan/insert`，**不是演示弹窗**。

表单分成「基础信息 / 计划时间 / 预警阈值」三段，下拉数据全部来自真实接口：

| 字段 | 来源 |
| --- | --- |
| 物料编码（多选、可手输） | `findAllMaterialCodes()` |
| 垛位（已排掉有计划的） | `getAllWareArea()` 拿库区 → `getStackPage({ wareAreaId })` 逐个拉垛位 → `findOccupiedStackIds()` 过滤 |
| 养护阶段 | `getPhases(maintenceStyle)`，随养护方式联动 |
| 负责人（多选） | `/username/findAllUserList` |
| 预警规则名称 | `findAlarmSettingTemplateList()` |

后端 `MaintainPlanController#insertMaintainPlan` 的约束与对应处理：

- `@PreAuthorize("hasAnyAuthority('f:m:add')")` —— 原菜单表没有该权限，直接请求会 403（前端还会被踢回登录页）。
  `dev-plan-create-seed.sql` 增量插入 `menu_id=2111`（`menu_type='F'`，不进侧边栏）并授权 `role_id=1`。
- `getUsername()` 从 Redis hash `MAINTIAN_USER_ID_USERNAME` 取负责人姓名，`RedisUtil.hget` 用
  `Jackson2JsonRedisSerializer`，**值必须是带双引号的 JSON 字符串**（`"rpzh"`），写裸字符串会报
  `Could not read JSON: Unrecognized token 'rpzh'`。
- `GetBetweenDayUtils` 要求 20 个阈值全非 null 且每组严格递增（下限 < 一级 < 二级 < 三级）。
  表单预填了一组可用默认值，折叠在「预警阈值」里，前端也会先按同样规则校验一次。
- `pidRoute` 必须是 `[00:0],[01:2],[02:13],[03:104],[04:205],[05:307]` 这种带类型标记的格式
  （`SiteRouteUtil.getSiteId` 按 `04:` / `05:` 切分），而 `tb_stack.pid_route` 是逗号分隔的
  `0,2,13,104,205`；前端 `buildPidRoute()` 负责转换并补上垛位自身的 `[05:id]`。
- `isExistStackId()` 拒绝已有计划（`is_end='0'`）的垛位；脚本增量加了 307 / 308 两个空闲测试垛位。

**后端一处必要修复**：`MaintainPlanServiceImpl.insertMaintainPlan` 原先在 Oracle 序列时代对
`id` / `alarmValueSettingId` / `stageId` 各做了一次 `+1`。Dao.xml 改成 MySQL `MAX(id)+1` 后，
`selectKey` 已经写回真实 id，再 `+1` 会让阶段/数量/预警表全部关联到不存在的 plan_id，
`updateMaintainPlanStageId` 影响 0 行，`stage_id` 为 NULL，而列表 SQL 用
`t1.stage_id = t2.id` 关联，导致**新建成功却看不到记录**。已去掉三处 `+1`，新建后可直接出现在列表。
（首次联调产生的 plan id=6 关联不一致，脚本里用 UPDATE 修正，没有删除任何数据。）

验证脚本：`e2e/verify-plan-insert.mjs`（接口直连）、`e2e/smoke-plan-create.mjs`（浏览器全流程）。

### 网关路由

`rpzh-gateway-a/b/c` 的 `bootstrap-local.yml` 都已加：

```yaml
- id: rpzh-function-routes
  uri: lb://rpzh-function
  predicates:
    - Path=/api/maintain/**,/api/dataReport/**,/api/issue/**,/api/node/**
```

> 网关只读启动时 `target/classes` 里的配置，改了 `src/main/resources` 必须重新编译/同步并重启；
> `/actuator/gateway/refresh` 在当前环境返回 404，不能靠热刷新。
> `rpzh-function` 本地还依赖 MQTT（1883）、ClickHouse（8123），并排除了
> `DruidDataSourceAutoConfigure`（与 `dynamic-datasource` 冲突）。

## 其他有意的取舍

- 「修改密码」调 `/username/editPassWord`（无需旧密码，后端就没做校验）
- 菜单页的搜索是**前端过滤**：`/menu/searchMenuList` 返回扁平 `MenuVo`，会丢失层级，不如本地过滤树结构直观
- `insertMenu` 只写 menu_name / parent_id / order_num / path / component / menu_type / status / perms / icon，所以弹窗里的「显示状态 / 是否外链 / 是否缓存」不会落库，界面上有提示
- 深色模式通过 `html.dark` + Element Plus dark css-vars 实现，刷新后保留

## 已验证

- `npm run build` 通过（1734 模块，**零警告**；`placeholder.vue` 改为动态 import 消除了重复导入警告）
- **完整链路实跑**：浏览器 → Vite 代理 → 网关 7001 → auth 16888 → system 16883，
  真实账号 `rpzh / 123456` 登录成功，拿到 JWT 并跳转 `/dashboard`
- 无头浏览器实跑：登录 → 侧边栏由 `/menu/getRouters` 生成 → 四个页面 → 新增用户弹窗，
  **零 Vue 运行时错误**；用户 6 行、角色 4 行、部门 6 行、菜单树 5 行，全部真实数据
- 登录日志写入 `tabac_maintenance_log.log_login` 成功（`@LoginLogging` 切面 Feign 链路打通）
- 接口自检页四个接口全绿

### function 模块（本轮）

- E2E（`e2e/smoke-function.mjs`）：真实登录 → 侧边栏出现「养护业务」分组 → **10/10 菜单点击成功**
  → 10 个 URL 直连 **0 白屏**；`console.error` 0、Vue/JS 运行时错误 0
- 真实数据渲染：节点 12 行、任务 10 行、仓库 2 / 垛位 6、预警历史 8 行、诱捕板 6 行、
  库区报警设置 3 行、阈值模板 2 行、监控中心 5 个接口自检全绿
- **Dao.xml 完成 Oracle → MySQL 适配后**：降级页面 **0 个**（原先养护计划、预警中心 2 个），
  养护计划 3 行、当前预警 2 行、虫害监测 12 行、阈值设置 6 行，全部真实数据；
  服务日志中 **0 个 SQL 语法错误**
- 接口回归（`e2e/verify-mysql-sql.mjs`）：21 个接口 16 个成功，其余 5 个为权限不足 /
  空配置表越界 / 业务空指针，均非 SQL 方言问题
- **养护计划新建全流程**：浏览器点「新建养护计划」→ 填表 → 提交，弹窗关闭、成功提示、
  列表 4→5 行且 `MAT-D008` 可见，0 运行时错误；接口直连 `flag=true` 且
  `updateMaintainPlanStageId` 影响 1 行，plan / stage / count / alarm_value_setting / early_time 均正确入库
- 截图在 `e2e/fn-*.png`、`e2e/plan-*.png`，完整结果在
  `e2e/function-e2e-result.json`、`e2e/plan-create-e2e-result.json`

### 养护业务 CRUD + 垛位筛选（本轮）

- **垛位筛选**：养护计划页提示「共 12 个垛位，8 个已有养护计划，4 个可选」，
  下拉只出现 `D009 / D010 / D011 / T2472`（其中 T2472 是页面新建分区下新建的垛位，
  验证了库区不再写死 201~205）；点「显示全部（含已有计划）」可切换回全量并标注「（已有计划）」
- **仓库五级 CRUD**：仓库 2 / 楼栋 3 / 楼层 2 / 分区 3 / 垛位 3 行均正常渲染，
  浏览器真实提交「新建楼栋」→ `tb_building` 新增 id=14，`site_name` 已带 `/` 分隔
- **各页面按钮与弹窗**（`e2e/smoke-crud2.mjs`）：节点（12 字段）、养护计划（31 字段）、
  任务（8 字段）、虫害（12 字段）、阈值模板（24 字段）新建弹窗全部可打开，0 运行时错误
- **养护计划新建 + 修改全流程**（`e2e/smoke-plan-create-edit.mjs`）：在 T2472 上新建计划
  → 列表 6→7 行（plan id=10 / stage id=13）→ 点「修改」回显 24 个字段 → 养护天数 30→31 提交
  → `tb_maintain_plan_stage.maintain_days=31`、`aftercare_date_end` 重算为 `2026-12-06`、
  `update_user=rpzh`，`tb_maintain_count` 关联行正确
- **修改弹窗修复**：`beforeUpdateMaintainPlan` 原先传裸 `pid_route` 给 `getAlarmValueSetting`，
  `split(":")` 后取 `route[1]` 必抛 `ArrayIndexOutOfBoundsException: 1`，弹窗「闪一下就关」；
  修复后接口 `flag=true`，回显 `stackId=303` / 物料 / 阈值 / 阶段全部正常
- **全页面回归**（`e2e/smoke-function.mjs`）：10/10 菜单点击成功、10 个 URL 直连 **0 白屏**、
  降级 **0 个**、`console.error` 0、Vue/JS 运行时错误 0
- 本轮结果文件：`e2e/crud-e2e-result.json`、`e2e/crud-e2e-result2.json`、
  `e2e/plan-create-edit-result.json`
