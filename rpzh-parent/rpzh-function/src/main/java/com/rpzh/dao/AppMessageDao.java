package com.rpzh.dao;

import com.rpzh.dto.AppMessageDto;
import com.rpzh.vo.AppMessageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/09/24/9:28
 * @Description: 功能
 */
@Mapper
public interface AppMessageDao {
    Integer findCount(@Param("ip")String ip);
    Integer insertAppMessage(@Param("appMessageDto")AppMessageDto appMessageDto);
    Integer updateStatus(@Param("id")Integer id);
    List<AppMessageVo> findRecipientInfo(@Param("appMessageDto")AppMessageDto appMessageDto);
    List<AppMessageVo> findSendInfo(@Param("appMessageDto")AppMessageDto appMessageDto);
    List<AppMessageVo> findDraftInfo(@Param("appMessageDto")AppMessageDto appMessageDto);
    AppMessageVo findRecipientInfoById(@Param("id")Integer id);
    AppMessageVo findSendInfoById(@Param("id")String id);
    List<AppMessageVo> findInfo(@Param("ids")List<String> ids);
    Integer updateRead(@Param("id")Integer id);
    AppMessageVo findDraftInfoById(@Param("id")String id);
    List<String> findNames(@Param("ids")List<String> ids);
    Integer deleteMessage(@Param("ids")List<String> ids);
}
