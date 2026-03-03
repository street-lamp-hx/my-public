package com.yiyitech.mf.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName ModelAutoFillConfig.java
 * @Description
 * @createTime 2025年07月15日 14:46:00
 */
@Component
public class ModelAutoFillConfig implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createBy", String.class, "system");
        this.strictInsertFill(metaObject, "updateBy", String.class, "system");

        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "updateBy", String.class, "system");
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
}
