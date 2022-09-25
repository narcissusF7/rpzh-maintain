package com.rpzh.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rpzh.dao.AppMessageDao;
import com.rpzh.dto.AppMessageDto;
import com.rpzh.service.AppMessageService;
import com.rpzh.util.RedisUtil;
import com.rpzh.util.TokenDecode;
import com.rpzh.vo.AppMessageVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Author: zyq
 * @Date: 2022/09/25/9:59
 * @Description: 功能
 */
@Service
public class AppMessageServiceImpl implements AppMessageService {
    @Resource
    private AppMessageDao appMessageDao;

    @Resource
    private RedisUtil redisUtil;
    @Override
    public Integer updateStatus(Integer id){
        return appMessageDao.updateStatus(id);
    }

    @Override
    public Integer insertAppMessage(AppMessageDto appMessageDto){
        appMessageDto.setType(2);
        //获取创建人
        String createUser = TokenDecode.getUserInfo().get("username");
        appMessageDto.setSendName(redisUtil.hget("USER_"+createUser,"nickname").toString());
        appMessageDto.setSendPhone(redisUtil.hget("USER_"+createUser,"phonenumber").toString());
        int i=0;
        Date date = new Date();
        for (String recipientName : appMessageDto.getRecipientNames()) {
            appMessageDto.setRecipientName(redisUtil.hget("USER_"+recipientName,"nickname").toString());
            appMessageDto.setRecipientPhone(redisUtil.hget("USER_"+recipientName,"phonenumber").toString());
            appMessageDto.setCreateTime(date);
            appMessageDao.insertAppMessage(appMessageDto);
            i++;
        }
        return i;
    }

    @Override
    public PageInfo<AppMessageVo> findRecipientInfo(AppMessageDto appMessageDto){
        PageHelper.startPage(appMessageDto.getPage(), appMessageDto.getSize());
        long todayTime = new Date().getTime();
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(todayTime);
        int d1 = instance.get(Calendar.DAY_OF_YEAR);
        //分页查询历史预警
        List<AppMessageVo> appMessageVos = appMessageDao.findRecipientInfo(appMessageDto);
        for (AppMessageVo appMessageVo : appMessageVos) {
            long sendTime = appMessageVo.getSendTime().getTime();
            instance.setTimeInMillis(sendTime);
            int d2 = instance.get(Calendar.DAY_OF_YEAR);
            if(d1==d2){
                SimpleDateFormat f = new SimpleDateFormat("HH:mm");
                appMessageVo.setStrTime(f.format(appMessageVo.getSendTime()));
            }else{
                SimpleDateFormat f = new SimpleDateFormat("MM-dd HH:mm");
                appMessageVo.setStrTime(f.format(appMessageVo.getSendTime()));
            }
        }
        return new PageInfo<>(appMessageVos);
    }

    @Override
    public PageInfo<AppMessageVo> findSendInfo(AppMessageDto appMessageDto){
        PageHelper.startPage(appMessageDto.getPage(), appMessageDto.getSize());
        long todayTime = new Date().getTime();
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(todayTime);
        int d1 = instance.get(Calendar.DAY_OF_YEAR);
        //分页查询历史预警
        List<AppMessageVo> appMessageVos = appMessageDao.findSendInfo(appMessageDto);
        for (AppMessageVo appMessageVo : appMessageVos) {
            appMessageVo.setIds(Arrays.asList(appMessageVo.getId().split(",")));
            long sendTime = appMessageVo.getSendTime().getTime();
            instance.setTimeInMillis(sendTime);
            int d2 = instance.get(Calendar.DAY_OF_YEAR);
            if(d1==d2){
                SimpleDateFormat f = new SimpleDateFormat("HH:mm");
                appMessageVo.setStrTime(f.format(appMessageVo.getSendTime()));
            }else{
                SimpleDateFormat f = new SimpleDateFormat("MM-dd HH:mm");
                appMessageVo.setStrTime(f.format(appMessageVo.getSendTime()));
            }
        }
        return new PageInfo<>(appMessageVos);
    }

    @Override
    public PageInfo<AppMessageVo> findDraftInfo(AppMessageDto appMessageDto){
        String sendUser = TokenDecode.getUserInfo().get("username");
        appMessageDto.setSendName(redisUtil.hget("USER_"+sendUser,"nickname").toString());
        PageHelper.startPage(appMessageDto.getPage(), appMessageDto.getSize());
        List<AppMessageVo> messageVos = appMessageDao.findDraftInfo(appMessageDto);
        for (AppMessageVo messageVo : messageVos) {
            messageVo.setIds(Arrays.asList(messageVo.getId().split(",")));
        }
        return new PageInfo<>(messageVos);
    }

    @Override
    public AppMessageVo findRecipientInfoById(Integer id) {
        appMessageDao.updateRead(id);
        AppMessageVo messageVo = appMessageDao.findRecipientInfoById(id);
        return messageVo;
    }

    @Override
    public AppMessageVo findSendInfoById(List<String> ids) {
        AppMessageVo appMessageVo = appMessageDao.findSendInfoById(ids.get(0));
        List<AppMessageVo> appMessageVos = appMessageDao.findInfo(ids);
        appMessageVo.setAppMessageVos(appMessageVos);
        return appMessageVo;
    }

    @Override
    public AppMessageVo findDraftInfoById(List<String> ids){
        AppMessageVo messageVo = appMessageDao.findDraftInfoById(ids.get(0));
        messageVo.setRecipientNames(appMessageDao.findNames(ids));
        return messageVo;
    }

    @Override
    public Integer updateDraftInfo(AppMessageDto appMessageDto){
        appMessageDao.deleteMessage(appMessageDto.getIds());
        appMessageDto.setType(2);
        //获取创建人
        String createUser = TokenDecode.getUserInfo().get("username");
        appMessageDto.setSendName(redisUtil.hget("USER_"+createUser,"nickname").toString());
        appMessageDto.setSendPhone(redisUtil.hget("USER_"+createUser,"phonenumber").toString());
        int i=0;
        Date date = new Date();
        for (String recipientName : appMessageDto.getRecipientNames()) {
            appMessageDto.setRecipientName(redisUtil.hget("USER_"+recipientName,"nickname").toString());
            appMessageDto.setRecipientPhone(redisUtil.hget("USER_"+recipientName,"phonenumber").toString());
            appMessageDto.setCreateTime(date);
            appMessageDao.insertAppMessage(appMessageDto);
            i++;
        }
        return i;
    }
}
