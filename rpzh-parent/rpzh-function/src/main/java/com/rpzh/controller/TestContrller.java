package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.NodeService;
import com.rpzh.vo.NodeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liubc
 * @create 2022-05-17 13:36
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/maintain/test")
@CrossOrigin
@Api(value = "测试用例类", tags = "测试用例类", description = "测试用例类")
public class TestContrller {


    @Autowired
    private NodeService nodeService;

    @ApiOperation(value = "测试用例方法", tags = "测试用例方法")
    @PostMapping("/one")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeType", value = "设备类型,默认:01", required = true,dataType = "String"),
            @ApiImplicitParam(name = "mainStre", value = "养护阶段", required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "pelpcileId", value = "负责人ID", required = true,dataType = "Integer"),
            @ApiImplicitParam(name = "jwt", value = "令牌",paramType = "请求头",dataType = "String"),
            @ApiImplicitParam(name = "stack", value = "垛位",dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间",dataType = "String")
    })
    public Result testOne(NodeDto nodeDto){
        PageInfo<NodeVo> pageInfo = nodeService.findAllNodeList(nodeDto);
        return new Result(true, StatusCode.OK, "发送成功",pageInfo);
    }

    /**
     * 参考这个方法
     * @param nodeId
     * @param nodeType
     * @param createTime
     * @param startTime
     * @param endTime
     * @return
     */
    @PostMapping("/two")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nodeId", value = "设备id", required = true,dataType = "String"),
            @ApiImplicitParam(name = "nodeType", value = "设备类型", required = true,dataType = "String"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = true,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = true,dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间",dataType = "String")
    })
    @ApiOperation(value = "测试用例方法2")
    public Result<NodeVo> two(String nodeId,String nodeType,String createTime,String startTime,String endTime){
        List<NodeVo> list = nodeService.testFindNode(nodeId,nodeType,createTime);
        return new Result<NodeVo>(true, StatusCode.OK, "测试方法2",list);
    }
}