package com.rpzh.dao;

import com.rpzh.dto.DeptDto;
import com.rpzh.pojo.Dept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/5/18 14:19
 */
@Mapper
public interface DeptDao {

    /**
     *查询所有的部门
     * @return
     */
    List<Dept> findAllDept(@Param("deptDto") DeptDto deptDto);

    /**
     * 查询部门是否存在
     * @param parentId
     * @param deptName
     * @return
     */
    Integer findOneDept(@Param("parentId") Integer parentId, @Param("deptName") String deptName);

    /**
     * 新增部门
     * @param deptDto
     */
    void addDept(@Param("deptDto") DeptDto deptDto);

    /**
     * 查询祖级列表
     * @param deptId
     * @return
     */
    String findAncestors(@Param("deptId") Integer deptId);

    /**
     * 查询是否存在下级部门
     * @param deptId
     * @return
     */
    Integer findDepts(@Param("deptId") Integer deptId);

    /**
     * 查询部门下是否存在用户
     * @param deptId
     * @return
     */
    Integer findUser(@Param("deptId") Integer deptId);

    /**
     * 删除部门
     * @param deptId
     * @return
     */
    Integer deleteDeptByDeptId(@Param("deptId") Integer deptId);

    /**
     * 获取父部门的名称
     * @param parentId
     * @return
     */
    String findDeptNameByParentId(@Param("parentId") Integer parentId);

    /**
     * 查询部门下所有的子部门
     * @param deptId
     * @return
     */
    List<String> findAllDeptByDeptId(@Param("deptId") Integer deptId);

    /**
     * 修改部门
     * @param deptDto
     * @return
     */
    Integer updateDept(@Param("deptDto") DeptDto deptDto);

    /**
     * 获取不包含本部门所有的同级部门
     * @param parentId
     * @param deptId
     * @return
     */
    List<String> findDeptNameNoContainThis(@Param("parentId") Integer parentId, @Param("deptId") Integer deptId);

    /**
     * 搜索部门
     * @param deptDto
     * @return
     */
    List<Dept> searchDept(@Param("deptDto") DeptDto deptDto);

    /**
     * 修改部门状态
     * @param deptDto
     * @return
     */
    Integer changeDeptStutas(@Param("deptDto") DeptDto deptDto);

    /**
     * 查询所有的子部门id
     * @param deptId1
     * @return
     */
    List<Integer> findParentSonDeptId(@Param("deptId1") Integer deptId1);

}
