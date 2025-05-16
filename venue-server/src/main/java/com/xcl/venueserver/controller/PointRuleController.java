package com.xcl.venueserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xcl.venueserver.dto.PointRuleDTO;
import com.xcl.venueserver.entity.PointRule;
import com.xcl.venueserver.service.PointRuleService;
import com.xcl.venueserver.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 积分规则控制器
 */
@RestController
@RequestMapping("/api")
public class PointRuleController {

    @Autowired
    private PointRuleService pointRuleService;

    /**
     * 获取所有启用的积分规则(用户端)
     */
    @GetMapping("/user/points/rules")
    public ResultVO<List<PointRule>> getAllEnabledRules() {
        List<PointRule> rules = pointRuleService.getAllEnabledRules();
        return ResultVO.success(rules);
    }

    /**
     * 分页获取积分规则(管理端)
     */
    @GetMapping("/admin/points/rules")
    public ResultVO<IPage<PointRule>> getRulesByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<PointRule> pageResult = pointRuleService.getRulesByPage(page, size);
        return ResultVO.success(pageResult);
    }

    /**
     * 根据ID获取积分规则
     */
    @GetMapping("/admin/points/rules/{id}")
    public ResultVO<PointRule> getRuleById(@PathVariable Integer id) {
        PointRule rule = pointRuleService.getRuleById(id);
        if (rule == null) {
            return ResultVO.error("积分规则不存在");
        }
        return ResultVO.success(rule);
    }

    /**
     * 添加或更新积分规则
     */
    @PostMapping("/admin/points/rules")
    public ResultVO<Void> saveOrUpdateRule(@Valid @RequestBody PointRuleDTO pointRuleDTO) {
        boolean result = pointRuleService.saveOrUpdateRule(pointRuleDTO);
        if (result) {
            return ResultVO.success("保存成功");
        } else {
            return ResultVO.error("保存失败，可能规则类型已存在");
        }
    }

    /**
     * 更新积分规则状态
     */
    @PutMapping("/admin/points/rules/{id}/status/{status}")
    public ResultVO<Void> updateRuleStatus(
            @PathVariable Integer id,
            @PathVariable Integer status) {
        boolean result = pointRuleService.updateRuleStatus(id, status);
        if (result) {
            return ResultVO.success("更新成功");
        } else {
            return ResultVO.error("更新失败，积分规则不存在");
        }
    }

    /**
     * 删除积分规则
     */
    @DeleteMapping("/admin/points/rules/{id}")
    public ResultVO<Void> deleteRule(@PathVariable Integer id) {
        boolean result = pointRuleService.deleteRule(id);
        if (result) {
            return ResultVO.success("删除成功");
        } else {
            return ResultVO.error("删除失败，积分规则不存在");
        }
    }
} 