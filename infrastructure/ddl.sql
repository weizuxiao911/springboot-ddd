-- Active: 1768275774191@@127.0.0.1@3306@test2
-- Framework DDL
-- Generated from JPA Entity definitions

CREATE TABLE `t_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `user_id`     VARCHAR(64)  NOT NULL COMMENT '用户唯一标识（业务ID）',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名（登录凭证）',
    `email`       VARCHAR(128) NULL     COMMENT '邮箱地址',
    `phone`       VARCHAR(32)  NULL     COMMENT '手机号码',
    `status`      VARCHAR(32)  NOT NULL COMMENT '用户状态',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `deleted`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否删除',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_tenant_id` (`tenant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
