package com.rpzh.dao;

import com.rpzh.dto.NodeDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 节点管理
 * @Author liubc
 * @create 2022/3/3 10:58
 * @Description: good good study,day day up
 */
@Mapper
public interface NodeDao {

    /**
     * 添加节点
     * @param nodeDto
     * @return
     */
    int addNode(@Param("nodeDto") NodeDto nodeDto);

    /**
     * 查询设备id
     * @param nodeId
     * @return
     */
    String findNodeID(@Param("nodeId") String nodeId,@Param("id") Integer id);

    /**
     * 更新设备信息
     * @param nodeDto
     * @return
     */
    int updateNode(@Param("nodeDto") NodeDto nodeDto);
}
