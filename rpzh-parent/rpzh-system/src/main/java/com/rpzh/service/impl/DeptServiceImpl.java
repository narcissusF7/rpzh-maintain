package com.rpzh.service.impl;

import com.rpzh.dao.DeptDao;
import com.rpzh.dto.DeptDto;
import com.rpzh.pojo.Dept;
import com.rpzh.service.DeptService;
import com.rpzh.util.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author liubc
 * @create 2021/5/18 14:19
 */
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private DeptDao deptDao;

    /**
     * 查询所有的部门
     *
     * @return
     */
    @Override
    public List<Dept> findAllDept(DeptDto deptDto) {
        List<Dept> deptList = deptDao.findAllDept(deptDto);
        return getChildPerms(deptList, 0);
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<Dept> getChildPerms(List<Dept> list, int parentId) {
        List<Dept> returnList = new ArrayList<Dept>();
        for (Iterator<Dept> iterator = list.iterator(); iterator.hasNext(); ) {
            Dept t = (Dept) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private void recursionFn(List<Dept> list, Dept t) {
        // 得到子节点列表
        List<Dept> childList = getChildList(list, t);
        t.setChildren(childList);
        for (Dept tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 判断是否有子节点
     * @param list
     * @param t
     * @return
     */
    private boolean hasChild(List<Dept> list, Dept t) {
        return getChildList(list, t).size() > 0 ? true : false;
    }

    /**
     * 得到子节点列表
     * @param list
     * @param t
     * @return
     */
    private List<Dept> getChildList(List<Dept> list, Dept t) {
        List<Dept> tlist = new ArrayList<Dept>();
        Iterator<Dept> it = list.iterator();
        while (it.hasNext()) {
            Dept n = (Dept) it.next();
            if (n.getParentId().longValue() == t.getDeptId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }


    /**
     * 查询部门是否存在
     *
     * @param parentId
     * @param deptName
     * @return
     */
    @Override
    public Integer findOneDept(Integer parentId, String deptName) {
        Integer sum = deptDao.findOneDept(parentId,deptName);
        return sum;
    }

    /**
     * 新增部门
     *
     * @param deptDto
     */
    @Override
    public void addDept(DeptDto deptDto) {
        //获取父部门id
        Integer deptId = deptDto.getParentId();
        //根据父部门id,查询父部门祖级列表
        String ancestor = deptDao.findAncestors(deptId);
        //拼接插入祖级列表
        String ancestors = ancestor + ","+ deptId;
        //祖级目录
        deptDto.setAncestors(ancestors);
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        deptDto.setCreateBy(username);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        //设置创建时间
        deptDto.setCreateTime(dateStr);
        deptDao.addDept(deptDto);
    }

    /**
     * 查询是否存在下级部门
     *
     * @param deptId
     * @return
     */
    @Override
    public Integer findDepts(Integer deptId) {
        Integer sum = deptDao.findDepts(deptId);
        return sum;
    }

    /**
     * 判断部门下是否存在用户
     *
     * @param deptId
     * @return
     */
    @Override
    public Integer findUser(Integer deptId) {
        Integer sum = deptDao.findUser(deptId);
        return sum;
    }

    /**
     * 删除部门
     *
     * @param deptId
     * @return
     */
    @Override
    public Integer deleteDeptByDeptId(Integer deptId) {
        Integer delDept = deptDao.deleteDeptByDeptId(deptId);
        return delDept;
    }

    /**
     * 根据父部门id获取父部门名称
     *
     * @param parentId
     * @return
     */
    @Override
    public String findDeptNameByParentId(Integer parentId) {
        String deptName = deptDao.findDeptNameByParentId(parentId);
        return deptName;
    }

    /**
     * 查看部门下所有的子部门
     *
     * @param deptId
     * @return
     */
    @Override
    public List<String> findAllDeptByDeptId(Integer deptId) {
        List<String> deptList = deptDao.findAllDeptByDeptId(deptId);
        return deptList;
    }

    /**
     * 修改部门
     *
     * @param deptDto
     * @return
     */
    @Override
    public Integer updateDept(DeptDto deptDto) {
        //获取修改的部门id
        Integer deptId1 = deptDto.getDeptId();
        //查询该部门下所有的子部门id
        List<Integer> deptIdList = deptDao.findParentSonDeptId(deptId1);
        //获取父部门id
        Integer deptId = deptDto.getParentId();
        for (int i = 0; i < deptIdList.size(); i++) {
            Integer deptid = deptIdList.get(i);
            if (deptid.equals(deptId)){
                throw new RuntimeException("父部门的父部门不能是子部门");
            }
        }
        //根据父部门id,查询父部门祖级列表
        String ancestor = deptDao.findAncestors(deptId);
        //拼接插入祖级列表
        String ancestors = ancestor + ","+ deptId;
        //祖级目录
        deptDto.setAncestors(ancestors);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdf.format(date);
        //设置更新时间
        deptDto.setUpdateTime(dateStr);
        //获取用户信息
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        //获取用户名
        String username = userInfo.get("username");
        deptDto.setUpdateBy(username);
        Integer deptResult = deptDao.updateDept(deptDto);
        return deptResult;
    }

    /**
     * 获取不包含本部门所有的同级部门
     *
     * @param parentId
     * @param deptId
     * @return
     */
    @Override
    public List<String> findDeptNameNoContainThis(Integer parentId, Integer deptId) {
        List<String> deptName = deptDao.findDeptNameNoContainThis(parentId,deptId);
        return deptName;
    }

    /**
     * 搜索部门
     *
     * @param deptDto
     * @return
     */
    @Override
    public List<Dept> searchDept(DeptDto deptDto) {
        List<Dept> deptList = deptDao.searchDept(deptDto);
        return deptList;
    }

    /**
     * 修改部门的状态
     *
     * @param deptDto
     * @return
     */
    @Override
    public Integer changeDeptStutas(DeptDto deptDto) {
        Integer changeFlag = deptDao.changeDeptStutas(deptDto);
        return changeFlag;
    }

}