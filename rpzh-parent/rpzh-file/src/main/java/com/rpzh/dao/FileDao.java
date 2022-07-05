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
     * 上传图片
     * @param path
     * @param group
     * @param fileName
     * @param id
     * @return
     */
    int insertFilePictureUrl(@Param("path") String path,@Param("group") String group,@Param("fileName") String fileName,@Param("id") String id);

    /**
     * 删除数据库中的路径
     * @param groupName
     * @param fileName
     * @return
     */
    int deletePic(@Param("groupName")String groupName,@Param("fileName")String fileName);
}
