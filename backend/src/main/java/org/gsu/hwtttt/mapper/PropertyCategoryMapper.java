package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.gsu.hwtttt.entity.PropertyCategory;

import java.util.List;

/**
 * 物性分类表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface PropertyCategoryMapper extends BaseMapper<PropertyCategory> {

    /**
     * 根据分类代码查询分类
     *
     * @param code 分类代码
     * @return 物性分类
     */
    @Select("SELECT * FROM property_categories WHERE code = #{code}")
    PropertyCategory selectByCode(@Param("code") String code);

    /**
     * 查询所有启用的分类
     *
     * @return 启用的分类列表
     */
    @Select("SELECT * FROM property_categories WHERE is_active = true ORDER BY sort_order")
    List<PropertyCategory> selectActiveCategories();

    /**
     * 根据分类名称模糊查询
     *
     * @param name 分类名称关键字
     * @return 分类列表
     */
    @Select("SELECT * FROM property_categories WHERE category_name LIKE CONCAT('%', #{name}, '%') ORDER BY sort_order")
    List<PropertyCategory> selectByNameLike(@Param("name") String name);

    /**
     * 根据父级分类查询子分类
     *
     * @param parentCode 父级分类代码
     * @return 子分类列表
     */
    @Select("SELECT * FROM property_categories WHERE parent_code = #{parentCode} ORDER BY sort_order")
    List<PropertyCategory> selectByParentCode(@Param("parentCode") String parentCode);

    /**
     * 查询所有顶级分类（无父级）
     *
     * @return 顶级分类列表
     */
    @Select("SELECT * FROM property_categories WHERE parent_code IS NULL ORDER BY sort_order")
    List<PropertyCategory> selectTopLevelCategories();

    /**
     * 更新分类状态
     *
     * @param code 分类代码
     * @param isActive 是否启用
     * @return 更新条数
     */
    @Update("UPDATE property_categories SET is_active = #{isActive}, update_time = NOW() WHERE code = #{code}")
    int updateCategoryStatus(@Param("code") String code, @Param("isActive") Boolean isActive);

    /**
     * 统计各分类的属性数量
     *
     * @return 统计结果
     */
    @Select("SELECT pc.code, pc.category_name, COUNT(pp.id) as property_count " +
            "FROM property_categories pc " +
            "LEFT JOIN physical_properties pp ON pc.code = pp.category_code " +
            "GROUP BY pc.code, pc.category_name " +
            "ORDER BY property_count DESC")
    List<CategoryStatistics> getCategoryPropertyCount();

    /**
     * 检查代码是否存在
     *
     * @param code 分类代码
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM property_categories WHERE code = #{code}")
    int existsByCode(@Param("code") String code);

    /**
     * 获取最大排序号
     *
     * @param parentCode 父级分类代码（可为null）
     * @return 最大排序号
     */
    @Select("<script>" +
            "SELECT MAX(sort_order) FROM property_categories " +
            "<where>" +
            "<if test='parentCode != null'> parent_code = #{parentCode} </if>" +
            "<if test='parentCode == null'> parent_code IS NULL </if>" +
            "</where>" +
            "</script>")
    Integer getMaxSortOrder(@Param("parentCode") String parentCode);

    /**
     * 分类统计信息内部类
     */
    static class CategoryStatistics {
        private String code;
        private String categoryName;
        private Integer propertyCount;

        // getter and setter methods
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public Integer getPropertyCount() { return propertyCount; }
        public void setPropertyCount(Integer propertyCount) { this.propertyCount = propertyCount; }
    }
} 