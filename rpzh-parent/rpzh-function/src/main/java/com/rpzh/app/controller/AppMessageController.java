package com.rpzh.app.controller;


import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AppMessageDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.AppMessageService;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.AppMessageVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/05/20/17:34
 * @Description: 功能
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/appMessage")
@Api(value = "APP消息类", tags = "APP消息类", description = "APP消息类")
public class AppMessageController {
    @Resource
    private AppMessageService appMessageService;

    @Resource
    private RedisUtil redisUtil;

    @PostMapping("/insertAppMessage")
    @ApiOperation(value = "发布消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recipientNames", value = "收消息的账号集合", dataType = "List<String>",required = true),
            @ApiImplicitParam(name = "content", value = "消息内容", required = true,dataType = "String"),
            @ApiImplicitParam(name = "messageType", value = "发布消息时:1:点取消---草稿,2:点确认---消息", required = true,dataType = "String")
    })
    public Result insertAppMessage(@ApiIgnore AppMessageDto appMessageDto){
        if(ToolUtil.isEmpty(appMessageDto.getRecipientNames())){
            return new Result(false, StatusCode.ERROR, "接收的用户名不能为空!");
        }
        if(ToolUtil.isEmpty(appMessageDto.getContent())){
            return new Result(false, StatusCode.ERROR, "消息内容不能为空!");
        }
        if(ToolUtil.isEmpty(appMessageDto.getMessageType())){
            return new Result(false, StatusCode.ERROR, "请点击按钮!");
        }
        Integer result = appMessageService.insertAppMessage(appMessageDto);
        if(result==0) {
            return new Result<>(true, StatusCode.OK, "发布消息失败");
        }
        return new Result<>(true, StatusCode.OK, "发布消息成功");
    }

    @PostMapping("findRecipientInfo")
    @ApiOperation(value = "收信箱(分页)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "分页大小", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "page", value = "当前页", required = true, dataType = "Integer")
    })
    public Result<AppMessageVo> findRecipientInfo(@ApiIgnore AppMessageDto appMessageDto){
        String recipientUser = TokenDecode.getUserInfo().get("username");
        appMessageDto.setRecipientName(redisUtil.hget("USER_"+recipientUser,"nickname").toString());
        PageInfo<AppMessageVo> recipientInfos = appMessageService.findRecipientInfo(appMessageDto);
        return new Result<>(true, StatusCode.OK, "查询收信箱成功",recipientInfos);
    }

    @PostMapping("findRecipientInfoById")
    @ApiOperation(value = "收信箱详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "Integer", required = true)
    })
    public Result<AppMessageVo> findRecipientInfoById(Integer id){
        if(ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "id不能为空!");
        }
        AppMessageVo recipientInfo = appMessageService.findRecipientInfoById(id);
        return new Result<>(true, StatusCode.OK, "根据id查询收信箱成功",recipientInfo);
    }

    @PostMapping("findSendInfo")
    @ApiOperation(value = "发信箱(分页)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "分页大小", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "page", value = "当前页", required = true, dataType = "Integer")
    })
    public Result<AppMessageVo> findSendInfo(@ApiIgnore AppMessageDto appMessageDto){
        String sendUser = TokenDecode.getUserInfo().get("username");
        appMessageDto.setSendName(redisUtil.hget("USER_"+sendUser,"nickname").toString());
        PageInfo<AppMessageVo> sendInfos = appMessageService.findSendInfo(appMessageDto);
        return new Result<>(true, StatusCode.OK, "查询收信箱成功",sendInfos);
    }

    @PostMapping("findSendInfoById")
    @ApiOperation(value = "发信箱详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "主键id集合", dataType = "Integer", required = true)
    })
    public Result<AppMessageVo> findSendInfoById(@RequestParam("ids") List<String> ids){
        if(ToolUtil.isEmpty(ids)){
            return new Result(false, StatusCode.ERROR, "id集合不能为空!");
        }
        AppMessageVo sendInfo = appMessageService.findSendInfoById(ids);
        return new Result<>(true, StatusCode.OK, "查询收信箱详情成功",sendInfo);
    }

    @PostMapping("/findDraftInfo")
    @ApiOperation(value = "草稿箱(分页)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "分页大小", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "page", value = "当前页", required = true, dataType = "Integer")
    })
    public Result<AppMessageVo> findDraftInfo(@ApiIgnore AppMessageDto appMessageDto){
        PageInfo<AppMessageVo> draftInfos = appMessageService.findDraftInfo(appMessageDto);
        return new Result<>(true, StatusCode.OK, "查询收信箱成功",draftInfos);
    }

    @PostMapping("/findDraftInfoById")
    @ApiOperation(value = "草稿箱回显")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "主键id集合", dataType = "Integer", required = true)
    })
    public Result<AppMessageVo> findDraftInfoById(@RequestParam("ids") List<String> ids){
        if(ToolUtil.isEmpty(ids)){
            return new Result(false, StatusCode.ERROR, "id集合不能为空!");
        }
        AppMessageVo draftInfo = appMessageService.findDraftInfoById(ids);
        return new Result<>(true, StatusCode.OK, "查询收信箱成功",draftInfo);
    }

    @PostMapping("/updateDraftInfo")
    @ApiOperation(value = "修改草稿箱")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "主键id集合", dataType = "Integer", required = true),
            @ApiImplicitParam(name = "recipientNames", value = "收消息的账号集合", dataType = "List<String>",required = true),
            @ApiImplicitParam(name = "content", value = "消息内容", required = true,dataType = "String"),
            @ApiImplicitParam(name = "messageType", value = "发布消息时:1:点取消---草稿,2:点确认---消息", required = true,dataType = "String")
    })
    public Result<AppMessageVo> updateDraftInfo(@ApiIgnore AppMessageDto appMessageDto){
        if(ToolUtil.isEmpty(appMessageDto.getIds())){
            return new Result(false, StatusCode.ERROR, "id集合不能为空!");
        }
        if(ToolUtil.isEmpty(appMessageDto.getRecipientNames())){
            return new Result(false, StatusCode.ERROR, "接收的用户名不能为空!");
        }
        if(ToolUtil.isEmpty(appMessageDto.getContent())){
            return new Result(false, StatusCode.ERROR, "消息内容不能为空!");
        }
        if(ToolUtil.isEmpty(appMessageDto.getMessageType())){
            return new Result(false, StatusCode.ERROR, "请点击按钮!");
        }
        appMessageService.updateDraftInfo(appMessageDto);
        return new Result<>(true, StatusCode.OK, "修改成功");
    }
}
