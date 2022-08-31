package com.rpzh.aspect;

import com.alibaba.fastjson.JSONObject;
import com.rpzh.constant.NodeConstant;
import com.rpzh.dao.NodeDao;
import com.rpzh.dto.MaintainPlanDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.dto.NodeOperationDto;
import com.rpzh.entity.Result;
import com.rpzh.entity.ToolUtil;
import com.rpzh.enums.NodeOperationEnum;
import com.rpzh.handler.MaintainDataHandler;
import com.rpzh.service.NodeOperationService;
import com.rpzh.util.StringUtils;
import com.rpzh.util.TokenDecode;
import org.apache.poi.ss.formula.functions.T;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 节点操作切面
 *
 * @Author liujj
 * @create 2022/4/30
 */
@Component
@Aspect
public class NodeOperateAspect {

    @Autowired
    NodeOperationService operationService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    NodeDao nodeDao;


    /**
     * 设备解垛 前置通知
     * @param pjp
     */
    @Before(value = "execution(* com.rpzh.controller.NodeController.unbindStack())")
    public void NodeUnBingOperateLog(JoinPoint pjp){
        Object[] args = pjp.getArgs();
        NodeDto nodeDto =(NodeDto) args[0];

        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户账号
        String username = userInfo.get("username");

        List<NodeOperationDto> nodeOperationDtos = new ArrayList<>();

        List<NodeDto> nodeDtos1 =  nodeDao.findNodeVos(nodeDto.getIds());
        for (int i = 0; i < nodeDtos1.size(); i++) {
            setValue("unbindStack",nodeDtos1.get(i),"设备"+nodeDtos1.get(i).getNodeId() +"从"+nodeDtos1.get(i).getGatherSite()+"解垛",username,nodeOperationDtos);
            redisTemplate.delete("MAINTAIN_CACHE_NODE_"+nodeDtos1.get(i).getNodeId());
            redisTemplate.delete("MAINTAIN_"+nodeDtos1.get(i).getNodeId());
            redisTemplate.delete("RPZH_MAINTAIN_FCNT_"+nodeDtos1.get(i).getNodeId());

        }

        if (nodeOperationDtos.size()>0){
            operationService.insertNodeOperation(nodeOperationDtos);
        }

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

            //获取用户信息
            Map<String, String> userInfo = TokenDecode.getUserInfo();
            //获取用户账号
            String username = userInfo.get("username");

            NodeDto nodeDto;

            List<NodeDto> nodeDtos;

            MaintainPlanDto maintainPlanDto;

            List<String> nodeDtoList;

            List<NodeOperationDto> nodeOperationDtos = new ArrayList<>();

        /*
          判断是否是枚举中的类型 不是的话退出切面
          1.添加 2.删除 3.修改 4.替换 5.入垛（批量） 6.解垛 7.翻垛 8.移垛
          //TODO 出库报警
         */
            switch (NodeOperationEnum.getTypeNum(type)){

                case NodeConstant.ADD_NODE_OPERATE_NUMBER:
                    nodeDto =(NodeDto) args[0];
                    setValue(type,nodeDto,"设备"+nodeDto.getNodeId() +"添加成功",username,nodeOperationDtos);
                    break;
//                case NodeConstant.DELETE_NODE_OPERATE_NUMBER:
//                    String[] nodeIds =(String[]) args[0];
//                    StringBuffer detail =new StringBuffer("设备");
//                    Arrays.stream(nodeIds).forEach(nodeId-> detail.append(nodeId).append("  "));
//                    detail.append("被删除");
//                    setValue(type,new NodeDto(),detail.toString(),username,nodeOperationDtos);
//                    break;
                case NodeConstant.UPDATE_NODE_OPERATE_NUMBER:
                    nodeDto =(NodeDto) args[0];
                    setValue(type,nodeDto,"设备"+nodeDto.getNodeId() +"被修改",username,nodeOperationDtos);
                    break;
                case NodeConstant.REPLACE_NODE_OPERATE_NUMBER:
                    nodeDto =(NodeDto) args[0];
                    String nodeId = nodeDto.getNodeId();
                    nodeDto.setNodeId(nodeDto.getAfterNodeId());
                    setValue(type,nodeDto,"设备"+nodeId +"被设备"+nodeDto.getAfterNodeId()+"替换",username,nodeOperationDtos);
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
                            setValue(type,nodeDto1,"设备"+nodeDto1.getNodeId() +"入垛至"+nodeDto1.getStack(),username,nodeOperationDtos)
                    );
                    break;
//                case NodeConstant.UNBIND_STACK_NODE_OPERATE_NUMBER:
//                    nodeDto =(NodeDto) args[0];
//                    List<NodeDto> nodeDtos1 =  nodeDao.findNodeVos(nodeDto.getIds());
//                    for (int i = 0; i < nodeDtos1.size(); i++) {
//                        setValue(type,nodeDtos1.get(i),"设备"+nodeDtos1.get(i).getNodeId() +"从"+nodeDtos1.get(i).getGatherSite()+"解垛",username,nodeOperationDtos);
//                    }
//
//                    break;
                case NodeConstant.FLIP_STACK_NODE_OPERATE_NUMBER:
                    maintainPlanDto = (MaintainPlanDto) args[0];
                    nodeDtoList = maintainPlanDto.getNodeDtoList();
                    nodeDtoList.forEach(nodeDtostr ->{
                        NodeDto nodeDto1 = JSONObject.parseObject(nodeDtostr,NodeDto.class);
                        setValue(type,nodeDto1,
                                "垛位"+nodeDto1.getStack() +"翻垛，设备"+nodeDto1.getNodeId()+"移动到"+nodeDto1.getSiteX()+","+nodeDto1.getSiteY()+","+nodeDto1.getSiteZ(),username,nodeOperationDtos);
                    });

                    break;
                case NodeConstant.MOVE_STACK_NODE_OPERATE_NUMBER:
                    maintainPlanDto = (MaintainPlanDto) args[0];
                    nodeDtoList = maintainPlanDto.getNodeDtoList();
                    nodeDtoList.forEach(nodeDtostr ->{
                        NodeDto nodeDto1 = JSONObject.parseObject(nodeDtostr,NodeDto.class);
                        setValue(type,nodeDto1,
                                "养护计划"+maintainPlanDto.getId() +"移垛至垛位"+maintainPlanDto.getStackId()+"，设备"+nodeDto1.getNodeId()+"移动到"+nodeDto1.getSiteX()+","+nodeDto1.getSiteY()+","+nodeDto1.getSiteZ(),username,nodeOperationDtos);
                    });
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
     * @param type 该操作类型
     * @param nodeDto 当前这个设备信息
     * @param username 操作人员
     * @param detail  操作详情
     */
    private void setValue(String type,NodeDto nodeDto,String detail,String username,List<NodeOperationDto>nodeOperationDtos){
        NodeOperationDto nodeOperationDto = new NodeOperationDto();
        nodeOperationDto.setOperationType(NodeOperationEnum.getTypeNum(type));
        //移除缓存中的设备数据
        if (!StringUtils.isEmpty(nodeDto.getNodeId())){
            nodeOperationDto.setNodeId(nodeDto.getNodeId());
            redisTemplate.delete(MaintainDataHandler.MAINTAIN_CACHE_NODE+nodeDto.getNodeId());
        }else if (ToolUtil.isNotEmpty(nodeDto.getIds())){

            List<String> nodeIds = nodeDao.findNodeIdByIds(nodeDto.getIds());
            for (int i = 0; i <nodeIds.size() ; i++) {
                redisTemplate.delete(MaintainDataHandler.MAINTAIN_CACHE_NODE+nodeIds.get(i));
            }
        }else if (ToolUtil.isNotEmpty(nodeDto.getId())){
            Integer[] ids  = {nodeDto.getId()};
            List<String> nodeIds = nodeDao.findNodeIdByIds(ids);
            for (int i = 0; i <nodeIds.size() ; i++) {
                redisTemplate.delete(MaintainDataHandler.MAINTAIN_CACHE_NODE+nodeIds.get(i));
            }
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
