package com.rpzh.controller;
import com.rpzh.dto.DataContrastDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.DataContrastService;
import io.swagger.annotations.*;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * @Author: zyq
 * @Date: 2022/06/23/13:32
 * @Description: 数据对比
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/dataContrast")
@Api(value = "数据对比类", tags = "数据对比类", description = "数据对比类")
public class DataContrastController {
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(true);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
    @Resource
    private DataContrastService dataContrastService;
    @PostMapping("/getNodeData")
    @ApiOperation(value = "数据对比中的条件选择设备")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "materialCode", value = "物料等级", required = false, dataType = "String"),
            @ApiImplicitParam(name = "siteRoute", value = "位置路由", required = false, dataType = "int"),
            @ApiImplicitParam(name = "stackId", value = "位置id", required = false, dataType = "int"),
            @ApiImplicitParam(name = "nodeType", value = "设备类型 01:氧气 02:包芯温度 03:环境温度 04:环境湿度 05:垛内平均包芯温度 06:仓间平均氧气 07:仓间平均温度 08:仓间平均湿度", required = true, dataType = "String"),
            @ApiImplicitParam(name = "contrastType", value = "对比的类型:01:设备  02:位置", required = true, dataType = "int")
    })
    public Result getNodeData(@ApiIgnore NodeDto nodeDto) {
        return dataContrastService.getNodeData(nodeDto);
    }

    @PostMapping("/getNodeDataList")
    @ApiOperation(value = "数据对比")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dataContrasts", value = "所选的设备信息(json):siteName、nodeId、nodeName、nodeType、colorGba", required = true, dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "采集的结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "beginTime", value = "对比的开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "finishTime", value = "对比的结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(name = "gatherType", value = "时间类型 1:天  2:周  3:月  4:年    默认月", required = false, dataType = "String")
    })
    public Result getNodeDateList(String dataContrasts, String gatherType, Date startTime,Date endTime,Date beginTime,Date finishTime){
        return dataContrastService.getNodeDateList(dataContrasts,gatherType,startTime,endTime,beginTime,finishTime);
    }

}
