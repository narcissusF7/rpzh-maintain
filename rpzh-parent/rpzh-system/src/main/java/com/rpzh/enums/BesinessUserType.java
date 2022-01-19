package com.rpzh.enums;

/**
 * 用户操作类型
 * @Author liubc
 * @create 2022/1/17 16:41
 * @Description: good good study,day day up
 */
public enum BesinessUserType {
    /**
     * 其它,0
     */
    OTHER,

    /**
     * 新增,1
     */
    INSERT,

    /**
     * 修改,2
     */
    UPDATE,


    /**
     * 修改用户状态,3
     */
    UPDATESTATUS,

    /**
     * 删除,4
     */
    DELETE,

    /**
     * 修改密码,5
     */
    PASSWORD,

    /**
     * 管理员修改密码,6
     */
    ADMINPASSWORD,


    /**
     * 个人修改信息,7
     */
    PERSONALUPDATE
}
