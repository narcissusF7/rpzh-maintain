package com.rpzh.controller;

import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.GMWSService;
import com.rpzh.vo.GWMSNodeDataVo;
import com.rpzh.vo.NodeDataVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/maintain/gwms")
@RestController
@CrossOrigin
@Api(value = "gwms控制类", tags = "gwms控制类", description = "gwms控制类")
public class GMWSController {

    @Autowired
    GMWSService gmwsService;
    /**
     * 查询养护环境数据（包芯 氧气 温湿度）
     * @return
     */
    @GetMapping("/findMaintainData")
    public Result<GWMSNodeDataVo> findMaintainData(){
        List<GWMSNodeDataVo> nodeDataVos = gmwsService.findMaintainData();
        return new Result<>(true, StatusCode.OK,"查询成功",nodeDataVos);
    }

    /**
     * 查询可用的烟叶
     * @return
     */
    @GetMapping("/findUsableTobacco")
    public Result<List<Map<String,String>>> findUsableTobacco(){
        List<Map<String,String>> list = gmwsService.findUsableTobacco();
        return new Result<>(true, StatusCode.OK,"查询成功",list);
    }
}
