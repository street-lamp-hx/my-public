package com.yiyitech.support.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * 轻量版 AbstractBaseMapper：替代原 com.yiyitech.support.mybatis.AbstractBaseMapper
 * 目的：去掉 yiyitech-support 后先让项目可编译/可运行
 *
 * @param <T> 实体类型
 */
public interface AbstractBaseMapper<T> extends BaseMapper<T> {

    /**
     * 批量插入
     *
     * @param entityClass 实体类
     * @param dataList    数据列表
     * @param batchSize   每批次大小
     * @param <E>         实体类型
     * @return 插入条数
     */
    default <E> int batchInsert(Class<E> entityClass, List<E> dataList, int batchSize) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }
        String sqlStatement = SqlHelper.getSqlStatement(entityClass,
                com.baomidou.mybatisplus.core.enums.SqlMethod.INSERT_ONE);
        try (SqlSession sqlSession = SqlHelper.sqlSessionBatch(entityClass)) {
            int i = 0;
            for (E entity : dataList) {
                sqlSession.insert(sqlStatement, entity);
                if (++i % batchSize == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.flushStatements();
            return dataList.size();
        }
    }

    /**
     * 批量根据ID更新
     *
     * @param entityClass 实体类
     * @param dataList    数据列表
     * @param batchSize   每批次大小
     * @param <E>         实体类型
     * @return 更新条数
     */
    default <E> int updateBatchById(Class<E> entityClass, List<E> dataList, int batchSize) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }
        String sqlStatement = SqlHelper.getSqlStatement(entityClass,
                com.baomidou.mybatisplus.core.enums.SqlMethod.UPDATE_BY_ID);
        try (SqlSession sqlSession = SqlHelper.sqlSessionBatch(entityClass)) {
            int i = 0;
            for (E entity : dataList) {
                sqlSession.update(sqlStatement, entity);
                if (++i % batchSize == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.flushStatements();
            return dataList.size();
        }
    }
}
