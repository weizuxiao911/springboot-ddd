package top.archaiharness.framework.infrastructure.config.jpa.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Data;

/**
 * 基础实体类
 */
@Data
@MappedSuperclass
@DynamicInsert
@DynamicUpdate
public abstract class BaseEntity {

    /**
     * 自增ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @UpdateTimestamp
    @Column(name = "modify_time")
    private LocalDateTime modifyTime;

    /**
     * 是否删除
     */
    @Column(name = "deleted")
    private Boolean deleted;

    @Version
    @Column(name = "version")
    private Integer version;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.modifyTime = LocalDateTime.now();
        this.version = 0;
        this.deleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifyTime = LocalDateTime.now();
    }
}
