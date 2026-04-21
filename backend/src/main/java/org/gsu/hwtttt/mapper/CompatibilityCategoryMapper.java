package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.gsu.hwtttt.entity.CompatibilityCategory;

import java.util.List;

/**
 * 危废相容性41类主数据字典Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface CompatibilityCategoryMapper extends BaseMapper<CompatibilityCategory> {

    /**
     * 根据索引范围查询相容性类别
     *
     * @param startIdx 开始索引
     * @param endIdx 结束索引
     * @return 相容性类别列表
     */
    @Select("SELECT * FROM compatibility_categories WHERE idx BETWEEN #{startIdx} AND #{endIdx} ORDER BY idx")
    List<CompatibilityCategory> selectByIdxRange(@Param("startIdx") Integer startIdx, @Param("endIdx") Integer endIdx);

    /**
     * 根据类别代码查询
     *
     * @param categoryCode 类别代码
     * @return 相容性类别
     */
    @Select("SELECT * FROM compatibility_categories WHERE category_code = #{categoryCode}")
    CompatibilityCategory selectByCategoryCode(@Param("categoryCode") String categoryCode);

    /**
     * 查询所有启用的类别（按索引排序）
     *
     * @return 相容性类别列表
     */
    @Select("SELECT * FROM compatibility_categories ORDER BY idx")
    List<CompatibilityCategory> selectAllOrderByIdx();

    /**
     * 根据中文名称模糊查询
     *
     * @param nameCn 中文名称关键字
     * @return 相容性类别列表
     */
    @Select("SELECT * FROM compatibility_categories WHERE category_name_cn LIKE CONCAT('%', #{nameCn}, '%') ORDER BY idx")
    List<CompatibilityCategory> selectByNameCnLike(@Param("nameCn") String nameCn);

    /**
     * 根据英文名称模糊查询
     *
     * @param nameEn 英文名称关键字
     * @return 相容性类别列表
     */
    @Select("SELECT * FROM compatibility_categories WHERE category_name_en LIKE CONCAT('%', #{nameEn}, '%') ORDER BY idx")
    List<CompatibilityCategory> selectByNameEnLike(@Param("nameEn") String nameEn);

    /**
     * 获取最大索引值
     *
     * @return 最大索引值
     */
    @Select("SELECT MAX(idx) FROM compatibility_categories")
    Integer getMaxIdx();

    /**
     * 检查类别代码是否存在
     *
     * @param categoryCode 类别代码
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM compatibility_categories WHERE category_code = #{categoryCode}")
    int existsByCategoryCode(@Param("categoryCode") String categoryCode);

    /**
     * 检查索引是否存在
     *
     * @param idx 索引值
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM compatibility_categories WHERE idx = #{idx}")
    int existsByIdx(@Param("idx") Integer idx);

    /**
     * 根据分类代码查询分类
     *
     * @param code 分类代码
     * @return 相容性分类
     */
    @Select("SELECT * FROM compatibility_categories WHERE code = #{code}")
    CompatibilityCategory selectByCode(@Param("code") String code);

    /**
     * 查询所有启用的分类
     *
     * @return 启用的分类列表
     */
    @Select("SELECT * FROM compatibility_categories WHERE is_active = true ORDER BY sort_order")
    List<CompatibilityCategory> selectActiveCategories();

    /**
     * 根据分类名称模糊查询
     *
     * @param name 分类名称关键字
     * @return 分类列表
     */
    @Select("SELECT * FROM compatibility_categories WHERE category_name_cn LIKE CONCAT('%', #{name}, '%') OR category_name_en LIKE CONCAT('%', #{name}, '%') ORDER BY sort_order")
    List<CompatibilityCategory> selectByNameLike(@Param("name") String name);

    /**
     * 查询所有主要类别
     *
     * @return 主要类别列表
     */
    @Select("SELECT * FROM compatibility_categories WHERE is_major_category = true ORDER BY sort_order")
    List<CompatibilityCategory> selectMajorCategories();

    /**
     * 更新分类状态
     *
     * @param code 分类代码
     * @param isActive 是否启用
     * @return 更新条数
     */
    @Update("UPDATE compatibility_categories SET is_active = #{isActive}, update_time = NOW() WHERE code = #{code}")
    int updateCategoryStatus(@Param("code") String code, @Param("isActive") Boolean isActive);

    /**
     * 统计各分类的危废数量
     *
     * @return 统计结果
     */
    @Select("SELECT cc.code, cc.category_name_cn, COUNT(hw.id) as waste_count " +
            "FROM compatibility_categories cc " +
            "LEFT JOIN hazardous_waste hw ON cc.code = hw.compatibility_category_code " +
            "GROUP BY cc.code, cc.category_name_cn " +
            "ORDER BY waste_count DESC")
    List<CategoryStatistics> getCategoryWasteCount();

    /**
     * 检查代码是否存在
     *
     * @param code 分类代码
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM compatibility_categories WHERE code = #{code}")
    int existsByCode(@Param("code") String code);

    /**
     * 分类统计信息内部类
     */
    static class CategoryStatistics {
        private String code;
        private String categoryNameCn;
        private Integer wasteCount;

        // getter and setter methods
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getCategoryNameCn() { return categoryNameCn; }
        public void setCategoryNameCn(String categoryNameCn) { this.categoryNameCn = categoryNameCn; }
        public Integer getWasteCount() { return wasteCount; }
        public void setWasteCount(Integer wasteCount) { this.wasteCount = wasteCount; }
    }
} 