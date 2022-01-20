package com.rpzh.service.impl;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.UserDao;
import com.rpzh.dto.UserDto;
import com.rpzh.entity.ToolUtil;
import com.rpzh.pojo.User;
import com.rpzh.service.UserService;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author liubc
 * @create 2021/3/30 16:32
 */
@Service
 public class UserServiceImpl implements UserService {


    @Autowired
    private UserDao userDao;


    /**
     * 用户登录
     *
     * @param id
     * @return
     */
    @Override
    public User findById(String id) {
        User user = userDao.findById(id);
        return user;
    }

    /**
     * 添加用户
     *
     * @param userDto
     * @return
     */
    @Override
    @Transactional
    public Integer addUser(UserDto userDto) {
        //获取用户账号,判断用户的账号的唯一性
        String userName = userDto.getUserName();
        //获取用户名称
        String nickName = userDto.getNickName();
        //获取用户密码
        String password = userDto.getPassword();
        if (ToolUtil.isEmpty(userName) || ToolUtil.isEmpty(nickName) || ToolUtil.isEmpty(password)){
            throw new RuntimeException("用户账号,用户密码,用户名称不能为空");
        }
        String template="[A-Za-z0-9]+";//用户账号只能含有字母和数字
        boolean matches = userName.matches(template);
        if (!matches){
            throw new RuntimeException("用户账号只能包含字母和数字");
        }
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");//密码中不能含有中文
        Matcher m = p.matcher(password);
        if (m.find()){
            throw new RuntimeException("密码中不能含有中文");
        }
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        userDto.setCreateBy(username);
        //获取用户手机号
        String phonenumber = userDto.getPhonenumber();
        //查看手机号是否唯一
        Integer phoneCount = userDao.findCountPhoneNumber(phonenumber);
        if (phoneCount > 0){
            throw new RuntimeException("手机号已经存在");
        }
        //查询用户账号是否存在
        Integer usernameCount = userDao.findUserNameCount(userName);
        if (usernameCount > 0){
            throw new RuntimeException("该账号已经存在,不允许添加");
        }
        //密码加密
        String passWord = new BCryptPasswordEncoder().encode(password);
        userDto.setPassword(passWord);
        //未加密的密码,用于Mq同步用户数据
        userDto.setNoEncryPassWord(password);
        //添加用户
        Integer insertUserFlag = userDao.addUser(userDto);
        //根据用户账号查询用户id
        Integer userId = userDao.findUserByUserName(userName);
        //获取部门id
        Long deptId = userDto.getDeptId();
        //将用户id,部门id插入到用户部门关联表中
        if (ToolUtil.isNotEmpty(deptId)){
            Integer userDept = userDao.insertUserDept(userId,deptId);
        }
        //获取角色
        Long[] roleIds = userDto.getRoleIds();
        //将用户id ,角色id插入到用户角色关联表
        if (ToolUtil.isNotEmpty(roleIds)){
            Integer userRoleFlag = userDao.insertUserRole(userId, roleIds);
        }
        return insertUserFlag;
    }

    /**
     * 分页查询所有用户信息
     *
     * @param userDto
     * @return
     */
    @Override
    public PageInfo<UserVo> findAllUserList(UserDto userDto) {
        int page = userDto.getPage();
        int size = userDto.getSize();
        //静态分页
        PageHelper.startPage(page, size);
        List<UserVo> userVoList = userDao.findAllUserList(userDto);
        return new PageInfo<UserVo>(userVoList);
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public Integer deleteUser(Long[] userId) {
        if (ToolUtil.isEmpty(userId)){
            throw new RuntimeException("请选择删除的用户");
        }
        //获取登录用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //查看用户账号
        String username = userInfo.get("username");
        //根据用户账号查询用户的id
        Long userid = userDao.findUserId(username);
        for (int i = 0; i < userId.length; i++) {
            Long aLong = userId[i];
            if (userid.equals(aLong)){
                throw new RuntimeException("不能删除自己账号"+username);
            }
            if (1 == aLong){
                //超级管理员不允许操作
                throw new RuntimeException("超级管理员用户不允许删除");
            }
        }
        //删除用户角色关联表
        Integer deleteUserRoleFlag = userDao.deleteUserConnectRole(userId);
        Integer deleteUserDeptFlag = userDao.deleteUserConnectDept(userId);
        Integer deleteFlag = userDao.updateUser(userId);
        return deleteFlag;
    }

    /**
     * 修改用户
     *
     * @param userDto
     * @return
     */
    @Override
    @Transactional
    public Integer editUser(UserDto userDto) {
        //获取用户id
        Long userId = userDto.getUserId();
        //判断是否是超级管理员
        if (1 == userId){
            //超级管理员只允许修改昵称,部门,手机号,邮箱,性别
            Integer updateAdminFlag = userDao.updateAdmin(userDto);
            //删除用户部门关联表
            Integer deleteUserDeptFlag = userDao.deleteUserDept(userId);
            //添加用户部门关联
            if (ToolUtil.isNotEmpty(userDto.getDeptId())){
                Integer addUserDeptFlag = userDao.addUserDept(userDto);
            }
            return updateAdminFlag;

        }else {
            //获取登录用户信息
            Map<String, String> userInfo = TokenDecode.getUserInfo();
            //查看用户账号
            String username = userInfo.get("username");
            userDto.setUpdateBy(username);
            //修改普通用户
            Integer updateCommonUserFlag = userDao.updateCommonUser(userDto);
            //删除用户部门关联表
            Integer deleteUserDeptFlag = userDao.deleteUserDept(userId);
            //添加用户部门关联
            if (ToolUtil.isNotEmpty(userDto.getDeptId())){
                Integer addUserDeptFlag = userDao.addUserDept(userDto);
            }
            //删除用户角色关联表
            Integer deleteUserRoleFlag = userDao.deleteUserRole(userId);
            //获取角色
            Long[] roleIds = userDto.getRoleIds();
            //添加用户角色关联表
            Integer addUserRoleFlag = userDao.addUserRole(userId,roleIds);
            return updateCommonUserFlag;
        }
    }

    /**
     * 用户修改密码
     *
     * @param username
     * @param newpassWord
     * @return
     */
    @Override
    public Integer editPassWord(String username,String newpassWord) {
        //密码加密
        String passWord = new BCryptPasswordEncoder().encode(newpassWord);
        Integer updatePasswordFlag = userDao.editPassWord(username,passWord);
        //获取当前时间
        DateTime dateTime = DateUtil.dateNew(new Date());
        //修改更新时间
        int updateTime = userDao.updateTime(username,dateTime);
        return updatePasswordFlag;
    }

    /**
     * 获取用户权限
     *
     * @return
     */
    @Override
    public String getUserRole(String username) {
        //获取角色集合
        List<String> rolesList = userDao.findUserRole(username);
        Set<String> set = new HashSet<>();
        String strRole = "";
        //获取所有的角色
        for (int i = 0; i < rolesList.size(); i++) {
            //取出每一个角色
            String role = rolesList.get(i);
            //获取角色对应的菜单权限
            List<String> roleList = userDao.findRole(role);
            for (int i1 = 0; i1 < roleList.size(); i1++) {
                //获取每一个权限
                String roleKey = roleList.get(i1);
                set.add(roleKey);
            }
        }
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()){
            String next = iterator.next();
            strRole = strRole + next + ",";
        }
        if (strRole.length() > 0){
            strRole = strRole.substring(0, strRole.length()-1);
        }
        return strRole;
    }

    /**
     * 超级管理员可以修改所有人密码
     * @param username
     * @param newpassWord
     * @return
     */
    @Override
    @Transactional
    public Integer adminUpdatePassWord(String username, String newpassWord) {
        //获取登录用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //查看用户账号
        String adminUsername = userInfo.get("username");
        //查看是否是管理员
        List<Integer> list = userDao.findIsAdminRole(adminUsername);
        if (ToolUtil.isEmpty(list)){
            throw new RuntimeException("你无权操作,不允许访问");
        }
        int flag = 0;
        for (int i = 0; i < list.size(); i++) {
            Integer roleId = list.get(i);
            if (roleId == 1){
                flag = 1;
            }
        }
        if (flag == 0){
            throw new RuntimeException("你无权操作,不允许访问");
        }
        //密码加密
        String passWord = new BCryptPasswordEncoder().encode(newpassWord);
        Integer adminUpdatePassWordFlag = userDao.adminUpdatePassWord(username,passWord);
        //修改密码后,当前用户的错误次数改为0
        int updateErrorTimes = userDao.updateErrorTimes(username);
        //获取当前时间
        DateTime dateTime = DateUtil.dateNew(new Date());
        //修改更新时间
        int updateTime = userDao.updateTime(username,dateTime);
        return adminUpdatePassWordFlag;
    }

    /**
     * 修改用户状态
     *
     * @param userDto
     * @return
     */
    @Override
    public Integer changeUserStatus(UserDto userDto) {
        //获取用户信息
        Long userId = userDto.getUserId();
        if (ToolUtil.isEmpty(userId) || ToolUtil.isEmpty(userDto.getStatus())){
            throw new RuntimeException("参数错误");
        }
        //不允许修改超级管理员角色
        if (userId == 1){
            throw new RuntimeException("不允许修改超级管理员用户");
        }
        //获取登录用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //查看用户账号
        String username = userInfo.get("username");
        //根据用户账号查询用户的id
        Long userid = userDao.findUserId(username);
        if (userId.equals(userid)){
            throw new RuntimeException("不允许修改自己账号状态");
        }
        Integer changeFlage = userDao.changeUserStatus(userDto);
        return changeFlage;
    }

    /**
     * 修改用户时返回
     *
     * @param userId
     * @return
     */
    @Override
    public List<Integer> findUserByUpdate(Long userId) {
        List<Integer> roidList = userDao.findUserByUpdate(userId);
        return roidList;
    }

    /**
     * 查询个人信息
     *
     * @return
     */
    @Override
    public UserVo findOneUserMessage() {
        //获取登录用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //查看用户账号
        String username = userInfo.get("username");
        //根据用户账号查询用户信息
        UserVo userVo = userDao.findOneUserMessage(username);
        List<String> roleNameList = userDao.findRoleNameList(username);
        userVo.setRoleNames(roleNameList);
        return userVo;
    }

    /**
     * 修改个人信息
     *
     * @param userDto
     * @return
     */
    @Override
    public Integer changeUserMessage(UserDto userDto) {
        //获取登录用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");
        userDto.setUserName(username);
        Integer changeFlag = userDao.changeUserMessage(userDto);
        return changeFlag;
    }

}