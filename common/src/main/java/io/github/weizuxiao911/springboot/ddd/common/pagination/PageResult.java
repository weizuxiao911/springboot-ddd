package io.github.weizuxiao911.springboot.ddd.common.pagination;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 * 用于封装分页查询返回的数据。
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<T> items;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private int page;

    /** 每页大小 */
    private int size;

    /** 总页数 */
    private int totalPages;

    /**
     * 默认构造函数
     */
    public PageResult() {
    }

    /**
     * 全参数构造函数
     *
     * @param items 数据列表
     * @param total 总记录数
     * @param page 当前页码
     * @param size 每页大小
     */
    public PageResult(List<T> items, long total, int page, int size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    /**
     * 是否有下一页
     *
     * @return 是否有下一页
     */
    public boolean hasNext() {
        return page < totalPages;
    }

    /**
     * 是否有上一页
     *
     * @return 是否有上一页
     */
    public boolean hasPrevious() {
        return page > 1;
    }

    /**
     * 创建空的分页结果
     *
     * @param page 页码
     * @param size 每页大小
     * @param <T> 数据类型
     * @return 空的分页结果
     */
    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), 0, page, size);
    }
}
