package com.songsy.iframe.core.persistence.provider.service;

import com.songsy.iframe.core.persistence.provider.entity.BaseEntity;
import com.songsy.iframe.core.persistence.provider.exception.UpdateException;
import com.songsy.iframe.core.persistence.provider.exception.VersionException;
import com.songsy.iframe.core.persistence.provider.mapper.BaseCurdMapper;
import com.songsy.iframe.core.persistence.provider.utils.IDGeneratorUtils;
import com.songsy.iframe.core.persistence.provider.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 抽象service基类
 *
 * @author songsy
 * @Date 2018/131 17:17
 */
public abstract class AbstractBaseService<T extends BaseEntity, ID extends Serializable> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public abstract BaseCurdMapper<T, ID> getRepository();

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public T findById(ID id) {
        return getRepository().findById(id);
    }

    /**
     * 通用插入更新方法
     *
     * @param entity
     * @return
     */
    @Transactional
    public T saveSelective(T entity) {
        return saveSelective(entity, false);
    }

    @Transactional
    public T saveSelective(T entity, Boolean hasId) {
        if (hasId) {
            // 之前已经生成了id
            insertSelective(entity);
        } else if (!StringUtils.isEmpty(entity.getId())) {
            updateSelective(entity);
            // 乐观锁自增
            entity.setVersion(entity.getVersion() + 1);
        } else {
            try {
                Class clazz = Class.forName(entity.getClass().getName());
                Class idClass = ReflectionUtils.getPrimarykeyClassType(clazz);
                // 如果主键是字符类型，则采用32位字符
                if (idClass.equals(String.class)) {
                    entity.setId(IDGeneratorUtils.generateID());
                } else {
                    // 默认主键由数据库自动生成（主要是自动增长型）
                }
            } catch (ClassNotFoundException e) {
                logger.error("");
            }
            insertSelective(entity);
        }
        return entity;
    }

    private void insertSelective(T entity) {
        entity.setCreatedDate(new Date());
        entity.setLastModifiedDate(new Date());
        entity.setVersion(1);
        if (null == entity.getCreatedBy()) {
            // TODO 设置当前登陆人
            entity.setCreatedBy("");
        }
        if (null == entity.getLastModifiedBy()) {
            // TODO 设置当前登陆人
            entity.setLastModifiedBy("");
        }
        getRepository().insert(entity);
    }

    private void updateSelective(T entity) {
        if (entity.getVersion() == null) {
            throw new VersionException();
        }
        entity.setLastModifiedDate(new Date());
        if (null == entity.getLastModifiedBy()) {
            // TODO 设置当前登陆人
            entity.setLastModifiedBy("");
        }
        Integer flag = getRepository().update(entity);
        if (flag == 0) {
            throw new UpdateException();
        }
    }
}
