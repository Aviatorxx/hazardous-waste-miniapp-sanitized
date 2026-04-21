package org.gsu.hwtttt.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.gsu.hwtttt.constant.SystemConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置Caffeine缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 设置默认缓存配置
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(SystemConstants.DEFAULT_CACHE_TIME, TimeUnit.SECONDS)
                .recordStats());
        
        // 设置缓存名称
        cacheManager.setCacheNames(Arrays.asList(
                // 危废相关缓存
                "hazardousWaste",        // 危废缓存
                "wasteDetail",           // 危废详情缓存 (HazardousWasteServiceImpl.getWasteDetail)
                "wasteKeyword",          // 关键字搜索缓存 (HazardousWasteServiceImpl.searchByKeyword)  
                "wasteSearch",           // 搜索结果缓存 (HazardousWasteServiceImpl.searchWaste)
                "wasteStatistics",       // 危废统计缓存 (HazardousWasteServiceImpl.getStatistics)
                "hazardProperties",      // 危险特性缓存 (HazardousWasteServiceImpl.getHazardProperties)
                
                // 理化特性相关缓存
                "physicalProperty",      // 理化特性缓存
                "propertyByWaste",       // 按危废ID的理化特性缓存 (PhysicalPropertyServiceImpl.getByWasteId)
                "propertyByCategory",    // 按分类的理化特性缓存 (PhysicalPropertyServiceImpl.getByCategoryCode)
                "propertyCategories",    // 理化特性分类缓存 (PropertyCategoryServiceImpl)
                
                // 热力学特性相关缓存
                "thermalByWaste",        // 按危废ID的热力学特性缓存 (ThermalPropertyServiceImpl.getByWasteId)
                "thermalByType",         // 按光谱类型的热力学特性缓存 (ThermalPropertyServiceImpl.getBySpectrumType)
                "spectrumTypes",         // 光谱类型缓存 (ThermalPropertyServiceImpl.getSupportedSpectrumTypes)
                "thermalStatistics",     // 热力学统计缓存
                
                // 其他缓存
                "compatibilityMatrix"    // 配伍矩阵缓存
        ));
        
        return cacheManager;
    }

    /**
     * 长期缓存配置（用于相对稳定的数据）
     */
    @Bean("longTermCache")
    public Caffeine<Object, Object> longTermCaffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(2, TimeUnit.HOURS)
                .recordStats();
    }

    /**
     * 短期缓存配置（用于频繁变动的数据）
     */
    @Bean("shortTermCache")
    public Caffeine<Object, Object> shortTermCaffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * 统计数据缓存配置
     */
    @Bean("statisticsCache")
    public Caffeine<Object, Object> statisticsCaffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats();
    }
} 