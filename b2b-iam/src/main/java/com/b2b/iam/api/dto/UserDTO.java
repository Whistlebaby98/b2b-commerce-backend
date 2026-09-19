package com.b2b.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 买方用户信息数据传输对象 (BuyerUser DTO)
 *
 * <p>对齐前端 UserProfile 契约，描述当前登录用户及其在当前企业下的职责。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "买方用户信息")
public class UserDTO implements Serializable {

    @Schema(description = "用户唯一标识", example = "usr_001")
    private String id;

    @Schema(description = "用户姓名", example = "张采购")
    private String name;

    @Schema(description = "当前企业下买方角色 (buyer, approver, finance, owner)", example = "buyer")
    private String role;

    @Schema(description = "所属部门", example = "生产采购部")
    private String department;

    @Schema(description = "头像文本缩写", example = "张")
    private String avatarText;

    @Schema(description = "当前绑定的企业 ID", example = "org_1001")
    private String companyId;
}
