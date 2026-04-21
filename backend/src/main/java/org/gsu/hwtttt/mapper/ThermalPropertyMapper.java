package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.gsu.hwtttt.entity.ThermalProperty;

import java.util.List;
import java.util.Map;

/**
 * 热力学特性数据表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Mapper
public interface ThermalPropertyMapper extends BaseMapper<ThermalProperty> {
    
    /**
     * Get spectrum type statistics
     */
    @Select("SELECT spectrum_type, COUNT(DISTINCT waste_id) as waste_count, COUNT(*) as test_count " +
            "FROM thermal_properties " +
            "GROUP BY spectrum_type")
    List<Map<String, Object>> getSpectrumTypeStatistics();
    
    /**
     * Search thermal properties with waste info
     */
    @Select("SELECT tp.*, hw.waste_code, hw.waste_name, hw.source_unit " +
            "FROM thermal_properties tp " +
            "LEFT JOIN hazardous_waste hw ON tp.waste_id = hw.id " +
            "WHERE (#{spectrumType} IS NULL OR tp.spectrum_type = #{spectrumType}) " +
            "AND (#{keyword} IS NULL OR hw.waste_code LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR hw.waste_name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR hw.source_unit LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND hw.deleted = 0 " +
            "ORDER BY tp.test_date DESC")
    List<Map<String, Object>> searchThermalPropertiesWithWasteInfo(
        @Param("spectrumType") String spectrumType, 
        @Param("keyword") String keyword
    );
    
    /**
     * Get thermal properties by waste ID
     */
    @Select("SELECT * FROM thermal_properties " +
            "WHERE waste_id = #{wasteId} " +
            "AND (#{spectrumType} IS NULL OR spectrum_type = #{spectrumType}) " +
            "ORDER BY spectrum_type, sequence_no")
    List<ThermalProperty> getThermalPropertiesByWasteId(
        @Param("wasteId") Long wasteId, 
        @Param("spectrumType") String spectrumType
    );
    
    /**
     * Get next sequence number
     */
    @Select("SELECT COALESCE(MAX(sequence_no), 0) + 1 " +
            "FROM thermal_properties " +
            "WHERE waste_id = #{wasteId} AND spectrum_type = #{spectrumType}")
    Integer getNextSequenceNumber(
        @Param("wasteId") Long wasteId, 
        @Param("spectrumType") String spectrumType
    );
    
    /**
     * Get thermal properties with waste info by ID
     */
    @Select("SELECT tp.*, hw.waste_code, hw.waste_name, hw.source_unit " +
            "FROM thermal_properties tp " +
            "LEFT JOIN hazardous_waste hw ON tp.waste_id = hw.id " +
            "WHERE tp.id = #{id}")
    Map<String, Object> getThermalPropertyWithWasteInfo(@Param("id") Long id);
    
    /**
     * Count by spectrum type
     */
    @Select("SELECT COUNT(*) FROM thermal_properties WHERE spectrum_type = #{spectrumType}")
    Integer countBySpectrumType(@Param("spectrumType") String spectrumType);
    
    /**
     * Get waste thermal summary
     */
    @Select("SELECT hw.id as waste_id, hw.waste_code, hw.waste_name, hw.source_unit, " +
            "COUNT(tp.id) as total_images, MAX(tp.test_date) as latest_test_date " +
            "FROM hazardous_waste hw " +
            "LEFT JOIN thermal_properties tp ON hw.id = tp.waste_id " +
            "WHERE hw.deleted = 0 " +
            "GROUP BY hw.id, hw.waste_code, hw.waste_name, hw.source_unit " +
            "HAVING total_images > 0 " +
            "ORDER BY latest_test_date DESC")
    List<Map<String, Object>> getWasteThermalSummary();
    
    /**
     * Search thermal properties with keyword
     */
    @Select("SELECT tp.*, hw.waste_code, hw.waste_name, hw.source_unit " +
            "FROM thermal_properties tp " +
            "LEFT JOIN hazardous_waste hw ON tp.waste_id = hw.id " +
            "WHERE (#{keyword} IS NULL OR hw.waste_code LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR hw.waste_name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR tp.test_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND hw.deleted = 0 " +
            "ORDER BY tp.test_date DESC")
    List<Map<String, Object>> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Get thermal properties by spectrum type
     */
    @Select("SELECT tp.*, hw.waste_code, hw.waste_name, hw.source_unit " +
            "FROM thermal_properties tp " +
            "LEFT JOIN hazardous_waste hw ON tp.waste_id = hw.id " +
            "WHERE tp.spectrum_type = #{spectrumType} " +
            "AND hw.deleted = 0 " +
            "ORDER BY tp.test_date DESC")
    List<Map<String, Object>> getThermalPropertiesBySpectrumType(@Param("spectrumType") String spectrumType);
    
    /**
     * Check if image file name exists
     */
    @Select("SELECT COUNT(*) FROM thermal_properties WHERE image_file_name = #{imageFileName}")
    Integer countByImageFileName(@Param("imageFileName") String imageFileName);
    
    /**
     * Get thermal property by image file name
     */
    @Select("SELECT * FROM thermal_properties WHERE image_file_name = #{imageFileName}")
    ThermalProperty selectByImageFileName(@Param("imageFileName") String imageFileName);
    
    /**
     * Update image file info
     */
    @Update("UPDATE thermal_properties SET " +
            "image_file_name = #{imageFileName}, " +
            "image_file_path = #{imageFilePath}, " +
            "image_file_size = #{imageFileSize}, " +
            "image_mime_type = #{imageMimeType}, " +
            "image_width = #{imageWidth}, " +
            "image_height = #{imageHeight}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    Integer updateImageFileInfo(
        @Param("id") Long id,
        @Param("imageFileName") String imageFileName,
        @Param("imageFilePath") String imageFilePath,
        @Param("imageFileSize") Long imageFileSize,
        @Param("imageMimeType") String imageMimeType,
        @Param("imageWidth") Integer imageWidth,
        @Param("imageHeight") Integer imageHeight
    );
    
    /**
     * Get distinct spectrum types
     */
    @Select("SELECT DISTINCT spectrum_type FROM thermal_properties ORDER BY spectrum_type")
    List<String> getDistinctSpectrumTypes();
    
    /**
     * Get distinct quality grades
     */
    @Select("SELECT DISTINCT quality_grade FROM thermal_properties ORDER BY quality_grade")
    List<String> getDistinctQualityGrades();
    
    /**
     * Get thermal properties count by waste ID
     */
    @Select("SELECT COUNT(*) FROM thermal_properties WHERE waste_id = #{wasteId}")
    Integer countByWasteId(@Param("wasteId") Long wasteId);
    
    /**
     * Delete thermal properties by waste ID
     */
    @Delete("DELETE FROM thermal_properties WHERE waste_id = #{wasteId}")
    Integer deleteByWasteId(@Param("wasteId") Long wasteId);
    
    /**
     * Get thermal properties with pagination support
     */
    @Select("SELECT tp.*, hw.waste_code, hw.waste_name, hw.source_unit " +
            "FROM thermal_properties tp " +
            "LEFT JOIN hazardous_waste hw ON tp.waste_id = hw.id " +
            "WHERE hw.deleted = 0 " +
            "ORDER BY tp.create_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Map<String, Object>> getThermalPropertiesWithPagination(
        @Param("offset") Integer offset,
        @Param("size") Integer size
    );
    
    /**
     * Count total thermal properties
     */
    @Select("SELECT COUNT(*) FROM thermal_properties tp " +
            "LEFT JOIN hazardous_waste hw ON tp.waste_id = hw.id " +
            "WHERE hw.deleted = 0")
    Integer countTotal();
} 