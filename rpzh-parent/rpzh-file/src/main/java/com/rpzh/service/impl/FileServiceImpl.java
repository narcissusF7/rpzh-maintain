package com.rpzh.service.impl;

import com.rpzh.dao.FileDao;
import com.rpzh.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 任务中心图片管理
 * @Author liubc
 * @create 2022-06-01 16:04
 * @Description: good good study,day day up
 */
@Service
public class FileServiceImpl implements FileService {


    @Autowired
    private FileDao fileDao;

    /**
     * 上传图片
     *
     * @param path 图片路径
     * @param group 图片组名
     * @param fileName 文件名
     * @param id 任务主键id
     * @return
     */
    @Override
    public int insertFilePictureUrl(String path, String group, String fileName, String id) {
        int insertFlag = fileDao.insertFilePictureUrl(path,group,fileName,id);
        return insertFlag;
    }

    /**
     * 删除数据库中的地址
     *
     * @param groupName
     * @param fileName
     * @return
     */
    @Override
    public int deletePic(String groupName, String fileName) {
        int delFlag = fileDao.deletePic(groupName,fileName);
        return delFlag;
    }
}