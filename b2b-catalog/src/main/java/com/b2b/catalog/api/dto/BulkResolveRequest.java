package com.b2b.catalog.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 批量采购清单解析请求参数 (BulkResolveRequest)
 *
 * <p>输入多行文本（支持空格、制表符或逗号分隔的 SKU 编码与采购数量），由服务端逐行解析。</p>
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量采购清单解析请求")
public class BulkResolveRequest implements Serializable {

    @NotBlank(message = "采购清单文本不可为空")
    @Schema(description = "多行文本清单，每行包含 SKU编码 [数量]", example = "SKU-NSK-6205ZZ 100\nSKU-SICK-WL12G3 20")
    private String rawText;
}
