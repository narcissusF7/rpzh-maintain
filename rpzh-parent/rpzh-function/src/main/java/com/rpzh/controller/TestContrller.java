package com.rpzh.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.PestMonitoringDao;
import com.rpzh.dto.NodeDto;
import com.rpzh.dto.PestBoardDto;
import com.rpzh.entity.ExcelUtils;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.handler.DataHandler;
import com.rpzh.parse.ParseData;
import com.rpzh.service.DataReportService;
import com.rpzh.service.NodeService;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.SendMessageUtil;
import com.rpzh.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author liubc
 * @create 2022-05-17 13:36
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/maintain/test")
@CrossOrigin
@Api(value = "测试用例类", tags = "测试用例类", description = "测试用例类")
@Slf4j
public class TestContrller{

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    //mq主题
    @Value("${rocketmq.topic}")
    private String topic;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private DataHandler dataHandler;

    @Autowired
    private PestMonitoringDao pestMonitoringDao;

    @Autowired
    DataReportService dataReportService;

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
     * http://localhost:16900/maintain/test/testSendMessage
     * 发送短信测试
     * @return
     */
    @RequestMapping("/testSendMessage")
    public String testSendMessage(){
        String tel = "13155555555";
        String content = "养护系统发送短信测试";
        MessageVo messageVo = SendMessageUtil.sendSms(tel, content, "h1");
        return "执行中";
    }

    /**
     * rocketmq发送数据
     * @return
     * http://localhost:16900/maintain/test/rocketmq 测试路径
     */
    @RequestMapping("/rocketmq")
    public String test(){
        Map map = new HashMap();
        map.put("type", "养护短信类型数据");
        map.put("data", "发送养护短信内容数据");
        map.put("phone", "131898292929");
        //系统标识,标识是哪个系统发送的消息,养护系统发送
        rocketMQTemplate.send(topic + ":" , MessageBuilder.withPayload(map).build()) ;
        return "发送成功";
    }


    /**
     * http://localhost:16900/maintain/test/test2
     * @return
     */
    @RequestMapping("/test2")
    public String test2(String username){
//        redisTemplate.opsForHash().put("USER_"+"rpzh","nickname","超级管理员");
//        redisTemplate.opsForHash().put("USER_"+"rpzh","phonenumber","13156555668");
//        String nickname1 = (String) redisTemplate.opsForHash().get("USER_"+username, "nickname");
//        String phonenumber1 = (String) redisTemplate.opsForHash().get("USER_"+username, "phonenumber");
        Object nickname = redisUtil.hget("USER_" + username, "nickname");
        Object phonenumber = redisUtil.hget("USER_" + username, "phonenumber");
        System.out.println(nickname);
        System.out.println(phonenumber);
        System.out.println("---------------------");
        return "成功";
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
            @ApiImplicitParam(name = "startTime", value = "开始时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", required = false,dataType = "String"),
            @ApiImplicitParam(name = "createTime", value = "创建时间",dataType = "String")
    })
    @ApiOperation(value = "测试用例方法2")
    public Result<NodeVo> two(String nodeId,String nodeType,String createTime,String startTime,String endTime){
        List<NodeVo> list = nodeService.testFindNode(nodeId,nodeType,createTime,startTime,endTime);
        return new Result<NodeVo>(true, StatusCode.OK, "测试方法2",list);
    }

    /**
     *测试用例方法3
     * @param nodeDto
     * @return
     */
    @PostMapping("/three")
    @ApiOperation(value = "测试用例方法3")
    public Result<NodeVo> three(NodeDto nodeDto){
        String nodeId = nodeDto.getNodeId();
        String nodeType = nodeDto.getNodeType();
        String createTime = nodeDto.getCreatedTime();
        String startTime = nodeDto.getStartTime();
        String endTime = nodeDto.getEndTime();
        List<NodeVo> list = nodeService.testFindNode(nodeId,nodeType,createTime,startTime,endTime);
        return new Result<NodeVo>(true, StatusCode.OK, "测试方法3",list);
    }

    /**
     *测试用例方法4
     * @param nodeDto
     * @return
     */
    @PostMapping("/four")
    @ApiOperation(value = "测试用例方法4")
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型,默认:01",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "createdTime()",
            value = "创建时间",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "startTime",
            value = "开始时间",
            dataType = "String"
    ), @ApiImplicitParam(
            name = "endTime",
            value = "结束时间",
            dataType = "Integer"
    )})
    public Result<NodeVo> four(@ApiIgnore NodeDto nodeDto){
        String nodeId = nodeDto.getNodeId();
        String nodeType = nodeDto.getNodeType();
        String createTime = nodeDto.getCreatedTime();
        String startTime = nodeDto.getStartTime();
        String endTime = nodeDto.getEndTime();
        List<NodeVo> list = nodeService.testFindNode(nodeId,nodeType,createTime,startTime,endTime);
        return new Result<>(true, StatusCode.OK, "测试方法4",list);
    }


    @ApiOperation(value = "数据推送测试4")
    @ApiImplicitParams({@ApiImplicitParam(
            name = "nodeId",
            value = "设备id",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "nodeType",
            value = "设备类型",
            required = true,
            dataType = "String"
    ), @ApiImplicitParam(
            name = "Data",
            value = "数据",
            dataType = "String",
            allowMultiple = true
    ), @ApiImplicitParam(
            name = "dateTime",
            value = "传输时间",
            dataType = "String",
            allowMultiple = true
    ), @ApiImplicitParam(
            name = "frequency",
            value = "频率",
            dataType = "Double"
    ), @ApiImplicitParam(
            name = "voltage",
            value = "电压",
            dataType = "Double"
    ), @ApiImplicitParam(
            name = "signalStrength",
            value = "信号强度",
            dataType = "Double"
    ), @ApiImplicitParam(
            name = "packetLossNum",
            value = "丢包个数",
            dataType = "Integer"
    )})
    @PostMapping("testParseData")
    public Result testParseData(@ApiIgnore ParseData parseData){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            list.add(sdf.format(new Date()));
        }
        parseData.setDateTime(list);
        dataHandler.handlerData(parseData);
        return new Result<>(true, StatusCode.OK, "测试方法4");
    }
    @PostMapping("five")
    public Result five(String map){
//        dataHandler.handlerData(parseData);

        JSONObject.parseObject(map,Map.class);
        return new Result<>(true, StatusCode.OK, "测试方法4");
    }

    @PostMapping("six")
    public Result six(){
        Map<String, Object> map = new HashMap<>();
        map.put("username","ljj");
        redisTemplate.opsForHash().put("WAREHOUSE_PRINCIPAL","1_1",map);
        return new Result<>(true, StatusCode.OK, "测试方法4");
    }

    @PostMapping("seven")
    public Result seven(MultipartFile file){
//读取表格中的数据，file 文件，startRow:开始的行数   startCloumn:开始的列数，endCloumn:结束的列数
        List<Map<Integer, String>> nodeDate;
        try {
            nodeDate = ExcelUtils.importExcel(file, 1 , 0, 7);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.ERROR, "导入失败");
        }
        for (int i = 0; i < nodeDate.size(); i++) {
            if (ToolUtil.isEmpty(nodeDate.get(i).get(1))){
                continue;
            }
            ParseData parseData = new ParseData(nodeDate.get(i));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<String> list = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                list.add(sdf.format(new Date()));
            }
            parseData.setDateTime(list);
            dataHandler.handlerData(parseData);
        }

        return new Result<>(true, StatusCode.OK, "测试方法7");
    }
    @PostMapping("addPestBoard")
    public Result addPestBoard(MultipartFile file){
        List<Map<Integer, String>> pestBoards;
        try {
            pestBoards = ExcelUtils.importExcel(file, 1 , 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<>(false, StatusCode.ERROR, "导入失败");
        }
        for (int i = 0; i < pestBoards.size(); i++) {
            if (ToolUtil.isEmpty(pestBoards.get(i).get(0))){
                continue;
            }

            PestBoardDto pestBoardDto = new PestBoardDto();
            pestBoardDto.setPrincipalUser("ljj");
            pestBoardDto.setPrincipalName("刘疆疆");
            pestBoardDto.setSiteRoute("[00:0],[01:13],[02:14],[03:9],[04:11]");
            pestBoardDto.setSiteId(9);
            pestBoardDto.setPestBoardId(pestBoards.get(i).get(0));

            pestMonitoringDao.addPestBoard(pestBoardDto);
//            ParseData parseData = new ParseData(pestBoards.get(i));
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            List<String> list = new ArrayList<>();
//            for (int j = 0; j < 6; j++) {
//                list.add(sdf.format(new Date()));
//            }
//            parseData.setDateTime(list);
//            dataHandler.handlerData(parseData);
        }
        return new Result<>(true, StatusCode.OK, "addPestBoard");
    }

    @GetMapping("export")
    public Result export(String[] pidRoutes, String comparisonType , String dateType, String acquisitionTimeOne, String acquisitionTimeTwo, HttpServletResponse response){
        return dataReportService.exportComparisonData(pidRoutes,comparisonType,dateType,acquisitionTimeOne,acquisitionTimeTwo, response);
    }
}