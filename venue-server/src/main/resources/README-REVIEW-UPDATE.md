# 评价模块功能变更说明

## 需求变更

评价模块由原来的"需要审核才能展示"变更为"无需审核直接发布"，并增加了以下功能：
1. 用户可以举报评价
2. 管理员可以查看举报并处理
3. 管理员可以封禁违规评价
4. 管理员可以解除评价封禁

## 数据库变更

1. 修改`venue_review`表的status字段含义：
   - 原来：0-待审核，1-已通过，2-已拒绝
   - 现在：0-正常，1-已封禁

2. 新增`venue_review_report`表（评价举报表）：
   - id: 举报ID
   - review_id: 评价ID
   - reporter_id: 举报人ID
   - reason: 举报原因
   - reason_detail: 举报详细说明
   - status: 状态（0-待处理，1-已通过，2-已拒绝）
   - admin_id: 处理管理员ID
   - admin_notes: 管理员处理备注
   - created_at: 创建时间
   - updated_at: 更新时间

3. 新增`venue_review_ban`表（评价封禁记录表）：
   - id: 封禁ID
   - review_id: 评价ID
   - admin_id: 操作管理员ID
   - report_id: 关联举报ID（可为空）
   - reason: 封禁原因
   - created_at: 创建时间

## 新增实体类

1. `VenueReviewReport` - 评价举报实体类
2. `VenueReviewBan` - 评价封禁记录实体类
3. `ReviewReportDTO` - 评价举报DTO
4. `ReviewBanDTO` - 评价封禁DTO
5. `ReviewReportAuditDTO` - 评价举报审核DTO
6. `VenueReviewReportVO` - 评价举报VO

## 服务层变更

1. 修改`VenueReviewServiceImpl`：
   - 修改createReview方法，评价创建后直接设为正常状态(0)，无需审核
   - 修改updateReview方法，保持原状态不变，检查评价未被封禁才允许修改
   - 修改getVenueReviews方法，只显示状态为0（正常）的评价

2. 新增服务接口和实现：
   - `VenueReviewReportService` - 评价举报服务
   - `VenueReviewBanService` - 评价封禁服务

## 接口变更

1. 用户接口（VenueReviewController）：
   - 新增 POST `/api/reviews/{id}/report` - 举报评价
   - 新增 GET `/api/reviews/my-reports` - 获取我的举报列表

2. 管理员接口（AdminReviewController）：
   - 删除 PUT `/api/admin/reviews/{id}/audit` - 移除审核评价的接口
   - 新增 POST `/api/admin/reviews/{id}/ban` - 封禁评价
   - 新增 POST `/api/admin/reviews/{id}/unban` - 解除评价封禁
   - 新增 GET `/api/admin/reviews/{id}/ban-record` - 获取评价封禁记录
   - 新增 GET `/api/admin/reviews/ban-records` - 分页获取所有封禁记录
   - 新增 GET `/api/admin/reviews/reports` - 分页获取所有举报记录
   - 新增 GET `/api/admin/reviews/reports/{id}` - 获取举报详情
   - 新增 PUT `/api/admin/reviews/reports/{id}/audit` - 审核举报

## 业务逻辑变更

1. 评价发布：用户发布评价后，评价直接处于正常状态，对所有人可见
2. 举报处理：
   - 用户发现不良评价后可以举报
   - 管理员可以查看举报详情并进行审核
   - 审核通过可选择同时封禁评价
3. 封禁管理：
   - 管理员可直接封禁评价（无需举报触发）
   - 管理员可解除已封禁的评价
4. 评价可见性：
   - 正常状态(0)的评价对所有人可见
   - 已封禁(1)的评价在前台不可见

## 前后端交互说明

1. 前端评价列表展示：
   - 只展示状态为0（正常）的评价
   - 状态为1（已封禁）的评价在用户前台不显示

2. 举报功能：
   - 用户可以举报任何评价
   - 举报后状态为待处理(0)，等待管理员审核

3. 管理员功能：
   - 管理员可以查看所有评价，包括已封禁的评价
   - 管理员可以查看待处理的举报并进行审核
   - 管理员可以直接封禁/解封评价 