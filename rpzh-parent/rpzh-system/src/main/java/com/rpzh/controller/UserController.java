package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.annotation.OperationLog;
import com.rpzh.dao.UserDao;
import com.rpzh.dto.UserDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.BesinessUserType;
import com.rpzh.enums.BusinessType;
import com.rpzh.pojo.User;
import com.rpzh.service.UserService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.UserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 用户管理
 *
 * @Author liubc
 * @create 2021/3/30 16:32
 */
@RestController
@RequestMapping("/username")
@CrossOrigin
@Api(value = "用户管理", tags = "用户管理", description = "用户管理")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 查询用户的信息,用户登录查询用户密码
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "查询用户信息,登录时查看用户密码", tags = "用户管理")
    @RequestMapping(value = {"/{id}"})
    @PreAuthorize("hasAnyAuthority('system:user:query','admin')")
    public Result<User> findById(@PathVariable(value = "id") String id) {
        //调用UserService实现根据主键查询User
        User user = userService.findById(id);
        return new Result<User>(true, StatusCode.OK, "查询成功", user);
    }

    /**
     * 新增用户
     *
     * @param userDto
     * @return
     */
    @ApiOperation(value = "新增用户", tags = "用户管理")
    //@OperationUserLog(title = "新增用户", businessUserType = BesinessUserType.INSERT)
    @PostMapping("/addUser")
    @PreAuthorize("hasAnyAuthority('system:user:add','admin')")
    @OperationLog(mapper = UserService.class, businessType = BusinessType.INSERT, title = "用户管理")
    public Result addUser(UserDto userDto) {
        Long[] roleIds = userDto.getRoleIds();
        if (ToolUtil.isEmpty(roleIds)) {
            return new Result(false, StatusCode.ERROR, "添加用户失败,请选择角色");
        }
        //添加用户
        Integer insertFlag = userService.addUser(userDto);
        return new Result(true, StatusCode.OK, "添加用户成功");
    }

    /**
     * 分页搜索查询用户
     *
     * @param userDto
     * @return
     */
    @ApiOperation(value = "分页搜索查询用户", tags = "用户管理")
    @PostMapping("/findAllUserList")
    public Result<PageInfo> findAllUserList(UserDto userDto) {
        PageInfo<UserVo> pageInfo = userService.findAllUserList(userDto);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /**
     * 修改用户的状态
     *
     * @param userDto
     * @return
     */
    @ApiOperation(value = "修改用户的状态", tags = "用户管理")
   // @OperationUserLog(title = "修改用户的状态", businessUserType = BesinessUserType.UPDATESTATUS)
    @PostMapping("/changeUserStatus")
    @OperationLog(mapper = UserService.class, businessType = BusinessType.UPDATE, title = "用户管理")
    public Result changeUserStatus(UserDto userDto) {
        Integer changeFlag = userService.changeUserStatus(userDto);
        return new Result(true, StatusCode.OK, "修改状态成功");
    }

    /**
     * 修改用户
     *
     * @param userDto
     * @return
     */
    @PostMapping("/editUser")
    @PreAuthorize("hasAnyAuthority('system:user:edit','admin')")
    //@OperationUserLog(title = "修改用户", businessUserType = BesinessUserType.UPDATE)
    @ApiOperation(value = "修改用户", tags = "用户管理")
    @OperationLog(mapper = UserService.class, businessType = BusinessType.UPDATE, title = "用户管理")
    public Result editUser(UserDto userDto) {
        //获取角色
        Long[] roleIds = userDto.getRoleIds();
        if (roleIds.length == 0) {
            return new Result(false, StatusCode.ERROR, "请选择用户角色");
        }
        //修改用户
        Integer updateFlag = userService.editUser(userDto);
        return new Result(true, StatusCode.OK, "修改用户成功");
    }


    /**
     * 删除用户
     *
     * @param userDto
     * @return
     */
    @ApiOperation(value = "删除用户", tags = "用户管理")
    //@OperationUserLog(title = "删除用户", businessUserType = BesinessUserType.DELETE)
    @PostMapping("/deleteUser")
    @PreAuthorize("hasAnyAuthority('system:user:remove','admin')")
    @OperationLog(mapper = UserService.class, businessType = BusinessType.DELETE, title = "用户管理")
    public Result deleteUser(UserDto userDto) {
        Long[] userId = userDto.getUserIds();
        Integer deleteFlag = userService.deleteUser(userId);
        return new Result(true, StatusCode.OK, "删除用户成功");
    }


    /**
     * 普通用户修改密码
     *
     * @param username
     * @param newpassWord
     * @return
     */
    @ApiOperation(value = "普通用户修改密码", tags = "用户管理")
    //@OperationUserLog(title = "新增用户", businessUserType = BesinessUserType.PASSWORD)
    @GetMapping("/editPassWord")
    @OperationLog(mapper = UserService.class, businessType = BusinessType.UPDATE, title = "用户管理")
    public Result editPassWord(@RequestParam("username") String username, @RequestParam("newpassWord") String newpassWord, @RequestParam("notarizePassword") String notarizePassword) {
        if (ToolUtil.isEmpty(username) || ToolUtil.isEmpty(newpassWord) || ToolUtil.isEmpty(notarizePassword)) {
            return new Result(false, StatusCode.ERROR, "请输入账号密码");
        }
        if (!(newpassWord.equals(notarizePassword))) {
            return new Result(false, StatusCode.ERROR, "密码与确认密码不一致,请重新输入");
        }
        // 普通用户只能改自己的密码；管理员及内部服务令牌可代改（auth 服务已校验旧密码）
        String loginName = null;
        try {
            Map<String, String> userInfo = TokenDecode.getUserInfo();
            loginName = userInfo == null ? null : userInfo.get("username");
        } catch (Exception e) {
            LOG.warn("解析登录令牌失败", e);
        }
        boolean privileged = false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String granted = authority.getAuthority();
                if ("admin".equals(granted) || "system:user:edit".equals(granted)) {
                    privileged = true;
                    break;
                }
            }
        }
        if (!privileged && (loginName == null || !loginName.equals(username))) {
            return new Result(false, StatusCode.ERROR, "无权修改他人密码");
        }
        Integer updatePassWordFlag = userService.editPassWord(username, newpassWord);
        return new Result(true, StatusCode.OK, "修改成功");
    }


    /**
     * 获取用户的角色权限
     *
     * @return
     */
    @ApiOperation(value = "获取用户的角色权限", tags = "用户管理")
    @GetMapping("/getUserRole")
    public String getUserRole(@ApiParam(name = "username", value = "用户账号") String username) {
        String roles = userService.getUserRole(username);
        return roles;
    }

    /**
     * 中台账号解析为本地账号（中台化改造）。
     * 已绑定返回本地账号；未绑定自动开户并绑定默认角色。
     */
    @ApiOperation(value = "中台账号解析为本地账号", tags = "用户管理")
    @GetMapping("/resolvePlatformUser")
    public Result<String> resolvePlatformUser(@ApiParam(name = "platformUid", value = "中台用户唯一标识") String platformUid,
                                              @ApiParam(name = "platformUsername", value = "中台登录账号/昵称") String platformUsername,
                                              @ApiParam(name = "defaultRoleKey", value = "自动开户默认角色") String defaultRoleKey) {
        String username = userService.resolvePlatformUser(platformUid, platformUsername,
                StringUtils.hasText(defaultRoleKey) ? defaultRoleKey : "common");
        return new Result<>(true, StatusCode.OK, "ok", username);
    }

    /**
     * 超级管理员可以修改所有人密码
     *
     * @param username
     * @param newpassWord
     * @return
     */
    @ApiOperation(value = "超级管理员可以修改所有人密码", tags = "用户管理")
    //@OperationUserLog(title = "超级管理员修改密码", businessUserType = BesinessUserType.ADMINPASSWORD)
    @GetMapping("/adminUpdatePassWord")
    @PreAuthorize("hasAnyAuthority('system:user:edit','admin')")
    @OperationLog(mapper = UserService.class, businessType = BusinessType.UPDATE, title = "用户管理")
    public Result adminUpdatePassWord(@RequestParam("username") String username, @RequestParam("newpassWord") String newpassWord) {
        if (ToolUtil.isEmpty(username) || ToolUtil.isEmpty(newpassWord)) {
            return new Result(false, StatusCode.ERROR, "请输入密码");
        }
        Integer adminUpdatePassWordFlag = userService.adminUpdatePassWord(username, newpassWord);
        return new Result(true, StatusCode.OK, "修改密码成功");
    }


    /**
     * 修改用户时返回用户信息
     *
     * @param userId
     * @return
     */
    @ApiOperation(value = "修改用户时返回用户信息", tags = "用户管理")
    @RequestMapping("/findUserByUpdate")
    public Result findUserByUpdate(@RequestParam("userId") Long userId) {
        List<Integer> roidList = userService.findUserByUpdate(userId);
        return new Result(true, StatusCode.OK, "查询用户角色成功", roidList);
    }

    /**
     * 预热单个用户的 Redis 缓存（USER_账号 / MAINTIAN_USER_ID_USERNAME）
     * <p>
     * 正常登录时会自动预热，此接口用于排查和人工补偿。
     *
     * @param username 用户账号
     * @return
     */
    @ApiOperation(value = "预热单个用户缓存", tags = "用户管理")
    @GetMapping("/warmUpUserCache")
    public Result warmUpUserCache(@RequestParam("username") String username) {
        if (ToolUtil.isEmpty(username)) {
            return new Result(false, StatusCode.ERROR, "用户账号不能为空");
        }
        boolean success = userService.warmUpUserCache(username);
        if (!success) {
            return new Result(false, StatusCode.ERROR, "缓存预热失败,用户不存在或已删除");
        }
        return new Result(true, StatusCode.OK, "缓存预热成功");
    }

    /**
     * 全量预热所有有效用户的 Redis 缓存，用于存量用户一次性补齐
     *
     * @return
     */
    @ApiOperation(value = "全量预热用户缓存", tags = "用户管理")
    @GetMapping("/warmUpAllUserCache")
    public Result warmUpAllUserCache() {
        int count = userService.warmUpAllUserCache();
        return new Result(true, StatusCode.OK, "缓存预热完成,成功 " + count + " 个用户", count);
    }

    /**
     * 查询个人信息
     *
     * @return
     */
    @ApiOperation(value = "查询个人信息", tags = "用户管理")
    @GetMapping("/findOneUserMessage")
    public Result findOneUserMessage() {
        UserVo userVo = userService.findOneUserMessage();
        return new Result(true, StatusCode.OK, "查询个人信息成功", userVo);
    }


    /**
     * 个人中心修改用户信息
     *
     * @param
     * @return
     */
    @ApiOperation(value = "个人中心修改用户信息", tags = "用户管理")
    //@OperationUserLog(title = "个人中心修改用户信息", businessUserType = BesinessUserType.PERSONALUPDATE)
    @PostMapping("/changeUserMessage")
    @OperationLog(mapper = UserService.class, businessType = BusinessType.UPDATE, title = "用户管理")
    public Result changeUserMessage(UserDto userDto) {
        Integer changeFlage = userService.changeUserMessage(userDto);
        return new Result(true, StatusCode.OK, "修改个人信息成功");
    }

}