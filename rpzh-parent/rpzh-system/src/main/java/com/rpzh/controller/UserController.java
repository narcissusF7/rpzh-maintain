package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.UserDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.User;
import com.rpzh.service.UserService;
import com.rpzh.vo.UserVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 用户管理
 * @Author liubc
 * @create 2021/3/30 16:32
 */
@RestController
@RequestMapping("/username")
@CrossOrigin
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 查询用户的信息,用户登录查询用户密码
     * @param id
     * @return
     */
    @RequestMapping(value = {"/{id}"})
    public Result<User> findById(@PathVariable(value = "id") String id){
        //调用UserService实现根据主键查询User
        User user = userService.findById(id);
        return new Result<User>(true, StatusCode.OK,"查询成功",user);
    }

    /**
     * 新增用户
     * @param userDto
     * @return
     */
    @PostMapping("/addUser")
    public Result addUser(@RequestBody UserDto userDto){
        Long[] roleIds = userDto.getRoleIds();
        if (ToolUtil.isEmpty(roleIds)){
            return new Result(false, StatusCode.ERROR, "添加用户失败,请选择角色");
        }
        //添加用户
        Integer insertFlag = userService.addUser(userDto);
        return new Result(true, StatusCode.OK, "添加用户成功");
    }

    /**
     * 分页搜索查询用户
     * @param userDto
     * @return
     */
    @PostMapping("/findAllUserList")
    public Result<PageInfo> findAllUserList(UserDto userDto){
        PageInfo<UserVo> pageInfo = userService.findAllUserList(userDto);
        return new Result<PageInfo>(true, StatusCode.OK,"查询成功",pageInfo);
    }


    /**
     * 修改用户的状态
     * @param userDto
     * @return
     */
    @PostMapping("/changeUserStatus")
    public Result changeUserStatus(UserDto userDto){
        Integer changeFlag = userService.changeUserStatus(userDto);
        return new Result(true, StatusCode.OK, "修改状态成功");
    }

    /**
     * 修改用户
     * @param userDto
     * @return
     */
    @PostMapping("/editUser")
    public Result editUser(UserDto userDto){
        //获取角色
        Long[] roleIds = userDto.getRoleIds();
        if (roleIds.length == 0){
            return new Result(false, StatusCode.ERROR, "请选择用户角色");
        }
        //修改用户
        Integer updateFlag = userService.editUser(userDto);
        return new Result(true, StatusCode.OK, "修改用户成功");
    }


    /**
     *删除用户
     * @param userId
     * @return
     */
    @GetMapping("/deleteUser")
    public Result deleteUser(Long[] userId){
        Integer deleteFlag = userService.deleteUser(userId);
        return new Result(true, StatusCode.OK, "删除用户成功");
    }


    /**
     * 普通用户修改密码
     * @param username
     * @param newpassWord
     * @return
     */
    @GetMapping("/editPassWord")
    public Result editPassWord(String username, String newpassWord, String notarizePassword){
        if (ToolUtil.isEmpty(username) || ToolUtil.isEmpty(newpassWord) || ToolUtil.isEmpty(notarizePassword)){
            return new Result(false, StatusCode.ERROR, "请输入账号密码");
        }
        if (!(newpassWord.equals(notarizePassword))){
            return new Result(false, StatusCode.ERROR, "密码与确认密码不一致,请重新输入");
        }
        Integer updatePassWordFlag = userService.editPassWord(username,newpassWord);
        return new Result(true, StatusCode.OK, "修改成功");
    }


    /**
     * 获取用户的角色权限
     * @return
     */
    @GetMapping("/getUserRole")
    public String getUserRole(String username){
       String roles = userService.getUserRole(username);
       return roles;
    }

    /**
     * 超级管理员可以修改所有人密码
     * @param username
     * @param newpassWord
     * @return
     */
    @GetMapping("/adminUpdatePassWord")
    public Result adminUpdatePassWord(String username, String newpassWord){
        if (ToolUtil.isEmpty(username) || ToolUtil.isEmpty(newpassWord)){
            return new Result(false, StatusCode.ERROR, "请输入密码");
        }
        Integer adminUpdatePassWordFlag = userService.adminUpdatePassWord(username,newpassWord);
        return new Result(true, StatusCode.OK, "修改密码成功");
    }


    /**
     * 修改用户时返回用户信息
     * @param userId
     * @return
     */
    @RequestMapping("/findUserByUpdate")
    public Result findUserByUpdate(Long userId){
        List<Integer> roidList = userService.findUserByUpdate(userId);
        return new Result(true, StatusCode.OK, "查询用户角色成功", roidList);
    }

    /**
     * 查询个人信息
     * @return
     */
    @GetMapping("/findOneUserMessage")
    public Result findOneUserMessage(){
        UserVo userVo = userService.findOneUserMessage();
        return new Result(true, StatusCode.OK, "查询个人信息成功",userVo);
    }


    /**
     * 个人中心修改用户信息
     * @param
     * @return
     */
    @PostMapping("/changeUserMessage")
    public Result changeUserMessage(UserDto userDto){
        Integer changeFlage = userService.changeUserMessage(userDto);
        return new Result(true, StatusCode.OK, "修改个人信息成功");
    }

}