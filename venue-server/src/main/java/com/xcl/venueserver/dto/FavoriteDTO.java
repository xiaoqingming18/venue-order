package com.xcl.venueserver.dto;

import lombok.Data;

/**
 * 收藏场馆请求DTO
 */
@Data
public class FavoriteDTO {

    /**
     * 场馆ID
     */
    private Long venueId;

    /**
     * 收藏备注
     */
    private String notes;
} 