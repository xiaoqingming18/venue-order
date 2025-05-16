package com.xcl.venueserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xcl.venueserver.entity.UserFavoriteVenue;
import com.xcl.venueserver.vo.UserFavoriteVenueVO;

import java.util.List;

/**
 * 用户收藏场馆服务接口
 */
public interface UserFavoriteVenueService extends IService<UserFavoriteVenue> {

    /**
     * 添加收藏
     * @param userId 用户ID
     * @param venueId 场馆ID
     * @param notes 备注信息
     * @return 收藏记录
     */
    UserFavoriteVenue addFavorite(Long userId, Long venueId, String notes);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param venueId 场馆ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long venueId);
    
    /**
     * 取消收藏(通过收藏ID)
     * @param userId 用户ID
     * @param favoriteId 收藏ID
     * @return 是否成功
     */
    boolean removeFavoriteById(Long userId, Long favoriteId);

    /**
     * 更新收藏备注
     * @param userId 用户ID
     * @param favoriteId 收藏ID
     * @param notes 新备注
     * @return 是否成功
     */
    boolean updateFavoriteNotes(Long userId, Long favoriteId, String notes);

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param venueId 场馆ID
     * @return 是否已收藏
     */
    boolean isFavorite(Long userId, Long venueId);

    /**
     * 获取用户收藏列表
     * @param userId 用户ID
     * @return 收藏场馆VO列表
     */
    List<UserFavoriteVenueVO> getUserFavorites(Long userId);
    
    /**
     * 获取用户收藏列表（分页）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 收藏场馆VO分页对象
     */
    Page<UserFavoriteVenueVO> getUserFavoritesPage(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取收藏详情
     * @param favoriteId 收藏ID
     * @return 收藏场馆VO
     */
    UserFavoriteVenueVO getFavoriteDetail(Long favoriteId);
} 