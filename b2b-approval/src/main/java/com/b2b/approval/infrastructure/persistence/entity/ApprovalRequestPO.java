package com.b2b.approval.infrastructure.persistence.entity;

import com.b2b.approval.api.dto.ApprovalRequestDTO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 订单审批工作项持久化实体 (ApprovalRequestPO)
 *
 * <p>映射数据表 {@code act_approval_request}，记录订单审批状态与处理轨迹。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "act_approval_request", autoResultMap = true)
public class ApprovalRequestPO implements Serializable {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("order_id")
    private String orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("company_id")
    private String companyId;

    @TableField("status")
    private String status;

    @TableField("current_node")
    private String currentNode;

    @TableField("submitted_by")
    private String submittedBy;

    @TableField(value = "timeline", typeHandler = JacksonTypeHandler.class)
    private List<ApprovalRequestDTO.ApprovalTimelineNode> timeline;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
