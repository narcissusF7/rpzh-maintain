/**
 * 本地兜底菜单树（结构与后端 /menu/getRouters 返回的 Menu 一致）
 *
 * 什么时候会用到：
 * 1. 后端 /menu/getRouters 返回空数组（MenuServiceImpl 里用户名写死为 "rpzh"，
 *    而 tb_user 里没有这个账号时，查出来的菜单就是空的）；
 * 2. 接口异常 / 未启动。
 *
 * 后端菜单一旦返回数据，会优先使用后端的，这里的配置自动失效。
 * sql/menu-seed.sql 就是按这份结构写的初始化数据，两边保持一致。
 */
export const localMenus = [
  {
    menuId: 1000,
    menuName: '系统管理',
    parentId: 0,
    path: 'system',
    component: null,
    menuType: 'M',
    visible: '0',
    status: '1',
    perms: '',
    icon: 'Setting',
    orderNum: 1,
    children: [
      {
        menuId: 1001,
        menuName: '用户管理',
        parentId: 1000,
        path: 'user',
        component: 'system/user/index',
        menuType: 'C',
        visible: '0',
        status: '1',
        perms: 'system:user:list',
        icon: 'User',
        orderNum: 1,
        children: []
      },
      {
        menuId: 1002,
        menuName: '角色管理',
        parentId: 1000,
        path: 'role',
        component: 'system/role/index',
        menuType: 'C',
        visible: '0',
        status: '1',
        perms: 'system:role:list',
        icon: 'UserFilled',
        orderNum: 2,
        children: []
      },
      {
        menuId: 1003,
        menuName: '菜单管理',
        parentId: 1000,
        path: 'menu',
        component: 'system/menu/index',
        menuType: 'C',
        visible: '0',
        status: '1',
        perms: 'system:menu:list',
        icon: 'Menu',
        orderNum: 3,
        children: []
      },
      {
        menuId: 1004,
        menuName: '部门管理',
        parentId: 1000,
        path: 'dept',
        component: 'system/dept/index',
        menuType: 'C',
        visible: '0',
        status: '1',
        perms: 'system:dept:list',
        icon: 'OfficeBuilding',
        orderNum: 4,
        children: []
      }
    ]
  },
  {
    menuId: 2100,
    menuName: '养护业务',
    parentId: 0,
    path: 'function',
    component: null,
    menuType: 'M',
    visible: '0',
    status: '1',
    perms: '',
    icon: 'DataAnalysis',
    orderNum: 2,
    children: [
      { menuId: 2101, menuName: '监控中心', parentId: 2100, path: 'monitoring', component: 'function/monitoring/index', menuType: 'C', visible: '0', status: '1', perms: 'f:m:f', icon: 'Monitor', orderNum: 1, children: [] },
      { menuId: 2102, menuName: '仓库管理', parentId: 2100, path: 'warehouse', component: 'function/warehouse/index', menuType: 'C', visible: '0', status: '1', perms: 'p:w:f', icon: 'House', orderNum: 2, children: [] },
      { menuId: 2103, menuName: '节点管理', parentId: 2100, path: 'node', component: 'function/node/index', menuType: 'C', visible: '0', status: '1', perms: 'f:n:up', icon: 'Connection', orderNum: 3, children: [] },
      { menuId: 2104, menuName: '养护计划', parentId: 2100, path: 'plan', component: 'function/plan/index', menuType: 'C', visible: '0', status: '1', perms: 'f:p:add', icon: 'Calendar', orderNum: 4, children: [] },
      { menuId: 2105, menuName: '预警中心', parentId: 2100, path: 'warning', component: 'function/warning/index', menuType: 'C', visible: '0', status: '1', perms: 'f:e:upr', icon: 'Warning', orderNum: 5, children: [] },
      { menuId: 2106, menuName: '任务管理', parentId: 2100, path: 'task', component: 'function/task/index', menuType: 'C', visible: '0', status: '1', perms: 'f:t:add', icon: 'Tickets', orderNum: 6, children: [] },
      { menuId: 2107, menuName: '虫害监测', parentId: 2100, path: 'pest', component: 'function/pest/index', menuType: 'C', visible: '0', status: '1', perms: 'f:h:fwc', icon: 'Aim', orderNum: 7, children: [] },
      { menuId: 2108, menuName: '数据报表', parentId: 2100, path: 'report', component: 'function/report/index', menuType: 'C', visible: '0', status: '1', perms: 'f:m:hcf', icon: 'DataLine', orderNum: 8, children: [] },
      { menuId: 2109, menuName: '阈值设置', parentId: 2100, path: 'alarm-setting', component: 'function/alarm-setting/index', menuType: 'C', visible: '0', status: '1', perms: 'f:a:lud', icon: 'Setting', orderNum: 9, children: [] },
      { menuId: 2110, menuName: '物料管理', parentId: 2100, path: 'material', component: 'function/material/index', menuType: 'C', visible: '0', status: '1', perms: 'f:m:up', icon: 'Box', orderNum: 10, children: [] },
      { menuId: 2115, menuName: '养护对比', parentId: 2100, path: 'compare', component: 'function/compare/index', menuType: 'C', visible: '0', status: '1', perms: 'f:c:v', icon: 'DataAnalysis', orderNum: 11, children: [] }
    ]
  }
]
