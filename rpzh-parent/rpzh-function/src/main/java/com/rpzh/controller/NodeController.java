package com.rpzh.controller;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.ExcelUtils;
import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.NodeService;
import com.rpzh.vo.NodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.stream.IIOByteBuffer;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 节点管理
 *
 * @Author liubc
 * @create 2022/3/3 9:39
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/node")
@CrossOrigin
public class NodeController {


    @Autowired
    private NodeService nodeService;

    /**
     * 添加节点
     *
     * @param nodeDto
     * @return
     */
    @PostMapping("/addNode")
    public Result addNode(NodeDto nodeDto) {
        String nodeId = nodeDto.getNodeId();
        if (ToolUtil.isEmpty(nodeId)) {
            return new Result(false, StatusCode.ERROR, "节点Id不能为空");
        }
        //判断设备id是否存在
        int nodeCount = nodeService.findNodeCountById(nodeId);
        if (nodeCount > 0) {
            return new Result(false, StatusCode.ERROR, "该设备已经存在,禁止添加");
        }
        int addFlag = nodeService.addNode(nodeDto);
        return new Result(true, StatusCode.OK, "添加节点成功");
    }

    /**
     * 更新设备
     *
     * @param nodeDto
     * @return
     */
    @PostMapping("/updateNode")
    public Result updateNode(NodeDto nodeDto) {
        String nodeId = nodeDto.getNodeId();
        if (ToolUtil.isEmpty(nodeId)) {
            return new Result(false, StatusCode.ERROR, "节点Id不能为空");
        }
        Integer id = nodeDto.getId();
        if (ToolUtil.isEmpty(id)) {
            return new Result(false, StatusCode.ERROR, "请选择一个设备");
        }
        String nodeIdFind = nodeService.findNodeID(nodeId, id);
        if (nodeId.equals(nodeIdFind)) {
            return new Result(false, StatusCode.ERROR, "节点id已经存在,不允许修改");
        }
        int updateFlag = nodeService.updateNode(nodeDto);
        return new Result(true, StatusCode.OK, "修改设备成功");
    }


    /**
     * 查询节点列表
     *
     * @param nodeDto
     * @return
     */
    @PostMapping("/findNode")
    public Result<PageInfo> findNode(NodeDto nodeDto) {
        PageInfo<NodeVo> pageInfo = nodeService.findAllNodeList(nodeDto);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /**
     * 删除节点
     *
     * @param
     * @return
     */
    @PostMapping("/deleteNode")
    public Result deleteNode(NodeDto nodeDto) {
        List<Integer> ids = nodeDto.getIds();
        if (ToolUtil.isEmpty(ids)) {
            return new Result(false, StatusCode.ERROR, "请选择一个节点");
        }
        int delFlag = nodeService.deleteNode(ids);
        return new Result(true, StatusCode.OK, "删除节点成功");
    }


    /**
     * 设备id替换,历史数据模块完成以后,需要完善历史模块的设备id替换
     *
     * @param nodeDto
     * @return
     */
    @PostMapping("/replaceNodeId")
    public Result replaceNodeId(NodeDto nodeDto) {
        String beforeNodeId = nodeDto.getBeforeNodeId();
        if (ToolUtil.isEmpty(beforeNodeId)) {
            return new Result(false, StatusCode.ERROR, "请选择需要替换的设备id");
        }
        String afterNodeId = nodeDto.getAfterNodeId();
        if (ToolUtil.isEmpty(afterNodeId)) {
            return new Result(false, StatusCode.ERROR, "请输入替换之后的设备id");
        }
        String gatherSite = nodeDto.getGatherSite();
        if (ToolUtil.isEmpty(gatherSite)) {
            return new Result(false, StatusCode.ERROR, "采集位置不能为空");
        }
        int updateFlag = nodeService.replaceNodeId(nodeDto);
        return new Result(true, StatusCode.OK, "设备替换成功");
    }


    /**
     * 设备替换日志
     *
     * @param nodeDto
     * @return
     */
    @PostMapping("/findNodeReplaceLog")
    public Result<PageInfo> findNodeReplaceLog(NodeDto nodeDto) {
        String nodeId = nodeDto.getNodeId();
        if (ToolUtil.isEmpty(nodeId)) {
            return new Result(false, StatusCode.ERROR, "设备id不能为空");
        }
        PageInfo<NodeVo> pageInfo = nodeService.findNodeReplaceLog(nodeDto);
        return new Result(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /**
     * 查询设备未入垛列表
     *
     * @param nodeDto
     * @return
     */
    @PostMapping("/findNoEnterStack")
    public Result<PageInfo> findNoEnterStack(NodeDto nodeDto) {
        PageInfo<NodeVo> pageInfo = nodeService.findNoEnterStack(nodeDto);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /**
     * 设备入垛
     *
     * @param
     * @return
     */
    @PostMapping("/enterStack")
    public Result enterStack(String data) {
        if (ToolUtil.isEmpty(data)) {
            return new Result(false, StatusCode.ERROR, "请选择一个设备");
        }
        int updateFlag = nodeService.enterStack(data);
        return new Result(true, StatusCode.OK, "设备入垛成功");
    }


    /**
     * 设备解垛
     *
     * @param nodeDto
     * @return
     */
    @PostMapping("/unbindStack")
    public Result unbindStack(NodeDto nodeDto) {
        Integer id = nodeDto.getId();
        if (ToolUtil.isEmpty(id)) {
            return new Result(false, StatusCode.ERROR, "请选择一个设备");
        }
        int updateFlag = nodeService.unbindStack(nodeDto);
        return new Result(true, StatusCode.OK, "解垛成功");
    }


    /**
     * 批量导入设备
     *
     * @param file
     * @param nodeType
     * @return
     */
    @PostMapping("/exportNode")
    public Result exportNode(MultipartFile file, String nodeType) {
        if (ToolUtil.isEmpty(nodeType)){
            return new Result(false, StatusCode.ERROR, "节点类型不能为空");
        }
        if (ToolUtil.isEmpty(file)){
            return new Result(false, StatusCode.ERROR, "请选择一个文件");
        }
        //读取表格中的数据，file 文件，startRow:开始的行数   startCloumn:开始的列数，endCloumn:结束的列数
        List<Map<Integer, String>> nodeDate = null;
        try {
            nodeDate = ExcelUtils.importExcel(file, 2, 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "导入失败");
        }
        Result result = nodeService.exportNode(nodeDate,nodeType);
        return result;
    }


    /**
     * 设备导入模板下载
     *
     * @param response
     * @return
     */
    @RequestMapping("/downloadTemplate")
    public Result downloadTemplate(HttpServletResponse response) {
        String downloadFilePath = "/template/node/node.xlsx";//被下载的文件在服务器中的路径,
        String fileName = "设备导入模板";//被下载文件的名称
        InputStream inputStream = this.getClass().getResourceAsStream(downloadFilePath);
        ExcelUtils.outFile(inputStream, response, fileName, "xlsx");
        return null;
    }

}