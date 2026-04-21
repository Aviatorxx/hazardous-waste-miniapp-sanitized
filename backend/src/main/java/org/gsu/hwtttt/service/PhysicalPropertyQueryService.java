package org.gsu.hwtttt.service;

import org.gsu.hwtttt.dto.request.PhysicalPropertySearchRequest;
import org.gsu.hwtttt.dto.response.PhysicalPropertyCategoryResponse;
import org.gsu.hwtttt.dto.response.PhysicalPropertySearchResponse;

import java.util.List;

/**
 * 物理特性查询服务接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
public interface PhysicalPropertyQueryService {

    /**
     * 获取所有物理特性分类信息
     *
     * @return 分类信息列表
     */
    List<PhysicalPropertyCategoryResponse> getPropertyCategories();

    /**
     * 根据分类查询危废的物理特性数据
     *
     * @param request 查询请求
     * @return 查询结果
     */
    PhysicalPropertySearchResponse searchByCategory(PhysicalPropertySearchRequest request);

    /**
     * 获取指定分类的有效记录总数
     *
     * @param categoryCode 分类代码
     * @return 记录总数
     */
    Long getCountByCategory(String categoryCode);

    /**
     * 根据分类和搜索条件获取有效记录总数
     *
     * @param categoryCode 分类代码
     * @param search 搜索关键字
     * @return 记录总数
     */
    Long getCountByCategory(String categoryCode, String search);

    /**
     * 根据查询请求获取有效记录总数（支持危害特性过滤）
     *
     * @param request 查询请求（包含分类代码、搜索条件和危害特性过滤条件）
     * @return 记录总数
     */
    Long getCountByCategory(PhysicalPropertySearchRequest request);
} 