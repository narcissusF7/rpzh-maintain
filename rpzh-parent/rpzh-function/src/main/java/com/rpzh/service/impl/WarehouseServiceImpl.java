package com.rpzh.service.impl;

import com.rpzh.dao.WarehouseDao;
import com.rpzh.service.WarehouseService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author liubc
 * @create 2022/2/23 11:33
 * @Description: good good study,day day up
 */
@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseDao warehouseDao;

}