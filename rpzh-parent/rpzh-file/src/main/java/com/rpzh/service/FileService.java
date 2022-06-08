package com.rpzh.service;

/**
 * 任务中心图片管理
 * @Author liubc
 * @create 2022-06-01 16:04
 * @Description: good good study,day day up
 */
public interface FileService {

    /**
     * 更新图片路径
     * @param path
     * @param group
     * @param fileName
     * @param id
     * @return
     */
    int updateFilePictureUrl(String path, String group, String fileName, String id);
}
