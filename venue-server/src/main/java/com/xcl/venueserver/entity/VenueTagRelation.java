package com.xcl.venueserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 场馆标签关联实体类
 */
@Data
@TableName("venue_tag_relation")
public class VenueTagRelation {

    /**
     * 关系ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 