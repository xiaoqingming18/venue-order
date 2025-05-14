package com.xcl.venueserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库工具类，用于应用启动时执行SQL脚本
 */
@Slf4j
@Component
public class SqlTestUtil implements CommandLineRunner {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("开始执行数据库表结构更新...");
            
            // 检查venue_location表是否存在price字段
            String checkSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'venue_location' AND column_name = 'price'";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count != null && count == 0) {
                log.info("venue_location表缺少price字段，开始添加...");
                
                // 为venue_location表添加price字段
                String alterTableSql = "ALTER TABLE `venue_location` ADD COLUMN `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '位置单独价格，为空则使用场馆基准价格' AFTER `status`";
                jdbcTemplate.execute(alterTableSql);
                
                log.info("venue_location表结构更新完成！");
            } else {
                log.info("venue_location表已包含price字段，无需更新");
            }
            
            // 检查venue_facility表是否存在price字段
            checkSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'venue_facility' AND column_name = 'price'";
            count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            if (count != null && count == 0) {
                log.info("venue_facility表缺少price字段，开始添加...");
                
                // 为venue_facility表添加price字段
                String alterTableSql = "ALTER TABLE `venue_facility` ADD COLUMN `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '设施单独价格，为空则使用场馆基准价格' AFTER `position_desc`";
                jdbcTemplate.execute(alterTableSql);
                
                log.info("venue_facility表结构更新完成！");
            } else {
                log.info("venue_facility表已包含price字段，无需更新");
            }
            
            log.info("数据库表结构检查和更新完成");
        } catch (Exception e) {
            log.error("执行数据库表结构更新时发生错误", e);
        }
    }
} 