package com.rpzh.util;

import com.github.pagehelper.Page;
import java.util.List;

public class ManuallyPageUtil {
    /**
     * 手动分页
     * @param list 需要分页的数据
     * @param pageNum 当前页
     * @param pageSize 每页数量
     * @return
     */
    public static<T> Page<T> manuallyPage(List<T> list, int pageNum, int pageSize) {
        //创建一个page类
        Page<T> page = new Page<>(pageNum, pageSize);
        //总数量
        int total = list.size();
        page.setTotal(total);
        //起始下标
        int startIndex = (pageNum - 1) * pageSize;
        //终止下标
        int endIndex = Math.min(startIndex + pageSize, total);
        //截取数量
        page.addAll(list.subList(startIndex, endIndex));

        return page;
    }
}
