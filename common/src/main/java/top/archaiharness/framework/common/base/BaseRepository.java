package top.archaiharness.framework.common.base;

import java.util.List;
import java.util.Optional;

/**
 * 仓库基础接口
 * 定义通用仓库能力，由基础设施层实现。
 *
 * @param <T> 实体类型
 * @param <ID> ID类型
 */
public interface BaseRepository<T, ID> {

    /**
     * 保存实体
     *
     * @param entity 要保存的实体
     * @return 保存后的实体
     */
    T save(T entity);

    /**
     * 根据ID查询实体
     *
     * @param id 实体ID
     * @return 实体 Optional
     */
    Optional<T> findById(ID id);

    /**
     * 查询所有实体
     *
     * @return 实体列表
     */
    List<T> findAll();

    /**
     * 根据ID删除实体
     *
     * @param id 实体ID
     */
    void deleteById(ID id);

    /**
     * 判断ID是否存在
     *
     * @param id 实体ID
     * @return 是否存在
     */
    boolean existsById(ID id);

    /**
     * 统计实体数量
     *
     * @return 实体数量
     */
    long count();
}
