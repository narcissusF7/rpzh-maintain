package com.rpzh.service;

import com.rpzh.dto.DataContrastDto;
import com.rpzh.dto.NodeDto;
import com.rpzh.entity.Result;

import java.util.Date;

/**
 * @Author: zyq
 * @Date: 2022/06/23/13:46
 * @Description: 功能
 */
public interface DataContrastService {

    Result getNodeData(NodeDto nodeDto);

    Result getNodeDateList(String dataContrasts, String gatherType, Date startTime, Date endTime, Date beginTime, Date finishTime);
}
