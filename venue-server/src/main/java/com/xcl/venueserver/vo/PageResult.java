package com.xcl.venueserver.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果VO
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 当前页码
     */
    private long current;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 构造分页结果
     * @param records 数据列表
     * @param total 总记录数
     * @param size 每页大小
     * @param current 当前页码
     * @param pages 总页数
     */
    public PageResult(List<T> records, long total, long size, long current, long pages) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = pages;
    }

    /**
     * 空构造函数
     */
    public PageResult() {
    }

    /**
     * 从MyBatis-Plus的Page对象构造
     * @param page MyBatis-Plus分页对象
     * @param <E> 源数据类型
     * @return PageResult对象
     */
    public static <E> PageResult<E> fromPage(com.baomidou.mybatisplus.extension.plugins.pagination.Page<E> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getSize(),
                page.getCurrent(),
                page.getPages()
        );
    }
} 