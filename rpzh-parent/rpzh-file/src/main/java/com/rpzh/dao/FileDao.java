package com.rpzh.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 任务中心图片管理
 * @Author liubc
 * @create 2022-06-01 16:05
 * @Description: good good study,day day up
 */
@Mapper
public interface FileDao {


    /**
     * 更新图片路径
     * @param path
     * @param group
     * @param fileName
     * @param taskNum
     * @return
     */
    int updateFilePictureUrl(@Param("path") String path,@Param("group") String group,@Param("fileName") String fileName,@Param("id") String id);
}
