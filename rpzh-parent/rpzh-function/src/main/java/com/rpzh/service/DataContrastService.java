package com.rpzh.service;

import com.rpzh.dto.DataContrastDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;
import com.rpzh.pojo.NodeData;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/06/23/13:46
 * @Description: 功能
 */
public interface DataContrastService {

    Result getNodeData(NodeDto nodeDto);

    Result getNodeDateList(DataContrastDto dataContrastDto);
}
