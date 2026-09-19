package com.b2b.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 批量勾选/取消勾选采购车商品请求 (BatchSelectCart Request)
 *
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量勾选采购车请求")
public class BatchSelectCartRequest implements Serializable {

    @Schema(description = "指定操作的采购车行 ID 列表 (为空时代表全选/全不选)", example = "[\"cart_item_1\", \"cart_item_2\"]")
    private List<String> itemIds;

    @Schema(description = "勾选状态 (true: 勾选, false: 取消勾选)", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "勾选状态不能为空")
    private Boolean selected;
}
