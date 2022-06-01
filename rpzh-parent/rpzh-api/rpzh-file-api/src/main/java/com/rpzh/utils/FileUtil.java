package com.rpzh.utils;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 文件操作工具类
 * @Author liubc
 * @create 2022-06-01 11:02
 * @Description: good good study,day day up
 */
public class FileUtil {
    /**
     * 加载配置文件
     */
    static {
        try {
            //加载配置文件
            ClassPathResource classPathResource = new ClassPathResource("fdfs_client.conf");
            //加载到全局变量中
            ClientGlobal.init(classPathResource.getPath());
        }catch (Exception e){

        }
    }


    /**
     * 文件上传
     */
    public static String[] upload(FastDFSFile fastDFSFile){

        NameValuePair[] meta_list = new NameValuePair[2];

        meta_list[0] = new NameValuePair("system","智慧养护系统");
        meta_list[1] = new NameValuePair("author",fastDFSFile.getAuthor());

        try {
            //获取trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storange
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //文件上传
            /**
             * 1.文件的字节码
             * 2.文件的拓展名
             * 3.附加参数
             */
            String[] strings = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);

            return strings;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件的下载
     */
    public static InputStream download(String groupName, String fileName){
        try {
            //获取trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storange
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //文件下载
            byte[] bytes = storageClient.download_file(groupName, fileName);
            //返回输入流
            return new ByteArrayInputStream(bytes);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取文件的信息
     */
    public static FileInfo getFileInfo(String groupName, String fileName){
        try {
            //获取trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storange
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //获取文件的信息
            FileInfo file_info = storageClient.get_file_info(groupName, fileName);
            //返回
            return file_info;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件的删除
     */
    public static int deleteFile(String groupName, String fileName){
        try {
            //获取trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获取trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storange
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //获取文件的信息
            int i = storageClient.delete_file(groupName, fileName);
            return i;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }
}