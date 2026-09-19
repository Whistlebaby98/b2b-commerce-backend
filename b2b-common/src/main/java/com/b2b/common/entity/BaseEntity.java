package com.b2b.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

/**
 * 业务持久化实体基类 (BaseEntity)
 *
 * <p>包含多租户隔离字段 {@code companyId}、审计时间戳与逻辑删除标识。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
public abstract class BaseEntity implements Serializable {

    /**
     * 所属客户企业组织 ID (用于行级隔离)
     */
    @TableField("company_id")
    private String companyId;

    /**
     * 实体创建时间 (UTC 时间戳)
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    /**
     * 实体更新时间 (UTC 时间戳)
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;

    /**
     * 逻辑删除标识 (false: 正常, true: 已删除)
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
