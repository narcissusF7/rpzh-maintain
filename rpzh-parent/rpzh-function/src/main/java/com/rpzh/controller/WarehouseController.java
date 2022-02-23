package com.rpzh.controller;

import com.rpzh.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仓库管理
 * @Author liubc
 * @create 2022/2/23 11:31
 * @Description: good good study,day day up
 */
@RestController
@RequestMapping("/warehouse")
@CrossOrigin
public class WarehouseController {


    @Autowired
    private WarehouseService warehouseService;






}