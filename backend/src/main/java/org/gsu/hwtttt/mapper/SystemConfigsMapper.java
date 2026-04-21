package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.gsu.hwtttt.entity.SystemConfigs;
import lombok.Data;

import java.util.List;

/**
 * 系统参数配置表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface SystemConfigsMapper extends BaseMapper<SystemConfigs> {

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 系统配置
     */
    @Select("SELECT * FROM system_configs WHERE config_key = #{configKey}")
    SystemConfigs selectByConfigKey(@Param("configKey") String configKey);

    /**
     * 根据配置组和配置键查询配置
     *
     * @param configGroup 配置组
     * @param configKey 配置键
     * @return 系统配置
     */
    @Select("SELECT * FROM system_configs WHERE config_group = #{configGroup} AND config_key = #{configKey}")
    SystemConfigs selectByGroupAndKey(@Param("configGroup") String configGroup, @Param("configKey") String configKey);

    /**
     * 根据配置组查询配置列表
     *
     * @param configGroup 配置组
     * @return 配置列表
     */
    @Select("SELECT * FROM system_configs WHERE config_group = #{configGroup} ORDER BY sort_order")
    List<SystemConfigs> selectByConfigGroup(@Param("configGroup") String configGroup);

    /**
     * 查询所有非只读配置
     *
     * @return 可编辑配置列表
     */
    @Select("SELECT * FROM system_configs WHERE is_readonly = false ORDER BY config_group, sort_order")
    List<SystemConfigs> selectEditableConfigs();

    /**
     * 根据值类型查询配置
     *
     * @param valueType 值类型
     * @return 配置列表
     */
    @Select("SELECT * FROM system_configs WHERE value_type = #{valueType} ORDER BY config_group, sort_order")
    List<SystemConfigs> selectByValueType(@Param("valueType") String valueType);

    /**
     * 更新配置值
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 更新条数
     */
    @Update("UPDATE system_configs SET config_value = #{configValue}, update_time = NOW() WHERE config_key = #{configKey}")
    int updateConfigValue(@Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 更新配置值（根据组和键）
     *
     * @param configGroup 配置组
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 更新条数
     */
    @Update("UPDATE system_configs SET config_value = #{configValue}, update_time = NOW() WHERE config_group = #{configGroup} AND config_key = #{configKey}")
    int updateConfigValueByGroupAndKey(@Param("configGroup") String configGroup, @Param("configKey") String configKey, @Param("configValue") String configValue);

    /**
     * 批量更新只读状态
     *
     * @param configKeys 配置键列表
     * @param isReadonly 是否只读
     * @return 更新条数
     */
    @Update("<script>" +
            "UPDATE system_configs SET is_readonly = #{isReadonly}, update_time = NOW() WHERE config_key IN " +
            "<foreach collection='configKeys' item='key' open='(' close=')' separator=','>" +
            "#{key}" +
            "</foreach>" +
            "</script>")
    int batchUpdateReadonlyStatus(@Param("configKeys") List<String> configKeys, @Param("isReadonly") Boolean isReadonly);

    /**
     * 获取配置组统计信息
     *
     * @return 配置组统计
     */
    @Select("SELECT config_group, COUNT(*) as count, " +
            "SUM(CASE WHEN is_readonly = true THEN 1 ELSE 0 END) as readonly_count " +
            "FROM system_configs GROUP BY config_group ORDER BY config_group")
    List<ConfigGroupStatistics> getConfigGroupStatistics();

    /**
     * 获取值类型分布统计
     *
     * @return 值类型统计
     */
    @Select("SELECT value_type, COUNT(*) as count FROM system_configs GROUP BY value_type ORDER BY count DESC")
    List<ValueTypeStatistics> getValueTypeStatistics();

    /**
     * 检查配置键是否存在
     *
     * @param configGroup 配置组
     * @param configKey 配置键
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM system_configs WHERE config_group = #{configGroup} AND config_key = #{configKey}")
    int existsByGroupAndKey(@Param("configGroup") String configGroup, @Param("configKey") String configKey);

    /**
     * 配置组统计信息内部类
     */
    @Data
    class ConfigGroupStatistics {
        private String configGroup;
        private Integer count;
        private Integer readonlyCount;
    }

    /**
     * 值类型统计内部类
     */
    @Data
    class ValueTypeStatistics {
        private String valueType;
        private Integer count;
    }
} 