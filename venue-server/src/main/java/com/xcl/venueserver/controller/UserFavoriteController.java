package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xcl.venueserver.common.utils.CurrentUser;
import com.xcl.venueserver.common.utils.Result;
import com.xcl.venueserver.dto.FavoriteDTO;
import com.xcl.venueserver.entity.UserFavoriteVenue;
import com.xcl.venueserver.service.UserFavoriteVenueService;
import com.xcl.venueserver.vo.UserFavoriteVenueVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏场馆控制器
 */
@Slf4j
@RestController
@RequestMapping({"/api/user/favorite", "/user/favorite"})
public class UserFavoriteController {

    private final UserFavoriteVenueService userFavoriteVenueService;
    private final CurrentUser currentUser;

    public UserFavoriteController(UserFavoriteVenueService userFavoriteVenueService, CurrentUser currentUser) {
        this.userFavoriteVenueService = userFavoriteVenueService;
        this.currentUser = currentUser;
    }

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Result<UserFavoriteVenue> addFavorite(@RequestBody FavoriteDTO favoriteDTO) {
        try {
            // 从token中获取用户ID
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }

            // 添加收藏
            UserFavoriteVenue favorite = userFavoriteVenueService.addFavorite(userId, favoriteDTO.getVenueId(), favoriteDTO.getNotes());
            return Result.success(favorite);
        } catch (Exception e) {
            log.error("添加收藏失败", e);
            return Result.error("添加收藏失败：" + e.getMessage());
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove/{venueId}")
    public Result<Boolean> removeFavorite(@PathVariable Long venueId) {
        try {
            // 从token中获取用户ID
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }

            // 取消收藏
            boolean success = userFavoriteVenueService.removeFavorite(userId, venueId);
            return success ? Result.success(true) : Result.error("取消收藏失败，可能未收藏该场馆");
        } catch (Exception e) {
            log.error("取消收藏失败", e);
            return Result.error("取消收藏失败：" + e.getMessage());
        }
    }

    /**
     * 通过收藏ID取消收藏
     */
    @DeleteMapping("/{favoriteId}")
    public Result<Boolean> removeFavoriteById(@PathVariable Long favoriteId) {
        try {
            // 从token中获取用户ID
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }

            // 取消收藏
            boolean success = userFavoriteVenueService.removeFavoriteById(userId, favoriteId);
            return success ? Result.success(true) : Result.error("取消收藏失败，可能未收藏该场馆或收藏ID不存在");
        } catch (Exception e) {
            log.error("取消收藏失败", e);
            return Result.error("取消收藏失败：" + e.getMessage());
        }
    }

    /**
     * 更新收藏备注
     */
    @PutMapping("/{favoriteId}/notes")
    public Result<Boolean> updateFavoriteNotes(@PathVariable Long favoriteId, @RequestBody Map<String, String> params) {
        try {
            // 从token中获取用户ID
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }

            String notes = params.get("notes");

            // 更新备注
            boolean success = userFavoriteVenueService.updateFavoriteNotes(userId, favoriteId, notes);
            return success ? Result.success(true) : Result.error("更新备注失败，收藏记录可能不存在");
        } catch (Exception e) {
            log.error("更新收藏备注失败", e);
            return Result.error("更新收藏备注失败：" + e.getMessage());
        }
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/list")
    public Result<List<UserFavoriteVenueVO>> getFavoriteList() {
        try {
            // 从token中获取用户ID
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }

            // 获取收藏列表
            List<UserFavoriteVenueVO> favorites = userFavoriteVenueService.getUserFavorites(userId);
            return Result.success(favorites);
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return Result.error("获取收藏列表失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取收藏列表
     */
    @GetMapping("/page")
    public Result<Page<UserFavoriteVenueVO>> getFavoritePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 从token中获取用户ID
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }

            // 获取收藏分页列表
            Page<UserFavoriteVenueVO> page = userFavoriteVenueService.getUserFavoritesPage(userId, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页获取收藏列表失败", e);
            return Result.error("分页获取收藏列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取收藏详情
     */
    @GetMapping("/{favoriteId}")
    public Result<UserFavoriteVenueVO> getFavoriteDetail(@PathVariable Long favoriteId) {
        try {
            // 获取收藏详情
            UserFavoriteVenueVO favorite = userFavoriteVenueService.getFavoriteDetail(favoriteId);
            if (favorite == null) {
                return Result.error("收藏记录不存在");
            }
            
            // 从token中获取用户ID并验证权限
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }
            
            // 验证收藏记录是否属于当前用户
            if (!favorite.getUserId().equals(userId) && !currentUser.isAdmin()) {
                return Result.error(403, "无权查看此收藏记录");
            }
            
            return Result.success(favorite);
        } catch (Exception e) {
            log.error("获取收藏详情失败", e);
            return Result.error("获取收藏详情失败：" + e.getMessage());
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{venueId}")
    public Result<Boolean> checkIsFavorite(@PathVariable Long venueId) {
        try {
            // 从token中获取用户ID
            Long userId = currentUser.getUserId();
            if (userId == null) {
                return Result.error(401, "用户未登录或登录已过期");
            }

            // 检查是否已收藏
            boolean isFavorite = userFavoriteVenueService.isFavorite(userId, venueId);
            return Result.success(isFavorite);
        } catch (Exception e) {
            log.error("检查收藏状态失败", e);
            return Result.error("检查收藏状态失败：" + e.getMessage());
        }
    }
} 