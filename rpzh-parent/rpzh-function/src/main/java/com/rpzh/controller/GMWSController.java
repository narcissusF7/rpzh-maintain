package com.rpzh.controller;

import com.rpzh.entity.Result;
import com.rpzh.service.GMWSService;
import com.rpzh.vo.NodeDataVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class GMWSController {

    @Autowired
    GMWSService gmwsService;
    /**
     * 查询养护环境数据（包芯 氧气 温湿度）
     * @return
     */
    public Result findMaintainData(){
        List<NodeDataVo> nodeDataVos = gmwsService.findMaintainData();
        return null;
    }
}
