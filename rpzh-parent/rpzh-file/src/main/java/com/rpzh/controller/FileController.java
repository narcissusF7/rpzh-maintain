package com.rpzh.controller;

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

import java.io.*;

/**
 * 任务中心图片管理
 * @Author liubc
 * @create 2022-06-01 11:29
 * @Description: good good study,day day up
 */
@RestController
@CrossOrigin
@RequestMapping("/maintain/file")
@Api(value = "任务管理中心", tags = "任务管理中心")
public class FileController {


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
            @ApiImplicitParam(name = "id", value = "任务id", required = true,dataType = "String")
    })
    @PostMapping("/upload")
    public Result upload(@ApiIgnore @RequestParam("file") MultipartFile file,@ApiIgnore String id) throws IOException {
        if (ToolUtil.isEmpty(id)){
            return new Result(false, StatusCode.ERROR, "请选择一个任务");
        }
        //todo 文件格式控制
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
        int insertFlag = fileService.insertFilePictureUrl(path,group,fileName,id);
        return new Result(true, StatusCode.OK, "文件上传成功");
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
    @PostMapping("/delete")
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
    @PostMapping("/getFileInfo")
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


    /**
     * 文件下载
     * @return
     * @throws IOException
     */
//    @GetMapping("/dowload")
//    public String dowload() throws IOException {
//        String groupName = "group1";
//        String fileName = "M00/00/00/rBIHX2KUy2OAI3QrAABPLMRw4Hw55.jpeg";
//        //下载文件获取输入流
//        InputStream is = FileUtil.download(groupName, fileName);
//        //定义一个输出流
//        File file = new File("d:/123.jpg");
//        OutputStream os = new FileOutputStream(file);
//        byte[] buffer = new byte[1024];
//        //读文件
//        while (is.read(buffer) != -1){
//            os.write(buffer);
//        }
//        //释放资源
//        os.close();
//        is.close();
//        return "发送成功";
//    }
}