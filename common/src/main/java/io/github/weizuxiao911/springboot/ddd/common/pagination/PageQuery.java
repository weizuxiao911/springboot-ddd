package io.github.weizuxiao911.springboot.ddd.common.pagination;

import lombok.Data;
import java.io.Serializable;

/**
 * 分页查询参数
 * 用于分页查询请求的参数封装。
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码（从1开始） */
    private int page = 1;

    /** 每页大小 */
    private int size = 20;

    /** 排序字段 */
    private String sortBy;

    /** 排序方向（ASC/DESC） */
    private String sortDirection = "DESC";

    /**
     * 计算偏移量
     *
     * @return 偏移量
     */
    public int getOffset() {
        return (page - 1) * size;
    }

    /**
     * 校验并修正分页参数
     */
    public void validate() {
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 20;
        }
    }
}
