package org.gsu.hwtttt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.gsu.hwtttt.common.result.PageResult;
import org.gsu.hwtttt.dto.request.ThermalPropertySearchRequest;
import org.gsu.hwtttt.dto.request.ThermalPropertyUpdateRequest;
import org.gsu.hwtttt.dto.response.SpectrumTypeStatistics;
import org.gsu.hwtttt.dto.response.ThermalPropertyDetail;
import org.gsu.hwtttt.dto.response.ThermalStatisticsResponse;
import org.gsu.hwtttt.dto.response.WasteThermalSummary;
import org.gsu.hwtttt.entity.ThermalProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Module 3: 热力学性质服务接口
 * 支持各种热力学性质子模块的查询功能
 *
 * @author WenXin
 * @date 2025/06/13
 */
public interface ThermalPropertyService extends IService<ThermalProperty> {

    // ==================== Module 3 新增核心方法 ====================

    /**
     * 获取光谱类型统计信息
     *
     * @return 光谱类型统计列表
     */
    List<SpectrumTypeStatistics> getSpectrumTypeStatistics();

    /**
     * 搜索热力学特性（分页）
     *
     * @param spectrumType 光谱类型
     * @param keyword 搜索关键字
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<WasteThermalSummary> searchThermalProperties(String spectrumType, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 高级搜索热力学特性
     *
     * @param request 搜索请求
     * @return 分页结果
     */
    PageResult<WasteThermalSummary> advancedSearchThermalProperties(ThermalPropertySearchRequest request);

    /**
     * 根据ID获取热力学特性详情
     *
     * @param id 热力学特性ID
     * @return 详情信息
     */
    ThermalPropertyDetail getThermalPropertyById(Long id);

    /**
     * 根据危废ID获取热力学特性列表
     *
     * @param wasteId 危废ID
     * @param spectrumType 光谱类型（可选）
     * @return 热力学特性详情列表
     */
    List<ThermalPropertyDetail> getThermalPropertiesByWasteId(Long wasteId, String spectrumType);

    /**
     * 上传热力学图像
     *
     * @param file 图像文件
     * @param wasteId 危废ID
     * @param spectrumType 光谱类型
     * @param testName 测试名称
     * @param testLab 测试实验室
     * @param remark 备注
     * @return 热力学特性记录
     */
    ThermalProperty uploadThermalImage(MultipartFile file, Long wasteId, String spectrumType, String testName, String testLab, String remark);

    /**
     * 删除热力学特性
     *
     * @param id 热力学特性ID
     */
    void deleteThermalProperty(Long id);

    /**
     * 更新热力学特性
     *
     * @param id 热力学特性ID
     * @param request 更新请求
     * @return 更新后的热力学特性
     */
    ThermalProperty updateThermalProperty(Long id, ThermalPropertyUpdateRequest request);

    /**
     * 获取图像资源
     *
     * @param spectrumType 光谱类型
     * @param fileName 文件名
     * @return 图像资源响应
     */
    ResponseEntity<Resource> getImageResource(String spectrumType, String fileName);

    /**
     * 生成图像URL
     *
     * @param spectrumType 光谱类型
     * @param fileName 文件名
     * @return 图像URL
     */
    String getImageUrl(String spectrumType, String fileName);

    // ==================== 文件处理方法 ====================

    /**
     * 生成文件名
     *
     * @param wasteId 危废ID
     * @param spectrumType 光谱类型
     * @param originalFilename 原始文件名
     * @return 生成的文件名
     */
    String generateFileName(Long wasteId, String spectrumType, String originalFilename);

    /**
     * 保存图像文件
     *
     * @param file 文件
     * @param spectrumType 光谱类型
     * @param fileName 文件名
     */
    void saveImageFile(MultipartFile file, String spectrumType, String fileName);

    /**
     * 获取图像资源文件
     *
     * @param spectrumType 光谱类型
     * @param fileName 文件名
     * @return 资源文件
     */
    Resource getImageResourceFile(String spectrumType, String fileName);

    /**
     * 读取图像尺寸
     *
     * @param filePath 文件路径
     * @return 图像对象
     */
    BufferedImage readImageDimensions(String filePath);

    /**
     * 获取下一个序号
     *
     * @param wasteId 危废ID
     * @param spectrumType 光谱类型
     * @return 下一个序号
     */
    int getNextSequenceNumber(Long wasteId, String spectrumType);

    // ==================== Module 3 原有方法 ====================

    /**
     * 根据关键字和分类搜索热力学性质
     *
     * @param keyword 搜索关键字或危废代码
     * @param category 性质分类
     * @return 热力学性质列表
     */
    List<ThermalProperty> searchByKeywordAndCategory(String keyword, String category);

    /**
     * 根据危废ID和分类获取热力学性质
     *
     * @param wasteId 危废ID
     * @param category 性质分类
     * @return 热力学性质列表
     */
    List<ThermalProperty> getByWasteIdAndCategory(Long wasteId, String category);

    /**
     * 根据关键字搜索热力学性质（分页）
     *
     * @param keyword 搜索关键字
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 热力学性质列表
     */
    List<ThermalProperty> searchByKeyword(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 根据危废代码获取热力学性质
     *
     * @param wasteCode 危废代码
     * @return 热力学性质列表
     */
    List<ThermalProperty> getByWasteCode(String wasteCode);

    /**
     * 获取所有热力学性质分类
     *
     * @return 分类列表
     */
    List<String> getAllCategories();

    /**
     * 根据分类获取热力学性质记录
     *
     * @param category 性质分类
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 热力学性质列表
     */
    List<ThermalProperty> getByCategory(String category, Integer pageNum, Integer pageSize);

    /**
     * 获取热力学性质统计信息
     *
     * @return 统计信息
     */
    ThermalStatisticsResponse getStatistics();

    /**
     * 按分类获取统计信息
     *
     * @return 分类统计信息
     */
    Map<String, Object> getStatisticsByCategory();

    /**
     * 根据分类获取文件信息
     *
     * @param category 数据分类
     * @return 文件信息列表
     */
    List<Map<String, Object>> getFileInfoByCategory(String category);

    /**
     * 获取数据完整性统计
     *
     * @return 完整性统计
     */
    Map<String, Object> getDataCompletenessStats();

    // ==================== 原有方法 ====================

    /**
     * 根据危废ID获取热力学特性
     *
     * @param wasteId 危废ID
     * @return 热力学特性列表
     */
    List<ThermalProperty> getByWasteId(Long wasteId);

    /**
     * 根据光谱类型查询热力学特性
     *
     * @param spectrumType 光谱类型
     * @return 热力学特性列表
     */
    List<ThermalProperty> getBySpectrumType(String spectrumType);

    /**
     * 根据危废ID和光谱类型获取热力学特性
     *
     * @param wasteId 危废ID
     * @param spectrumType 光谱类型
     * @return 热力学特性列表
     */
    List<ThermalProperty> getByWasteIdAndSpectrumType(Long wasteId, String spectrumType);

    /**
     * 获取最新的热力学数据
     *
     * @param wasteId 危废ID
     * @param limit 限制数量
     * @return 热力学特性列表
     */
    List<ThermalProperty> getLatestByWasteId(Long wasteId, Integer limit);

    /**
     * 分页查询热力学特性
     *
     * @param current 当前页
     * @param size 页大小
     * @param spectrumType 光谱类型（可选）
     * @param wasteId 危废ID（可选）
     * @return 分页结果
     */
    Page<ThermalProperty> getThermalDataPage(Long current, Long size, String spectrumType, Long wasteId);
    
    /**
     * 通用分页查询方法（兼容旧方法名）
     *
     * @param current 当前页
     * @param size 页大小
     * @param spectrumType 光谱类型（可选）
     * @param wasteId 危废ID（可选）
     * @return 分页结果
     */
    Page<ThermalProperty> getPage(Long current, Long size, String spectrumType, Long wasteId);

    /**
     * 根据温度范围筛选热力学特性
     *
     * @param minTemp 最小温度
     * @param maxTemp 最大温度
     * @return 热力学特性列表
     */
    List<ThermalProperty> getByTemperatureRange(BigDecimal minTemp, BigDecimal maxTemp);

    /**
     * 根据升温速率筛选热力学特性
     *
     * @param heatingRate 升温速率
     * @return 热力学特性列表
     */
    List<ThermalProperty> getByHeatingRate(BigDecimal heatingRate);

    /**
     * 导出热力学数据
     *
     * @param wasteIds 危废ID列表
     * @param spectrumTypes 光谱类型列表
     * @return 导出文件路径
     */
    String exportThermalData(List<Long> wasteIds, List<String> spectrumTypes);

    /**
     * 获取热力学特性统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getThermalStatistics();

    /**
     * 根据实验室统计热力学特性
     *
     * @return 统计结果
     */
    List<Map<String, Object>> getStatisticsByLab();

    /**
     * 根据光谱类型统计热力学特性
     *
     * @return 统计结果
     */
    List<Map<String, Object>> getStatisticsBySpectrumType();

    /**
     * 搜索相似特征峰的危废
     *
     * @param peakValue 特征峰值
     * @return 相似的热力学特性列表
     */
    List<ThermalProperty> searchSimilarPeaks(String peakValue);

    /**
     * 获取支持的光谱类型列表
     *
     * @return 光谱类型列表
     */
    List<String> getSupportedSpectrumTypes();
    
    /**
     * 批量插入热力学特性数据
     *
     * @param properties 热力学特性列表
     * @return 插入成功的数量
     */
    int batchInsert(List<ThermalProperty> properties);

    // ==================== 新增高级分析方法 ====================
    
    /**
     * 根据数据质量等级筛选热力学特性
     *
     * @param qualityGrade 数据质量等级（A, B, C, D）
     * @return 热力学特性列表
     */
    List<ThermalProperty> getByQualityGrade(String qualityGrade);

    /**
     * 获取热力学数据质量分布统计
     *
     * @return 质量分布统计数据
     */
    List<Map<String, Object>> getQualityGradeDistribution();

    /**
     * 根据检测实验室统计热力学数据数量
     *
     * @return 实验室统计数据
     */
    List<Map<String, Object>> getCountByTestLab();

    /**
     * 根据设备型号分组查询热力学特性
     *
     * @param equipmentModel 设备型号
     * @return 分组的热力学特性数据
     */
    Map<String, List<ThermalProperty>> getPropertiesByEquipment(String equipmentModel);

    /**
     * 获取热力学特性完整性报告
     *
     * @param wasteId 危废ID
     * @return 完整性报告
     */
    Map<String, Object> getDataCompletenessReport(Long wasteId);

    /**
     * 根据特征峰数量筛选热力学特性
     *
     * @param minPeakCount 最小特征峰数量
     * @param maxPeakCount 最大特征峰数量
     * @return 热力学特性列表
     */
    List<ThermalProperty> getByPeakCountRange(Integer minPeakCount, Integer maxPeakCount);

    /**
     * 获取热力学数据趋势分析
     *
     * @param wasteIds 危废ID列表
     * @param spectrumType 光谱类型
     * @return 趋势分析数据
     */
    Map<String, Object> getThermalTrendAnalysis(List<Long> wasteIds, String spectrumType);

    /**
     * 验证热力学数据一致性
     *
     * @param wasteId 危废ID
     * @return 一致性检查结果
     */
    List<Map<String, Object>> validateThermalDataConsistency(Long wasteId);

    /**
     * 批量更新热力学数据质量等级
     *
     * @param updates 更新数据 Map<ID, 质量等级>
     * @return 更新成功的数量
     */
    int batchUpdateQualityGrade(Map<Long, String> updates);

    /**
     * 根据测试气氛条件筛选热力学特性
     *
     * @param atmosphere 测试气氛
     * @return 热力学特性列表
     */
    List<ThermalProperty> getByAtmosphere(String atmosphere);

    /**
     * 获取热行为类型分布统计
     *
     * @return 热行为分布统计数据
     */
    List<Map<String, Object>> getThermalBehaviorDistribution();

    /**
     * 查找相似热力学特性
     *
     * @param referenceId 参考热力学特性ID
     * @param threshold 相似度阈值
     * @return 相似的热力学特性列表
     */
    List<ThermalProperty> findSimilarThermalProperties(Long referenceId, BigDecimal threshold);
} 