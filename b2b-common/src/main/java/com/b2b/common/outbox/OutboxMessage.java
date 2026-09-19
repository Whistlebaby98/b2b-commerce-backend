package com.b2b.common.outbox;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * 本地消息表实体 (Outbox Pattern)
 *
 * <p>遵循 ADR 0002 & ADR 0004 规范，在本地数据库事务中持久化领域事件，
 * 后续由可靠投递组件异步发送至 RocketMQ，保障分布式事务最终一致性。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_outbox_message")
public class OutboxMessage implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属企业组织 ID (租户隔离)
     */
    @TableField("company_id")
    private String companyId;

    /**
     * 全局唯一事件 ID (用于消费端幂等核销)
     */
    @TableField("event_id")
    private String eventId;

    /**
     * 领域事件类型 (如 OrderSubmittedEvent)
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 序列化后的事件载荷 (JSONB)
     */
    @TableField("payload")
    private String payload;

    /**
     * 发送状态: PENDING(待发送), SENT(已投递), FAILED(已废弃)
     */
    @TableField("status")
    private String status;

    /**
     * 已重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 下次重试投递时间
     */
    @TableField("next_retry_time")
    private Instant nextRetryTime;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
