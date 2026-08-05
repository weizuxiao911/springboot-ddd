package io.github.weizuxiao911.springboot.ddd.infrastructure.config.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import io.github.weizuxiao911.springboot.ddd.infrastructure.config.jpa.entity.BaseEntity;

/**
 * 基础实体 JPA 仓库
 */
@NoRepositoryBean
public interface BaseJpaRepository<T extends BaseEntity> extends JpaRepository<T, Long>,
                JpaSpecificationExecutor<T> {

}
