package org.gsu.hwtttt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.common.exception.BusinessException;
import org.gsu.hwtttt.common.result.PageResult;
import org.gsu.hwtttt.common.result.ResultCode;
import org.gsu.hwtttt.constant.QualityGrade;
import org.gsu.hwtttt.constant.SpectrumType;
import org.gsu.hwtttt.dto.request.ThermalPropertySearchRequest;
import org.gsu.hwtttt.dto.request.ThermalPropertyUpdateRequest;
import org.gsu.hwtttt.dto.response.SpectrumTypeStatistics;
import org.gsu.hwtttt.dto.response.ThermalImageInfo;
import org.gsu.hwtttt.dto.response.ThermalPropertyDetail;
import org.gsu.hwtttt.dto.response.ThermalStatisticsResponse;
import org.gsu.hwtttt.dto.response.WasteThermalSummary;
import org.gsu.hwtttt.entity.HazardousWaste;
import org.gsu.hwtttt.entity.ThermalProperty;
import org.gsu.hwtttt.mapper.HazardousWasteMapper;
import org.gsu.hwtttt.mapper.ThermalPropertyMapper;
import org.gsu.hwtttt.service.ThermalPropertyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThermalPropertyServiceImpl extends ServiceImpl<ThermalPropertyMapper, ThermalProperty> 
        implements ThermalPropertyService {

    private final ThermalPropertyMapper thermalPropertyMapper;
    private final HazardousWasteMapper hazardousWasteMapper;

    @Value("${app.thermal.image.storage-path:src/main/resources/static/thermal-images/}")
    private String imageStoragePath;

    @Value("${app.thermal.image.base-url:/api/v3/thermal-properties/image/}")
    private String imageBaseUrl;

    @Value("${app.thermal.image.max-size:10485760}")
    private long maxFileSize;

    @Value("${app.thermal.image.allowed-types:image/jpeg,image/png,image/jpg}")
    private String allowedTypes;

    @Override
    public List<SpectrumTypeStatistics> getSpectrumTypeStatistics() {
        log.info("获取光谱类型统计信息");
        
        List<SpectrumTypeStatistics> statistics = new ArrayList<>();
        
        for (SpectrumType type : SpectrumType.values()) {
            LambdaQueryWrapper<ThermalProperty> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ThermalProperty::getSpectrumType, type);
            
            long testCount = count(wrapper);
            
            LambdaQueryWrapper<ThermalProperty> wasteWrapper = new LambdaQueryWrapper<>();
            wasteWrapper.eq(ThermalProperty::getSpectrumType, type)
                       .select(ThermalProperty::getWasteId)
                       .groupBy(ThermalProperty::getWasteId);
            long wasteCount = list(wasteWrapper).size();
            
            SpectrumTypeStatistics stat = SpectrumTypeStatistics.builder()
                    .spectrumType(type.name())
                    .typeName(getSpectrumTypeName(type))
                    .typeNameEn(type.name())
                    .wasteCount((int) wasteCount)
                    .testCount((int) testCount)
                    .icon(getSpectrumTypeIcon(type))
                    .description(getSpectrumTypeDescription(type))
                    .build();
            
            statistics.add(stat);
        }
        
        return statistics;
    }

    @Override
    public PageResult<WasteThermalSummary> searchThermalProperties(String spectrumType, String keyword, Integer pageNum, Integer pageSize) {
        log.info("搜索热力学特性，光谱类型: {}, 关键字: {}, 页码: {}, 页大小: {}", spectrumType, keyword, pageNum, pageSize);
        
        ThermalPropertySearchRequest request = new ThermalPropertySearchRequest();
        if (StringUtils.hasText(spectrumType)) {
            request.setSpectrumType(SpectrumType.fromString(spectrumType));
        }
        request.setTestNameKeyword(keyword);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        
        return advancedSearchThermalProperties(request);
    }

    @Override
    public PageResult<WasteThermalSummary> advancedSearchThermalProperties(ThermalPropertySearchRequest request) {
        log.info("高级搜索热力学特性，请求参数: {}", request);
        
        LambdaQueryWrapper<ThermalProperty> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getSpectrumType() != null) {
            wrapper.eq(ThermalProperty::getSpectrumType, request.getSpectrumType());
        }
        
        if (request.getSpectrumTypes() != null && !request.getSpectrumTypes().isEmpty()) {
            wrapper.in(ThermalProperty::getSpectrumType, request.getSpectrumTypes());
        }
        
        if (request.getQualityGrade() != null) {
            wrapper.eq(ThermalProperty::getQualityGrade, request.getQualityGrade());
        }
        
        if (request.getQualityGrades() != null && !request.getQualityGrades().isEmpty()) {
            wrapper.in(ThermalProperty::getQualityGrade, request.getQualityGrades());
        }
        
        if (StringUtils.hasText(request.getTestLabKeyword())) {
            wrapper.like(ThermalProperty::getTestLab, request.getTestLabKeyword());
        }
        
        if (StringUtils.hasText(request.getEquipmentModelKeyword())) {
            wrapper.like(ThermalProperty::getEquipmentModel, request.getEquipmentModelKeyword());
        }
        
        if (StringUtils.hasText(request.getOperatorKeyword())) {
            wrapper.like(ThermalProperty::getOperator, request.getOperatorKeyword());
        }
        
        if (StringUtils.hasText(request.getThermalBehaviorKeyword())) {
            wrapper.like(ThermalProperty::getThermalBehavior, request.getThermalBehaviorKeyword());
        }
        
        if (request.getTestDateStart() != null) {
            wrapper.ge(ThermalProperty::getTestDate, request.getTestDateStart());
        }
        
        if (request.getTestDateEnd() != null) {
            wrapper.le(ThermalProperty::getTestDate, request.getTestDateEnd());
        }
        
        if (request.getWasteId() != null) {
            wrapper.eq(ThermalProperty::getWasteId, request.getWasteId());
        }
        
        if (request.getWasteIds() != null && !request.getWasteIds().isEmpty()) {
            wrapper.in(ThermalProperty::getWasteId, request.getWasteIds());
        }
        
        if (StringUtils.hasText(request.getTestNameKeyword())) {
            wrapper.and(w -> {
                w.like(ThermalProperty::getTestName, request.getTestNameKeyword())
                 .or().like(ThermalProperty::getRemark, request.getTestNameKeyword());
                
                if (request.getWasteId() == null && (request.getWasteIds() == null || request.getWasteIds().isEmpty())) {
                    List<HazardousWaste> wastes = hazardousWasteMapper.searchByKeyword(request.getTestNameKeyword());
                    List<Long> wasteIds = wastes.stream().map(HazardousWaste::getId).collect(Collectors.toList());
                    
                    if (!wasteIds.isEmpty()) {
                        w.or().in(ThermalProperty::getWasteId, wasteIds);
                    }
                }
            });
        }
        
        if (StringUtils.hasText(request.getOrderBy())) {
            if ("desc".equalsIgnoreCase(request.getOrderDirection())) {
                wrapper.orderByDesc(ThermalProperty::getCreateTime);
            } else {
                wrapper.orderByAsc(ThermalProperty::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(ThermalProperty::getCreateTime);
        }
        
        Page<ThermalProperty> page = new Page<>(
            request.getPageNum() != null ? request.getPageNum() : 1,
            request.getPageSize() != null ? request.getPageSize() : 10
        );
        
        Page<ThermalProperty> resultPage = page(page, wrapper);
        
        Map<Long, List<ThermalProperty>> groupedByWaste = resultPage.getRecords()
                .stream()
                .collect(Collectors.groupingBy(ThermalProperty::getWasteId));
        
        List<WasteThermalSummary> summaries = groupedByWaste.entrySet().stream()
                .map(entry -> convertToWasteThermalSummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        return new PageResult<>(
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getTotal(),
                summaries
        );
    }

    @Override
    public ThermalPropertyDetail getThermalPropertyById(Long id) {
        log.info("根据ID获取热力学特性详情，ID: {}", id);
        
        ThermalProperty property = getById(id);
        if (property == null) {
            throw new BusinessException(ResultCode.THERMAL_PROPERTY_NOT_FOUND, "热力学特性不存在");
        }
        
        return convertToThermalPropertyDetail(property);
    }

    @Override
    public List<ThermalPropertyDetail> getThermalPropertiesByWasteId(Long wasteId, String spectrumType) {
        log.info("根据危废ID获取热力学特性列表，危废ID: {}, 光谱类型: {}", wasteId, spectrumType);
        
        LambdaQueryWrapper<ThermalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThermalProperty::getWasteId, wasteId);
        
        if (StringUtils.hasText(spectrumType)) {
            wrapper.eq(ThermalProperty::getSpectrumType, SpectrumType.fromString(spectrumType));
        }
        
        wrapper.orderByDesc(ThermalProperty::getCreateTime);
        
        List<ThermalProperty> properties = list(wrapper);
        return properties.stream()
                .map(this::convertToThermalPropertyDetail)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ThermalProperty uploadThermalImage(MultipartFile file, Long wasteId, String spectrumType, String testName, String testLab, String remark) {
        log.info("上传热力学图像，危废ID: {}, 光谱类型: {}, 测试名称: {}", wasteId, spectrumType, testName);
        
        validateUploadParameters(file, wasteId, spectrumType);
        
        HazardousWaste waste = hazardousWasteMapper.selectById(wasteId);
        if (waste == null) {
            throw new BusinessException(ResultCode.WASTE_NOT_FOUND, "危废信息不存在");
        }
        
        String fileName = generateFileName(wasteId, spectrumType, file.getOriginalFilename());
        saveImageFile(file, spectrumType, fileName);
        
        int sequenceNo = getNextSequenceNumber(wasteId, spectrumType);
        
        BufferedImage image = readImageDimensions(getImageFilePath(spectrumType, fileName));
        
        ThermalProperty thermalProperty = new ThermalProperty();
        thermalProperty.setWasteId(wasteId);
        thermalProperty.setSpectrumType(SpectrumType.fromString(spectrumType));
        thermalProperty.setTestName(StringUtils.hasText(testName) ? testName : generateDefaultTestName(waste, spectrumType));
        thermalProperty.setImageFileName(fileName);
        thermalProperty.setImageFileSize(file.getSize());
        thermalProperty.setImageMimeType(file.getContentType());
        thermalProperty.setImageFilePath(getImageUrl(spectrumType, fileName));
        
        if (image != null) {
            thermalProperty.setImageWidth(image.getWidth());
            thermalProperty.setImageHeight(image.getHeight());
        }
        
        thermalProperty.setTestDate(new Date());
        thermalProperty.setTestLab(testLab);
        thermalProperty.setSequenceNo(sequenceNo);
        thermalProperty.setRemark(remark);
        thermalProperty.setCreateTime(new Date());
        thermalProperty.setUpdateTime(new Date());
        
        save(thermalProperty);
        
        log.info("热力学图像上传成功，ID: {}, 文件名: {}", thermalProperty.getId(), fileName);
        return thermalProperty;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThermalProperty(Long id) {
        log.info("删除热力学特性，ID: {}", id);
        
        ThermalProperty property = getById(id);
        if (property == null) {
            throw new BusinessException(ResultCode.THERMAL_PROPERTY_NOT_FOUND, "热力学特性不存在");
        }
        
        if (StringUtils.hasText(property.getImageFileName()) && property.getSpectrumType() != null) {
            deleteImageFile(property.getSpectrumType().name(), property.getImageFileName());
        }
        
        removeById(id);
        
        log.info("热力学特性删除成功，ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ThermalProperty updateThermalProperty(Long id, ThermalPropertyUpdateRequest request) {
        log.info("更新热力学特性，ID: {}, 请求参数: {}", id, request);
        
        ThermalProperty property = getById(id);
        if (property == null) {
            throw new BusinessException(ResultCode.THERMAL_PROPERTY_NOT_FOUND, "热力学特性不存在");
        }
        
        if (StringUtils.hasText(request.getTestName())) {
            property.setTestName(request.getTestName());
        }
        if (StringUtils.hasText(request.getThermalBehavior())) {
            property.setThermalBehavior(request.getThermalBehavior());
        }
        if (StringUtils.hasText(request.getTestConditions())) {
            property.setTestConditions(request.getTestConditions());
        }
        if (StringUtils.hasText(request.getTemperatureRange())) {
            property.setTemperatureRange(request.getTemperatureRange());
        }
        if (request.getHeatingRate() != null) {
            property.setHeatingRate(request.getHeatingRate());
        }
        if (StringUtils.hasText(request.getAtmosphere())) {
            property.setAtmosphere(request.getAtmosphere());
        }
        if (request.getSampleMass() != null) {
            property.setSampleMass(request.getSampleMass());
        }
        if (request.getTestDate() != null) {
            property.setTestDate(convertToDate(request.getTestDate()));
        }
        if (StringUtils.hasText(request.getTestLab())) {
            property.setTestLab(request.getTestLab());
        }
        if (StringUtils.hasText(request.getEquipmentModel())) {
            property.setEquipmentModel(request.getEquipmentModel());
        }
        if (StringUtils.hasText(request.getOperator())) {
            property.setOperator(request.getOperator());
        }
        if (StringUtils.hasText(request.getQualityGrade())) {
            property.setQualityGrade(QualityGrade.fromCode(request.getQualityGrade()));
        }
        if (StringUtils.hasText(request.getRemark())) {
            property.setRemark(request.getRemark());
        }
        
        property.setUpdateTime(new Date());
        
        updateById(property);
        
        log.info("热力学特性更新成功，ID: {}", id);
        return property;
    }

    @Override
    public ResponseEntity<Resource> getImageResource(String spectrumType, String fileName) {
        log.info("获取热力学图像，光谱类型: {}, 文件名: {}", spectrumType, fileName);
        
        try {
            Resource resource = getImageResourceFile(spectrumType, fileName);
            if (!resource.exists()) {
                throw new BusinessException(ResultCode.FILE_NOT_FOUND, "图像文件不存在");
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
            
            String contentType = getContentType(fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("获取图像文件失败", e);
            throw new BusinessException(ResultCode.FILE_NOT_FOUND, "图像文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public String getImageUrl(String spectrumType, String fileName) {
        return imageBaseUrl + spectrumType + "/" + fileName;
    }

    @Override
    public String generateFileName(Long wasteId, String spectrumType, String originalFilename) {
        HazardousWaste waste = hazardousWasteMapper.selectById(wasteId);
        String wasteInfo = waste != null ? waste.getSourceUnit() + "-" + waste.getWasteName() : "未知危废";
        
        int sequenceNo = getNextSequenceNumber(wasteId, spectrumType);
        
        String extension = getFileExtension(originalFilename);
        
        return String.format("%s-%s-%03d.%s", spectrumType, wasteInfo, sequenceNo, extension);
    }

    @Override
    public void saveImageFile(MultipartFile file, String spectrumType, String fileName) {
        try {
            Path directoryPath = Paths.get(getImageDirectoryPath(spectrumType));
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
            
            Path filePath = directoryPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("图像文件保存成功: {}", filePath);
            
        } catch (IOException e) {
            log.error("图像文件保存失败", e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR, "图像文件保存失败: " + e.getMessage());
        }
    }

    @Override
    public Resource getImageResourceFile(String spectrumType, String fileName) {
        String filePath = getImageFilePath(spectrumType, fileName);
        return new FileSystemResource(filePath);
    }

    @Override
    public BufferedImage readImageDimensions(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                log.warn("图像文件不存在: {}", filePath);
                return null;
            }
            return ImageIO.read(file);
        } catch (IOException e) {
            log.error("读取图像尺寸失败: {}", filePath, e);
            return null;
        }
    }

    @Override
    public int getNextSequenceNumber(Long wasteId, String spectrumType) {
        return thermalPropertyMapper.getNextSequenceNumber(wasteId, spectrumType);
    }

    private String getSpectrumTypeName(SpectrumType type) {
        switch (type) {
            case FTIR: return "FTIR红外光谱";
            case TG_DSC: return "TGA-DSC热分析";
            case XRF: return "X射线荧光光谱";
            case GC_MS: return "气相色谱-质谱联用";
            default: return type.getName();
        }
    }

    private String getSpectrumTypeIcon(SpectrumType type) {
        switch (type) {
            case FTIR: return "ftir-icon";
            case TG_DSC: return "tg-dsc-icon";
            case XRF: return "xrf-icon";
            case GC_MS: return "gc-ms-icon";
            default: return "default-icon";
        }
    }

    private String getSpectrumTypeDescription(SpectrumType type) {
        switch (type) {
            case FTIR: return "用于识别分子功能基团";
            case TG_DSC: return "分析热稳定性和相变";
            case XRF: return "元素组成定性定量分析";
            case GC_MS: return "有机化合物定性定量分析";
            default: return "光谱分析技术";
        }
    }

    private WasteThermalSummary convertToWasteThermalSummary(Long wasteId, List<ThermalProperty> properties) {
        HazardousWaste waste = hazardousWasteMapper.selectById(wasteId);
        
        List<ThermalImageInfo> imageInfos = properties.stream()
                .map(this::convertToThermalImageInfo)
                .collect(Collectors.toList());
        
        Map<String, Long> typeCount = properties.stream()
                .collect(Collectors.groupingBy(p -> p.getSpectrumType().name(), Collectors.counting()));
        
        return WasteThermalSummary.builder()
                .wasteId(wasteId)
                .wasteCode(waste != null ? waste.getWasteCode() : null)
                .wasteName(waste != null ? waste.getWasteName() : null)
                .sourceUnit(waste != null ? waste.getSourceUnit() : null)
                .totalImages(properties.size())
                .latestTestDate(properties.stream().map(ThermalProperty::getTestDate).filter(Objects::nonNull).max(Date::compareTo).map(this::convertToLocalDate).orElse(null))
                .images(imageInfos)
                .build();
    }

    private ThermalImageInfo convertToThermalImageInfo(ThermalProperty property) {
        return ThermalImageInfo.builder()
                .id(property.getId())
                .spectrumType(property.getSpectrumType() != null ? property.getSpectrumType().name() : "")
                .fileName(property.getImageFileName())
                .filePath(property.getImageFilePath())
                .testName(property.getTestName())
                .testDate(convertToLocalDate(property.getTestDate()))
                .testLab(property.getTestLab())
                .qualityGrade(property.getQualityGrade() != null ? property.getQualityGrade().name() : "")
                .imageUrl(getImageUrl(property.getSpectrumType().name(), property.getImageFileName()))
                .fileSize(property.getImageFileSize())
                .remark(property.getRemark())
                .build();
    }

    private ThermalPropertyDetail convertToThermalPropertyDetail(ThermalProperty property) {
        HazardousWaste waste = hazardousWasteMapper.selectById(property.getWasteId());
        
        return ThermalPropertyDetail.builder()
                .id(property.getId())
                .wasteId(property.getWasteId())
                .wasteCode(waste != null ? waste.getWasteCode() : null)
                .wasteName(waste != null ? waste.getWasteName() : null)
                .sourceUnit(waste != null ? waste.getSourceUnit() : null)
                .spectrumType(property.getSpectrumType() != null ? property.getSpectrumType().name() : "")
                .testName(property.getTestName())
                .imageUrl(getImageUrl(property.getSpectrumType().name(), property.getImageFileName()))
                .fileName(property.getImageFileName())
                .fileSize(property.getImageFileSize())
                .mimeType(property.getImageMimeType())
                .width(property.getImageWidth())
                .height(property.getImageHeight())
                .thermalBehavior(property.getThermalBehavior())
                .testConditions(property.getTestConditions())
                .temperatureRange(property.getTemperatureRange())
                .heatingRate(property.getHeatingRate())
                .atmosphere(property.getAtmosphere())
                .sampleMass(property.getSampleMass())
                .testDate(convertToLocalDate(property.getTestDate()))
                .testLab(property.getTestLab())
                .equipmentModel(property.getEquipmentModel())
                .operator(property.getOperator())
                .qualityGrade(property.getQualityGrade() != null ? property.getQualityGrade().name() : "")
                .sequenceNo(property.getSequenceNo())
                .remark(property.getRemark())
                .createTime(convertToLocalDateTime(property.getCreateTime()))
                .updateTime(convertToLocalDateTime(property.getUpdateTime()))
                .build();
    }

    private void validateUploadParameters(MultipartFile file, Long wasteId, String spectrumType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图像文件不能为空");
        }
        
        if (wasteId == null || wasteId <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "危废ID无效");
        }
        
        if (!StringUtils.hasText(spectrumType)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "光谱类型不能为空");
        }
        
        try {
            SpectrumType.fromString(spectrumType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ResultCode.SPECTRUM_TYPE_NOT_SUPPORT, "不支持的光谱类型: " + spectrumType);
        }
        
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEED, "文件大小超出限制");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORT, "不支持的文件类型: " + contentType);
        }
    }

    private String generateDefaultTestName(HazardousWaste waste, String spectrumType) {
        return String.format("%s-%s-%s测试", waste.getWasteCode(), waste.getWasteName(), spectrumType);
    }

    private String getImageDirectoryPath(String spectrumType) {
        return imageStoragePath + File.separator + spectrumType;
    }

    private String getImageFilePath(String spectrumType, String fileName) {
        return getImageDirectoryPath(spectrumType) + File.separator + fileName;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }

    private void deleteImageFile(String spectrumType, String fileName) {
        try {
            Path filePath = Paths.get(getImageFilePath(spectrumType, fileName));
            Files.deleteIfExists(filePath);
            log.info("删除图像文件成功: {}", filePath);
        } catch (IOException e) {
            log.error("删除图像文件失败", e);
        }
    }

    // ==================== 其他必要方法的基本实现 ====================

    @Override
    public List<ThermalProperty> searchByKeywordAndCategory(String keyword, String category) {
        return Collections.emptyList();
    }

    @Override
    public List<ThermalProperty> getByWasteIdAndCategory(Long wasteId, String category) {
        return Collections.emptyList();
    }

    @Override
    public List<ThermalProperty> searchByKeyword(String keyword, Integer pageNum, Integer pageSize) {
        return Collections.emptyList();
    }

    @Override
    public List<ThermalProperty> getByWasteCode(String wasteCode) {
        return Collections.emptyList();
    }

    @Override
    @Cacheable(value = "thermalCategories")
    public List<String> getAllCategories() {
        return Arrays.asList("FTIR", "TG-DSC", "XRF", "GC-MS");
    }

    @Override
    public List<ThermalProperty> getByCategory(String category, Integer pageNum, Integer pageSize) {
        return Collections.emptyList();
    }

    @Override
    @Cacheable(value = "thermalStatistics")
    public ThermalStatisticsResponse getStatistics() {
        return new ThermalStatisticsResponse();
    }

    @Override
    @Cacheable(value = "thermalStatsByCategory")
    public Map<String, Object> getStatisticsByCategory() {
        return Collections.emptyMap();
    }

    @Override
    public List<Map<String, Object>> getFileInfoByCategory(String category) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getDataCompletenessStats() {
        return Collections.emptyMap();
    }

    @Override
    @Cacheable(value = "thermalByWaste", key = "#wasteId")
    public List<ThermalProperty> getByWasteId(Long wasteId) {
        return Collections.emptyList();
    }

    @Override
    @Cacheable(value = "thermalByType", key = "#spectrumType")
    public List<ThermalProperty> getBySpectrumType(String spectrumType) {
        return Collections.emptyList();
    }

    @Override
    public List<ThermalProperty> getByWasteIdAndSpectrumType(Long wasteId, String spectrumType) {
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<ThermalProperty> properties) {
        return 0;
    }

    @Override
    public Page<ThermalProperty> getThermalDataPage(Long current, Long size, String spectrumType, Long wasteId) {
        return new Page<>();
    }

    @Override
    public Page<ThermalProperty> getPage(Long current, Long size, String spectrumType, Long wasteId) {
        return new Page<>();
    }

    @Override
    public List<ThermalProperty> searchSimilarPeaks(String peakValue) {
        return Collections.emptyList();
    }

    @Override
    public String exportThermalData(List<Long> wasteIds, List<String> spectrumTypes) {
        return "";
    }

    @Override
    @Cacheable(value = "spectrumTypes", key = "'all'")
    public List<String> getSupportedSpectrumTypes() {
        return Arrays.asList("FTIR", "TG-DSC", "XRF", "GC-MS");
    }

    @Override
    public List<ThermalProperty> getLatestByWasteId(Long wasteId, Integer limit) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getThermalStatistics() {
        return Collections.emptyMap();
    }

    @Override
    public List<Map<String, Object>> getStatisticsByLab() {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getStatisticsBySpectrumType() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<ThermalProperty>> getPropertiesByEquipment(String equipmentModel) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getThermalTrendAnalysis(List<Long> wasteIds, String spectrumType) {
        return Collections.emptyMap();
    }

    @Override
    @Cacheable(value = "thermalByQuality", key = "#qualityGrade")
    public List<ThermalProperty> getByQualityGrade(String qualityGrade) {
        return Collections.emptyList();
    }

    @Override
    @Cacheable(value = "qualityDistribution", key = "'all'")
    public List<Map<String, Object>> getQualityGradeDistribution() {
        return Collections.emptyList();
    }

    @Override
    @Cacheable(value = "labStatistics", key = "'all'")
    public List<Map<String, Object>> getCountByTestLab() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getDataCompletenessReport(Long wasteId) {
        return Collections.emptyMap();
    }

    @Override
    public List<ThermalProperty> getByPeakCountRange(Integer minPeakCount, Integer maxPeakCount) {
        return Collections.emptyList();
    }

    @Override
    public List<ThermalProperty> getByTemperatureRange(BigDecimal minTemp, BigDecimal maxTemp) {
        return Collections.emptyList();
    }

    @Override
    public List<ThermalProperty> getByHeatingRate(BigDecimal heatingRate) {
        return Collections.emptyList();
    }

    @Override
    public List<ThermalProperty> getByAtmosphere(String atmosphere) {
        return Collections.emptyList();
    }

    @Override
    @Cacheable(value = "thermalBehavior", key = "'distribution'")
    public List<Map<String, Object>> getThermalBehaviorDistribution() {
        return Collections.emptyList();
    }

    @Override
    public List<ThermalProperty> findSimilarThermalProperties(Long referenceId, BigDecimal threshold) {
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateQualityGrade(Map<Long, String> updates) {
        return 0;
    }

    @Override
    public List<Map<String, Object>> validateThermalDataConsistency(Long wasteId) {
        return Collections.emptyList();
    }

    // Add utility methods for date conversion
    private Date convertToDate(LocalDate localDate) {
        return localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
    }
    
    private LocalDate convertToLocalDate(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }
    
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }
} 