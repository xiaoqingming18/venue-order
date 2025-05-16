package com.xcl.venueserver.dto;

import lombok.Data;

/**
 * 分页查询DTO基类
 */
@Data
public class PageDTO {
    
    /**
     * 当前页码，默认为1
     */
    private Integer current = 1;
    
    /**
     * 每页大小，默认为10
     */
    private Integer size = 10;
    
    /**
     * 排序字段
     */
    private String orderField;
    
    /**
     * 排序方式：asc-升序 desc-降序
     */
    private String orderType;
} 