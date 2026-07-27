package edu.fjut.mall.common.page;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页响应
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private int pageNum;

    /** 每页条数 */
    private int pageSize;

    /** 总页数 */
    private int totalPages;

    private PageResult() {
    }

    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records != null ? records : Collections.emptyList();
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    }

    /**
     * 快捷构建空结果
     */
    public static <T> PageResult<T> empty(PageQuery query) {
        return new PageResult<>(Collections.emptyList(), 0, query.getPageNum(), query.getPageSize());
    }

    /**
     * 数据转换（用于 Entity → VO）
     */
    public <R> PageResult<R> map(Function<? super T, ? extends R> converter) {
        List<R> converted = this.records.stream().map(converter).collect(Collectors.toList());
        return new PageResult<>(converted, this.total, this.pageNum, this.pageSize);
    }
}
