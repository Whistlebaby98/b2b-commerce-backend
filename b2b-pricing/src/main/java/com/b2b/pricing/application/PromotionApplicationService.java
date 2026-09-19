package com.b2b.pricing.application;

import com.b2b.common.api.ResultCode;
import com.b2b.common.exception.BizException;
import com.b2b.pricing.api.dto.PromotionDTO;
import com.b2b.pricing.infrastructure.persistence.entity.PromotionPO;
import com.b2b.pricing.infrastructure.persistence.mapper.PromotionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 营销与促销活动应用服务 (PromotionApplicationService)
 *
 * <p>提供前台进行中的促销活动查询、详情检索与资格预检。</p>
 *
 * @author b2b-commerce-backend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionApplicationService {

    private final PromotionMapper promotionMapper;

    /**
     * 查询当前正在生效且对公开放的促销活动列表
     *
     * @return 正在进行的活动列表（按优先级降序）
     */
    public List<PromotionDTO> listActivePromotions() {
        Instant now = Instant.now();
        List<PromotionPO> list = promotionMapper.selectList(
                new LambdaQueryWrapper<PromotionPO>()
                        .eq(PromotionPO::getIsActive, true)
                        .le(PromotionPO::getStartsAt, now)
                        .ge(PromotionPO::getEndsAt, now)
                        .orderByDesc(PromotionPO::getPriority)
                        .orderByDesc(PromotionPO::getCreatedAt)
        );

        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 查询指定促销活动详情
     *
     * @param id 活动唯一标识
     * @return 活动规则详情
     */
    public PromotionDTO getPromotionById(String id) {
        PromotionPO po = promotionMapper.selectById(id);
        if (po == null) {
            throw new BizException(ResultCode.BAD_REQUEST.getCode(), "促销活动不存在: " + id);
        }
        return toDTO(po);
    }

    private PromotionDTO toDTO(PromotionPO po) {
        return PromotionDTO.builder()
                .id(po.getId())
                .code(po.getCode())
                .title(po.getTitle())
                .description(po.getDescription())
                .type(po.getType())
                .scope(po.getScope())
                .categoryIds(po.getCategoryIds())
                .productIds(po.getProductIds())
                .skuIds(po.getSkuIds())
                .startsAt(po.getStartsAt() != null ? po.getStartsAt().toString() : null)
                .endsAt(po.getEndsAt() != null ? po.getEndsAt().toString() : null)
                .isActive(po.getIsActive())
                .discountRate(po.getDiscountRate())
                .discountAmount(po.getDiscountAmount())
                .minSubtotal(po.getMinSubtotal())
                .minQuantity(po.getMinQuantity())
                .maxDiscount(po.getMaxDiscount())
                .tiers(po.getTiers())
                .stackable(po.getStackable())
                .priority(po.getPriority())
                .build();
    }
}
