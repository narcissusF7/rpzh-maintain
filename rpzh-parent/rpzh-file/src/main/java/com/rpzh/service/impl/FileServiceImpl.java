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
     * 更新图片路径
     *
     * @param path
     * @param group
     * @param fileName
     * @param id
     * @return
     */
    @Override
    public int updateFilePictureUrl(String path, String group, String fileName, String id) {
        int updateFlag = fileDao.updateFilePictureUrl(path,group,fileName,id);
        return updateFlag;
    }
}