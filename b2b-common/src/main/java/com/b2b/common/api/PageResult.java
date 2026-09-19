package com.b2b.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页结果包装对象 (PageResult)
 *
 * @param <T> 数据行泛型
 * @author b2b-commerce-backend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用分页结果对象")
public class PageResult<T> implements Serializable {

    @Schema(description = "当前页数据列表")
    private List<T> items;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer page;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize;

    @Schema(description = "总页数", example = "5")
    private Integer totalPages;
}
