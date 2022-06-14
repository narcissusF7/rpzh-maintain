package com.rpzh.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RfidRelationshipDao {
    Integer addRfidRelationship(@Param("rfid") String rfid, @Param("rfidType")String rfidType , @Param("siteId")Integer siteId);

    Integer deleteRfidRelationship(@Param("siteIds") List<Integer> ids, @Param("rfidType")String rfidType);

    Integer findRfidCount(@Param("rfid") String rfid);
}
