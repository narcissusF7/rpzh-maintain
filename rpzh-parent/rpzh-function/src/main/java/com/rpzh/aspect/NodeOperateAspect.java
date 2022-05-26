package com.rpzh.aspect;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.dto.NodeOperationDto;
import com.rpzh.entity.Result;
import com.rpzh.enums.NodeOperationEnum;
import com.rpzh.enums.NodeTypeEnum;
import com.rpzh.service.NodeOperationService;
import com.rpzh.util.StringUtils;
import com.rpzh.util.TokenDecode;
import org.apache.poi.ss.formula.functions.T;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component
@Aspect
public class NodeOperateAspect {

    @Autowired
    NodeOperationService operationService;
    @Before(value = "execution(* com.rpzh.controller.NodeController.*(..))||execution(* com.rpzh.controller.MaintainPlanController..*(..)))")
    public  void NodeOperateLog(JoinPoint pjp){
        System.out.println("进来了");
    }

    @AfterReturning(value = "execution(* com.rpzh.controller.NodeController.*(..))||execution(* com.rpzh.controller.MaintainPlanController..*(..)))", returning = "result")
    public void NodeOperateLog(JoinPoint pjp, Result<T> result){

        if (result.isFlag()){

            RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes sra = (ServletRequestAttributes) ra;
            assert sra != null;
            HttpServletRequest request = sra.getRequest();
            String url = request.getRequestURL().toString();

            //获取执行的Controller层的方法
            String type;
            if (url.contains("/node/")){
                type =url.split("/node/")[1];
            }else {
                type =url.split("/maintainPlan/")[1];
            }


            Object[] args = pjp.getArgs();

            NodeOperationDto nodeOperationDto = new NodeOperationDto();

            nodeOperationDto.setOperationType(NodeOperationEnum.getTypeNum(type));

            //获取用户信息
            Map<String, String> userInfo = TokenDecode.getUserInfo();
            //获取用户账号
            String username = userInfo.get("username");

            NodeDto nodeDto;

            List<NodeDto> nodeDtos;

            List<NodeOperationDto> nodeOperationDtos = new ArrayList<>();

        /*
          判断是否是枚举中的类型 不是的话退出切面
          1.添加 2.删除 3.修改 4.替换 5.入垛（批量） 6.解垛 7.翻垛 8.移垛
         */
            switch (NodeOperationEnum.getTypeNum(type)){

                case NodeConstant.ADD_NODE_OPERATE_NUMBER:
                    nodeDto =(NodeDto) args[0];
                    setValue(nodeOperationDto,nodeDto,"设备"+nodeDto.getNodeId() +"添加成功",username,nodeOperationDtos);
                    break;
                case NodeConstant.DELETE_NODE_OPERATE_NUMBER:
                    String[] nodeIds =(String[]) args[0];
                    StringBuffer detail =new StringBuffer("设备");
                    Arrays.stream(nodeIds).forEach(nodeId-> detail.append(nodeId).append("  "));
                    detail.append("被删除");
                    setValue(nodeOperationDto,new NodeDto(),detail.toString(),username,nodeOperationDtos);
                    break;
                case NodeConstant.UPDATE_NODE_OPERATE_NUMBER:
                    nodeDto =(NodeDto) args[0];
                    setValue(nodeOperationDto,nodeDto,"设备"+nodeDto.getNodeId() +"被修改",username,nodeOperationDtos);
                    break;
                case NodeConstant.REPLACE_NODE_OPERATE_NUMBER:
                    nodeDto =(NodeDto) args[0];
                    setValue(nodeOperationDto,nodeDto,"设备"+nodeDto.getNodeId() +"被设备"+nodeDto.getAfterNodeId()+"替换",username,nodeOperationDtos);
                    break;
                case NodeConstant.ENTER_STACK_NODE_OPERATE_NUMBER:

                    List<NodeDto> mapList = new ArrayList<>();
                    String object =(String)(args[0]);
                    List<Map> list = JSONObject.parseObject(object, List.class);
                    if (list.size()>0){
                            list.forEach(map-> mapList.add( JSONObject.parseObject(JSONObject.toJSONString(map), NodeDto.class)));
                    }

                    nodeDtos =mapList;
                    nodeDtos.forEach(nodeDto1 ->
                            setValue(nodeOperationDto,nodeDto1,"设备"+nodeDto1.getNodeId() +"入垛至"+nodeDto1.getStack(),username,nodeOperationDtos)
                    );
                    break;
                case NodeConstant.UNBIND_STACK_NODE_OPERATE_NUMBER:
                    nodeDto =(NodeDto) args[0];
                    setValue(nodeOperationDto,nodeDto,"设备"+nodeDto.getNodeId() +"从"+nodeDto.getStack()+"解垛",username,nodeOperationDtos);
                    break;
                case NodeConstant.FLIP_STACK_NODE_OPERATE_NUMBER:
                    nodeDtos = (List<NodeDto>) args[0];
                    nodeDtos.forEach(nodeDto1 ->
                            setValue(nodeOperationDto,nodeDto1,
                                    "垛位"+nodeDto1.getStack() +"翻垛，设备"+nodeDto1.getNodeId()+"移动到"+nodeDto1.getSiteX()+","+nodeDto1.getSiteY()+","+nodeDto1.getSiteZ(),username,nodeOperationDtos)
                    );
                    break;
                case NodeConstant.MOVE_STACK_NODE_OPERATE_NUMBER:
                    MaintainPlanDto maintainPlanDto = (MaintainPlanDto) args[0];
                    nodeDtos = maintainPlanDto.getNodeDtoList();
                    nodeDtos.forEach(nodeDto1 ->
                            setValue(nodeOperationDto,nodeDto1,
                                    "养护计划"+maintainPlanDto.getId() +"移垛至垛位"+maintainPlanDto.getStackId()+"，设备"+nodeDto1.getNodeId()+"移动到"+nodeDto1.getSiteX()+","+nodeDto1.getSiteY()+","+nodeDto1.getSiteZ(),username,nodeOperationDtos)
                    );

                default:
                    break;

            }
            //判断
            if (nodeOperationDtos.size()>0){
                operationService.insertNodeOperation(nodeOperationDtos);
            }
        }



    }

    /**
     *  给nodeOperationDto赋值
     * @param nodeOperationDto 该操作信息
     * @param nodeDto 当前这个设备信息
     * @param username 操作人员
     * @param detail  操作详情
     */
    private void setValue(NodeOperationDto nodeOperationDto,NodeDto nodeDto,String detail,String username,List<NodeOperationDto>nodeOperationDtos){

        if (!StringUtils.isEmpty(nodeDto.getNodeId())){
            nodeOperationDto.setNodeId(nodeDto.getNodeId());
        }
        if (nodeDto.getSiteId()!=null){
            nodeOperationDto.setSiteId(nodeDto.getSiteId());
        }
        if (nodeDto.getGatherSite()!=null){
            nodeOperationDto.setGatherSite(nodeDto.getGatherSite());
        }
        if (nodeDto.getSiteX()!=null){
            nodeOperationDto.setSiteX(nodeDto.getSiteX());
        }
        if (nodeDto.getSiteY()!=null){
            nodeOperationDto.setSiteY(nodeDto.getSiteY());
        }
        if (nodeDto.getSiteZ()!=null){
            nodeOperationDto.setSiteZ(nodeDto.getSiteZ());
        }

        if (nodeDto.getNodeType()!=null){
            nodeOperationDto.setNodeType(nodeDto.getNodeType());
        }
        if (!StringUtils.isEmpty(detail)){
            nodeOperationDto.setOperationDetails(detail);
        }
        if (!StringUtils.isEmpty(username)){
            nodeOperationDto.setCreatedUser(username);
        }

        nodeOperationDtos.add(nodeOperationDto);

    }
}
