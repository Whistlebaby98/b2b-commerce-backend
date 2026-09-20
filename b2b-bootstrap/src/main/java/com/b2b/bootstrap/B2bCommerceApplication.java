package com.b2b.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 企采云对公商城后端服务启动主类 (B2bCommerceApplication)
 *
 * <p>聚合所有业务限界上下文（IAM、Catalog、Pricing、Trade、Approval、Finance），
 * 负责 Spring 容器启动、自动装配与 Flyway 数据库迁移初始化。</p>
 *
 * @author b2b-commerce-backend
 */
@SpringBootApplication(scanBasePackages = "com.b2b")
@MapperScan(basePackages = {"com.b2b.**.mapper", "com.b2b.common.outbox"})
public class B2bCommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(B2bCommerceApplication.class, args);
    }
}
