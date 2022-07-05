package com.rpzh.service;

/**
 * 任务中心图片管理
 * @Author liubc
 * @create 2022-06-01 16:04
 * @Description: good good study,day day up
 */
public interface FileService {
    /**
     * 上传图片
     * @param path
     * @param group
     * @param fileName
     * @param id
     * @return
     */
    int insertFilePictureUrl(String path, String group, String fileName, String id);

    /**
     * 删除数据库中的地址
     * @param groupName
     * @param fileName
     * @return
     */
    int deletePic(String groupName, String fileName);
}
