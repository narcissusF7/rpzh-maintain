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
import com.rpzh.util.RedisUtil;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.UserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author liubc
 * @create 2021/3/30 16:32
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserDao userDao;
    @Resource
    private RedisUtil redisUtil;

    private final String accountId = "ACCOUNT_ID_";

    /** 下游 function/job 模块读取用户昵称、手机号等信息的 hash key 前缀 */
    private static final String USER_KEY_PREFIX = "USER_";
    /** userId -> 账号 的反查映射 key（与 function 模块保持一致） */
    private static final String USER_ID_USERNAME_KEY = "MAINTIAN_USER_ID_USERNAME";
    private static final String USER_ID_FIELD_PREFIX = "USER_ID_";


    /**
     * 用户登录
     *
     * @param id
     * @return
     */
    @Override
    public User findById(String id) {
        User user = userDao.findById(id);
        if (user == null) {
            return null;
        }
        // 登录链路（auth 的 UserDetailsServiceImpl 必调本方法）顺带预热用户缓存；
        // 缓存写入失败绝不能影响登录，整段兜住异常。
        try {
            redisUtil.set(accountId + user.getUsername(), user.getNickname() + "(" + user.getUsername() + ")");
            warmUpUserCache(user.getUsername());
        } catch (Exception e) {
            log.warn("用户缓存预热失败, username={}", id, e);
        }
        return user;
    }

    /**
     * 预热单个用户的 Redis 缓存。
     * <p>
     * 写入 USER_账号 的 hash（nickname / phonenumber / userId / deptname / roleName）以及
     * MAINTIAN_USER_ID_USERNAME 中 userId -> 账号 的反查映射，保证下游按账号取昵称、
     * 按负责人 id 还原账号时不再大面积落空。
     *
     * @param username 用户账号
     * @return 是否写入成功
     */
    @Override
    public boolean warmUpUserCache(String username) {
        if (ToolUtil.isEmpty(username)) {
            return false;
        }
        UserVo info = userDao.findUserCache(username);
        if (info == null) {
            log.warn("用户缓存预热跳过, 用户不存在或已删除, username={}", username);
            return false;
        }
        Map<String, Object> cache = new LinkedHashMap<>();
        cache.put("userId", info.getUserId());
        cache.put("nickname", info.getNickname());
        cache.put("phonenumber", info.getPhonenumber());
        cache.put("deptname", info.getDeptname());
        cache.put("roleName", joinRoleName(username));
        boolean ok = redisUtil.hmset(USER_KEY_PREFIX + username, cache);
        if (info.getUserId() != null) {
            redisUtil.hset(USER_ID_USERNAME_KEY, USER_ID_FIELD_PREFIX + info.getUserId(), username);
        }
        return ok;
    }

    /**
     * 全量预热所有有效用户的缓存（存量用户无需逐个登录即可补齐 USER_ 键）
     *
     * @return 成功预热的用户数
     */
    @Override
    public int warmUpAllUserCache() {
        List<String> usernames = userDao.findAllValidUsername();
        if (ToolUtil.isEmpty(usernames)) {
            return 0;
        }
        int success = 0;
        for (String username : usernames) {
            try {
                if (warmUpUserCache(username)) {
                    success++;
                }
            } catch (Exception e) {
                log.warn("用户缓存预热失败, username={}", username, e);
            }
        }
        log.info("用户缓存全量预热完成, 成功 {}/{}", success, usernames.size());
        return success;
    }

    /**
     * 按用户 id 刷新缓存（用户信息被管理员修改后调用，避免旧昵称残留在列表里）
     */
    private void refreshUserCacheById(Long userId) {
        if (ToolUtil.isEmpty(userId)) {
            return;
        }
        try {
            String username = userDao.findUsernameById(userId);
            if (!ToolUtil.isEmpty(username)) {
                warmUpUserCache(username);
            }
        } catch (Exception e) {
            log.warn("用户缓存刷新失败, userId={}", userId, e);
        }
    }

    /**
     * 拼接用户的角色名称，多个用逗号分隔（与 function 模块读取 roleName 的口径一致）
     */
    private String joinRoleName(String username) {
        List<String> roleNames = userDao.findRoleNameList(username);
        if (ToolUtil.isEmpty(roleNames)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String roleName : roleNames) {
            if (ToolUtil.isEmpty(roleName)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(roleName);
        }
        return builder.toString();
    }

    /**
     * 用户登录
     *
     * @param id
     * @return
     */
    @Override
    public User logFindById(String id) {
        return userDao.logFindById(id);
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
        if (ToolUtil.isEmpty(userName) || ToolUtil.isEmpty(nickName) || ToolUtil.isEmpty(password)) {
            throw new RuntimeException("用户账号,用户密码,用户名称不能为空");
        }
        String template = "[A-Za-z0-9]+";//用户账号只能含有字母和数字
        boolean matches = userName.matches(template);
        if (!matches) {
            throw new RuntimeException("用户账号只能包含字母和数字");
        }
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");//密码中不能含有中文
        Matcher m = p.matcher(password);
        if (m.find()) {
            throw new RuntimeException("密码中不能含有中文");
        }
        //获取创建用户,Mq收到消息后,不会携带令牌,无法通过令牌获取用户信息
        String createBy = userDto.getCreateBy();
        if (ToolUtil.isEmpty(createBy)) {
            //获取用户信息,手动创建用户时,从令牌中获取创建用户的信息
            Map<String, String> userInfo = TokenDecode.getUserInfo();
            //获取用户名
            String username = userInfo.get("username");
            userDto.setCreateBy(username);
        }
        //获取用户手机号
        String phonenumber = userDto.getPhonenumber();
        //查看手机号是否唯一
        Integer phoneCount = userDao.findCountPhoneNumber(phonenumber);
        if (phoneCount > 0) {
            throw new RuntimeException("手机号已经存在");
        }
        //查询用户账号是否存在
        Integer usernameCount = userDao.findUserNameCount(userName);
        if (usernameCount > 0) {
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
        if (ToolUtil.isNotEmpty(deptId)) {
            Integer userDept = userDao.insertUserDept(userId, deptId);
        }
        //获取角色
        Long[] roleIds = userDto.getRoleIds();
        //将用户id ,角色id插入到用户角色关联表
        if (ToolUtil.isNotEmpty(roleIds)) {
            Integer userRoleFlag = userDao.insertUserRole(userId, roleIds);
        }
        // 新增用户后立即预热，避免新用户在列表里显示成账号
        warmUpUserCache(userName);
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
        if (ToolUtil.isEmpty(userId)) {
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
            if (userid.equals(aLong)) {
                throw new RuntimeException("不能删除自己账号" + username);
            }
            if (1 == aLong) {
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
        if (1 == userId) {
            //超级管理员只允许修改昵称,部门,手机号,邮箱,性别
            Integer updateAdminFlag = userDao.updateAdmin(userDto);
            //删除用户部门关联表
            Integer deleteUserDeptFlag = userDao.deleteUserDept(userId);
            //添加用户部门关联
            if (ToolUtil.isNotEmpty(userDto.getDeptId())) {
                Integer addUserDeptFlag = userDao.addUserDept(userDto);
            }
            refreshUserCacheById(userId);
            return updateAdminFlag;

        } else {
            //获取登录用户信息
            Map<String, String> userInfo = TokenDecode.getUserInfo();
            //查看用户账号
            String username = userInfo.get("username");
            userDto.setUpdateBy("qq");
            //修改普通用户
            Integer updateCommonUserFlag = userDao.updateCommonUser(userDto);
            //删除用户部门关联表
            Integer deleteUserDeptFlag = userDao.deleteUserDept(userId);
            //添加用户部门关联
            if (ToolUtil.isNotEmpty(userDto.getDeptId())) {
                Integer addUserDeptFlag = userDao.addUserDept(userDto);
            }
            //删除用户角色关联表
            Integer deleteUserRoleFlag = userDao.deleteUserRole(userId);
            //获取角色
            Long[] roleIds = userDto.getRoleIds();
            //添加用户角色关联表
            Integer addUserRoleFlag = userDao.addUserRole(userId, roleIds);
            refreshUserCacheById(userId);
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
    public Integer editPassWord(String username, String newpassWord) {
        //密码加密
        String passWord = new BCryptPasswordEncoder().encode(newpassWord);
        Integer updatePasswordFlag = userDao.editPassWord(username, passWord);
        //获取当前时间
        DateTime dateTime = DateUtil.dateNew(new Date());
        //修改更新时间
        int updateTime = userDao.updateTime(username, dateTime);
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
        while (iterator.hasNext()) {
            String next = iterator.next();
            strRole = strRole + next + ",";
        }
        if (strRole.length() > 0) {
            strRole = strRole.substring(0, strRole.length() - 1);
        }
        return strRole;
    }

    /**
     * 超级管理员可以修改所有人密码
     *
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
        if (ToolUtil.isEmpty(list)) {
            throw new RuntimeException("你无权操作,不允许访问");
        }
        int flag = 0;
        for (int i = 0; i < list.size(); i++) {
            Integer roleId = list.get(i);
            if (roleId == 1) {
                flag = 1;
            }
        }
        if (flag == 0) {
            throw new RuntimeException("你无权操作,不允许访问");
        }
        //密码加密
        String passWord = new BCryptPasswordEncoder().encode(newpassWord);
        Integer adminUpdatePassWordFlag = userDao.adminUpdatePassWord(username, passWord);
        //修改密码后,当前用户的错误次数改为0
        int updateErrorTimes = userDao.updateErrorTimes(username);
        //获取当前时间
        DateTime dateTime = DateUtil.dateNew(new Date());
        //修改更新时间
        int updateTime = userDao.updateTime(username, dateTime);
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
        if (ToolUtil.isEmpty(userId) || ToolUtil.isEmpty(userDto.getStatus())) {
            throw new RuntimeException("参数错误");
        }
        //不允许修改超级管理员角色
        if (userId == 1) {
            throw new RuntimeException("不允许修改超级管理员用户");
        }
        //获取登录用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //查看用户账号
        String username = userInfo.get("username");
        //根据用户账号查询用户的id
        Long userid = userDao.findUserId(username);
        if (userId.equals(userid)) {
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
        //mq发送消息时,不会携带令牌,无法通过令牌获取用户账号
        if (ToolUtil.isEmpty(userDto.getUserName())){
            //获取登录用户信息
            Map<String, String> userInfo = TokenDecode.getUserInfo();
            //获取用户账号
            String username = userInfo.get("username");
            userDto.setUserName(username);
        }
        Integer changeFlag = userDao.changeUserMessage(userDto);
        // 个人资料变更后刷新缓存，保证其他模块立刻看到新昵称/手机号
        warmUpUserCache(userDto.getUserName());
        return changeFlag;
    }

    /**
     * mq修改用户状态
     *
     * @param userDto
     * @return
     */
    @Override
    public Integer mqChangeUserStatus(UserDto userDto) {
        Integer flag = userDao.mqChangeUserStatus(userDto);
        return flag;
    }

    /**
     * mq删除用户
     *
     * @param userNames
     * @param userIds
     * @return
     */
    @Override
    @Transactional
    public Integer mqDeleteUser(List<String> userNames, List<Integer> userIds) {
        Integer updateFlag = userDao.mqDeleteUser(userNames);
        //删除关联表
        Integer deleteUserRoleFlag = userDao.deleteMqUserConnectRole(userIds);
        Integer deleteUserDeptFlag = userDao.deleteMqUserConnectDept(userIds);
        return updateFlag;
    }

    /**
     * Mq修改信息
     *
     * @param userDto
     * @return
     */
    @Override
    public Integer updateUser(UserDto userDto) {
        String updateBy = userDto.getUpdateBy();
        userDto.setUpdateBy(updateBy);
        //修改用户
        Integer updateCommonUserFlag = userDao.mqUpdateUser(userDto);
        return updateCommonUserFlag;
    }

    /**
     * 管理员通过Mq修改用户密码
     *
     * @param usernameAdmin
     * @param newpassWordAdmin
     * @return
     */
    @Override
    public Integer adminMqUpdatePassWord(String usernameAdmin, String newpassWordAdmin) {
        //密码加密
        String passWord = new BCryptPasswordEncoder().encode(newpassWordAdmin);
        Integer adminUpdatePassWordFlag = userDao.adminUpdatePassWord(usernameAdmin, passWord);
        //修改密码后,当前用户的错误次数改为0
        int updateErrorTimes = userDao.updateErrorTimes(usernameAdmin);
        //获取当前时间
        DateTime dateTime = DateUtil.dateNew(new Date());
        //修改更新时间
        int updateTime = userDao.updateTime(usernameAdmin, dateTime);
        return adminUpdatePassWordFlag;
    }

}