package com.rpzh.service;

import com.rpzh.dto.DataContrastDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;

/**
 * @Author: zyq
 * @Date: 2022/06/23/13:46
 * @Description: 功能
 */
public interface DataContrastService {

    Result getNodeData(NodeDto nodeDto);

    Result getNodeDateList(DataContrastDto dataContrastDto);
}
