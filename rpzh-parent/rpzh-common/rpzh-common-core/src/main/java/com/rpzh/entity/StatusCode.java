package com.rpzh.entity;

/**
 * 返回结果集状态码
 * @Author liubc
 * @create 2021/3/29 15:32
 */
public class StatusCode {
    public static final int OK = 200;//成功返回
    public static final int ERROR = 20001;//失败
    public static final int LOGINERROR = 20002;//用户名或密码错误
    public static final int ACCESSERROR = 20003;//权限不足
    public static final int REMOTEERROR = 20004;//远程调用失败
    public static final int REPERROR = 20005;//重复操作
    public static final int NOTFOUNDERROR = 20006;//响应失败
}