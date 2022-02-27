package com.rpzh.mapper;

import com.rpzh.entity.LogSystemModule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemModuleMapper {
    LogSystemModule findSystemModule();
}
