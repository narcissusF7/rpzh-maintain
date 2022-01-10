package com.rpzh.service;

import com.rpzh.dto.DeptDto;
import com.rpzh.pojo.Dept;

import java.util.List;

/**
 * @Author liubc
 * @create 2021/5/18 14:18
 */
public interface DeptService {
    /**
     * 查询所有的部门
     * @return
     */
    List<Dept> findAllDept(DeptDto deptDto);


    /**
     * 查询部门是否存在
     * @param parentId
     * @param deptName
     * @return
     */
    Integer findOneDept(Integer parentId, String deptName);

    /**
     * 新增部门
     * @param deptDto
     */
    void addDept(DeptDto deptDto);

    /**
     * 查询是否存在下级部门
     * @param deptId
     * @return
     */
    Integer findDepts(Integer deptId);

    /**
     * 判断部门下是否存在用户
     * @param deptId
     * @return
     */
    Integer findUser(Integer deptId);

    /**
     * 删除部门
     * @param deptId
     * @return
     */
    Integer deleteDeptByDeptId(Integer deptId);

    /**
     * 根据父部门id获取父部门名称
     * @param parentId
     * @return
     */
    String findDeptNameByParentId(Integer parentId);

    /**
     * 查看部门下所有的子部门
     * @param deptId
     * @return
     */
    List<String> findAllDeptByDeptId(Integer deptId);

    /**
     * 修改部门
     * @param deptDto
     * @return
     */
    Integer updateDept(DeptDto deptDto);

    /**
     * 获取不包含本部门所有的同级部门
     * @param parentId
     * @param deptId
     * @return
     */
    List<String> findDeptNameNoContainThis(Integer parentId, Integer deptId);

    /**
     * 搜索部门
     * @param deptDto
     * @return
     */
    List<Dept> searchDept(DeptDto deptDto);

    /**
     * 修改部门的状态
     * @param deptDto
     * @return
     */
    Integer changeDeptStutas(DeptDto deptDto);
}
