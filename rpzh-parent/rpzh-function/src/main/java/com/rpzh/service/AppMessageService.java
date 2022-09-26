package com.rpzh.service;

import com.github.pagehelper.PageInfo;
import com.rpzh.dto.AppMessageDto;
import com.rpzh.vo.AppMessageVo;

import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/09/25/9:59
 * @Description: 功能
 */
public interface AppMessageService {
    Integer updateStatus(Integer id);
    Integer insertAppMessage(AppMessageDto appMessageDto);
    PageInfo<AppMessageVo> findSendInfo(AppMessageDto appMessageDto);
    PageInfo<AppMessageVo> findRecipientInfo(AppMessageDto appMessageDto);
    PageInfo<AppMessageVo> findDraftInfo(AppMessageDto appMessageDto);
    AppMessageVo findRecipientInfoById(Integer id);
    AppMessageVo findSendInfoById(List<String> ids);
    AppMessageVo findDraftInfoById(List<String> ids);
    Integer updateDraftInfo(AppMessageDto appMessageDto);
    Integer deleteMessage(List<String> ids);
}
