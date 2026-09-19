package com.b2b.iam.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 切换企业上下文请求参数
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "切换企业组织上下文请求")
public class SwitchContextRequest implements Serializable {

    @NotBlank(message = "目标企业组织 ID 不可为空")
    @Schema(description = "目标企业组织 ID", example = "company-lantu")
    private String targetCompanyId;
}
