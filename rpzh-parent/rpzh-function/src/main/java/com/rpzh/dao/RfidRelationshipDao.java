package com.rpzh.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RfidRelationshipDao {
    Integer addRfidRelationship(@Param("rfid") String rfid, @Param("siteType")String siteType , @Param("siteId")Integer siteId);
}
