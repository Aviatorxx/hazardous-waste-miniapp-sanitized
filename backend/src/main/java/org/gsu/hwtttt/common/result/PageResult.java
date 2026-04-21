package org.gsu.hwtttt.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "分页响应结果", description = "分页响应结果")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "当前页码", example = "1")
    private Long current;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Long size;

    @ApiModelProperty(value = "总记录数", example = "100")
    private Long total;

    @ApiModelProperty(value = "总页数", example = "10")
    private Long pages;

    @ApiModelProperty(value = "数据列表")
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long current, Long size, Long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.records = records;
        // 计算总页数
        this.pages = (total + size - 1) / size;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Long current, Long size, Long total, List<T> records) {
        return new PageResult<>(current, size, total, records);
    }

    /**
     * 空分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(1L, 10L, 0L, null);
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return current < pages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return current > 1;
    }
} 