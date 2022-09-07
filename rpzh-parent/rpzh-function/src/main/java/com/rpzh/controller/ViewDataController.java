package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.ViewDataDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.service.ViewDataService;
import com.rpzh.vo.ViewDataVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对接db2视图,从视图中取数据
 * @Author liubc
 * @create 2022-09-07 14:20
 * @Description: good good study,day day up                    /maintain/view/vmMaintainInventorybatch
 */
@RestController
@RequestMapping("/maintain/view")
@CrossOrigin
@Api(value = "视图取数据", tags = "视图取数据")
public class ViewDataController {


    @Autowired
    private ViewDataService viewDataService;



    /**
     * 查询实时库存条码信息
     * @param viewDataDto  PageInfo<TaskVo>
     * @return
     */
    @PostMapping("/vmMaintainInventorybatch")
    public Result<PageInfo<ViewDataVo>> vmMaintainInventorybatch(ViewDataDto viewDataDto){
        PageInfo<ViewDataVo> pageInfo = viewDataService.vmMaintainInventorybatch(viewDataDto);
        return new Result<>(true, StatusCode.OK, "查询成功", pageInfo);
    }




}