package com.b2b.common.tenant;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * MyBatis-Plus 多租户行级隔离拦截器配置
 *
 * <p>遵循 ADR 0001 & ADR 0005 规范，在所有多租户业务表的 SQL 执行期
 * 自动追加 {@code WHERE company_id = ?} 过滤条件，防止越权。</p>
 *
 * @author b2b-commerce-backend
 */
@Configuration
public class MybatisPlusTenantConfig {

    @Autowired
    public void configureJacksonTypeHandler(ObjectMapper objectMapper) {
        JacksonTypeHandler.setObjectMapper(objectMapper);
    }

    /**
     * 系统级/公共非租户表白名单（不自动追加 company_id 过滤）
     */
    private static final Set<String> IGNORE_TENANT_TABLES = Set.of(
            "pms_category",
            "pms_product",
            "pms_sku",
            "price_tier",
            "org_company",
            "org_user",
            "flyway_schema_history",
            "oms_order_line",
            "mkt_promotion"
    );

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {

            @Override
            public Expression getTenantId() {
                String companyId = TenantContextHolder.getCompanyId();
                if (companyId == null) {
                    // 当无租户上下文时（如部分异步系统任务或公共数据读取），返回空字符串或特定占位符
                    return new StringValue("");
                }
                return new StringValue(companyId);
            }

            @Override
            public String getTenantIdColumn() {
                return "company_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                // 如果当前上下文未设置租户，或表属于白名单，则忽略租户过滤
                return TenantContextHolder.getCompanyId() == null || IGNORE_TENANT_TABLES.contains(tableName.toLowerCase());
            }
        }));
        return interceptor;
    }
}
