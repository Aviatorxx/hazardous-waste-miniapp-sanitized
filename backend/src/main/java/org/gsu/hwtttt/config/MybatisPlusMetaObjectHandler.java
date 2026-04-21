package org.gsu.hwtttt.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus自动填充处理器
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的填充策略
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充...");
        
        // 创建时间 - 对所有实体都填充
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        
        // 更新时间 - 只对有updateTime字段的实体填充（排除MatchingDetails）
        String tableName = metaObject.getOriginalObject().getClass().getSimpleName();
        if (!"MatchingDetails".equals(tableName) && metaObject.hasSetter("updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
        
        // 创建人 (这里可以从当前登录用户获取)
        this.strictInsertFill(metaObject, "createUser", String.class, getCurrentUser());
        // 更新人
        if (!"MatchingDetails".equals(tableName) && metaObject.hasSetter("updateUser")) {
            this.strictInsertFill(metaObject, "updateUser", String.class, getCurrentUser());
        }
        // 操作时间
        this.strictInsertFill(metaObject, "operationTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时的填充策略
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充...");
        
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 更新人
        this.strictUpdateFill(metaObject, "updateUser", String.class, getCurrentUser());
    }

    /**
     * 获取当前用户
     * TODO: 这里可以从Spring Security或者其他认证框架获取当前登录用户
     */
    private String getCurrentUser() {
        // 暂时返回系统用户
        return "system";
    }
} 