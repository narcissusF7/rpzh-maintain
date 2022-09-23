package com.rpzh.app.controller;

import com.rpzh.entity.Result;
import com.rpzh.entity.StatusCode;
import com.rpzh.entity.ToolUtil;
import com.rpzh.service.FileService;
import com.rpzh.utils.FastDFSFile;
import com.rpzh.utils.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.csource.fastdfs.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * app任务中心图片管理
 * @Author zyq
 * @create 2022-09-23 11:29
 * @Description: good good study,day day up
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/appFile")
@Api(value = "app任务管理中心中上传图片", tags = "app任务管理中心中上传图片")
public class AppFileController {


    //文件上传的地址
    @Value("${fastdfs.ip}")
    private String ipAddress;

    @Autowired
    private FileService fileService;


    /**
     * 图片文件上传
     * @param file
     * @return
     * @throws IOException
     */
    @ApiOperation(value = "任务处理图片文件上传", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required = true,dataType = "File"),
            @ApiImplicitParam(name = "taskId", value = "任务id", required = true,dataType = "String")
    })
    @PostMapping("/appUpload")
    public Result upload(@ApiIgnore @RequestParam("file") MultipartFile file,@ApiIgnore String taskId) throws IOException {
        if (ToolUtil.isEmpty(taskId)){
            return new Result(false, StatusCode.ERROR, "请选择一个任务");
        }
        String filename = file.getOriginalFilename();
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        if(!("jpg".equals(extension))){
            return new Result(false, StatusCode.ERROR, "图片格式错误");
        }
        //todo 文件格式控制,前端已做
        //封装一个FastDFSFile
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(), //文件名字
                file.getBytes(),            //文件字节数组
                StringUtils.getFilenameExtension(file.getOriginalFilename()));//文件扩展名
        //文件上传
        String[] uploads = FileUtil.upload(fastDFSFile);
        FileInfo fileInfo = FileUtil.getFileInfo(uploads[0], uploads[1]);
        //String sourceIpAddr = fileInfo.getSourceIpAddr();
        //组装文件上传地址,uploads[0]文件组名,uploads[1];文件路径名
        //String path = sourceIpAddr+"/"+uploads[0]+"/"+uploads[1];
        //文件存放路径
        String path = ipAddress+"/" + uploads[0]+"/"+uploads[1];
        //图片组名
        String group = uploads[0];
        //图片文件名
        String fileName = uploads[1];
        //用于返回前端数据
        Map<String,String> mapPic = new HashMap<>();
        mapPic.put("group", group);
        mapPic.put("fileName", fileName);
        int insertFlag = fileService.insertFilePictureUrl(path,group,fileName,taskId);
        return new Result(true, StatusCode.OK, "文件上传成功",mapPic);
    }

    /**
     * 文件删除
     * @param groupName
     * @param fileName
     * @return
     */
    @ApiOperation(value = "图片删除", tags = "任务管理中心")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "组名", required = true,dataType = "String"),
            @ApiImplicitParam(name = "fileName", value = "文件名", required = true,dataType = "String")
    })
    @PostMapping("/appDelete")
    public Result delete(@ApiIgnore String groupName,@ApiIgnore String fileName){
        if (ToolUtil.isEmpty(groupName) || ToolUtil.isEmpty(fileName)){
            return new Result(false, StatusCode.ERROR, "组名和文件名不能为空");
        }
        //删除服务器中的图片
        int i = FileUtil.deleteFile(groupName, fileName);
        //删除数据库中的路径
        int delFlag = fileService.deletePic(groupName,fileName);
        return new Result(true, StatusCode.OK, "删除成功", i);
    }

    /**
     * 获取文件信息
     * @param groupName
     * @param fileName
     * @return
     */
    @ApiOperation(value = "查看图片信息", tags = "任务管理中心")
    @PostMapping("/getAppFileInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "组名", required = true,dataType = "String"),
            @ApiImplicitParam(name = "fileName", value = "文件名", required = true,dataType = "String")
    })
    public Result getFileInfo(@ApiIgnore String groupName,@ApiIgnore String fileName){
        if (ToolUtil.isEmpty(groupName) || ToolUtil.isEmpty(fileName)){
            return new Result(false, StatusCode.ERROR, "组名和文件名不能为空");
        }
        FileInfo fileInfo = FileUtil.getFileInfo(groupName, fileName);
        return new Result(true, StatusCode.OK, "查询文件信息成功", fileInfo);
    }

}