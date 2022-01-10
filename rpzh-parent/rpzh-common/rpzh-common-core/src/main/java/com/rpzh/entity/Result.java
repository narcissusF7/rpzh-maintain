package com.rpzh.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 返回结果集实体类
 * @Author liubc成
 * @create 2021/3/29 15:31
 */
@ApiModel(description = "通用返回响应结果对象", value = "result")
public class Result<T> {
    @ApiModelProperty(notes = "是否成功,flase:失败,true:成功", required = true)
    private boolean flag;//是否成功
    @ApiModelProperty(notes = "响应结果的错误信息状态码,200表示成功,20001表示错误", required = true)
    private Integer code;//返回码
    @ApiModelProperty(notes = "返回消息", required = true)
    private String message;//返回消息
    @ApiModelProperty(notes = "返回数据", required = true)
    private T data;//返回数据

    public Result(boolean flag, Integer code, String message, Object data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = (T) data;
    }

    public Result(boolean flag, Integer code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    public Result() {
        this.flag = true;
        this.code = StatusCode.OK;
        this.message = "操作成功!";
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
