package com.github.thundax.modules.common.persistence.interceptor;

import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

@Component
@Intercepts({
    @Signature(
            type = Executor.class,
            method = "update",
            args = {MappedStatement.class, Object.class})
})
public class AuditFieldInterceptor implements Interceptor {

    private static final String CREATE_DATE = "createDate";
    private static final String CREATE_BY = "createBy";
    private static final String UPDATE_DATE = "updateDate";
    private static final String UPDATE_BY = "updateBy";
    private static final String UPDATE_DATE_COLUMN = "update_date";
    private static final String UPDATE_BY_COLUMN = "update_by";

    private final ConcurrentMap<String, Class<?>> mapperDataObjectCache = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameter = args[1];
        if (statement.getSqlCommandType() == SqlCommandType.INSERT) {
            fillInsert(parameter);
        } else if (statement.getSqlCommandType() == SqlCommandType.UPDATE) {
            fillUpdate(statement, parameter);
        }
        return invocation.proceed();
    }

    private void fillInsert(Object parameter) {
        if (parameter == null) {
            return;
        }
        Date now = new Date();
        String currentUserId = UserAccessHolder.currentUserId();
        MetaObject metaObject = SystemMetaObject.forObject(parameter);
        setValue(metaObject, CREATE_DATE, now);
        setValue(metaObject, UPDATE_DATE, now);
        setValue(metaObject, CREATE_BY, currentUserId);
        setValue(metaObject, UPDATE_BY, currentUserId);
    }

    private void fillUpdate(MappedStatement statement, Object parameter) {
        if (parameter == null) {
            return;
        }
        Date now = new Date();
        String currentUserId = UserAccessHolder.currentUserId();
        if (parameter instanceof Map) {
            Map<?, ?> parameterMap = (Map<?, ?>) parameter;
            Object entity = getParameter(parameterMap, Constants.ENTITY);
            if (entity != null) {
                MetaObject metaObject = SystemMetaObject.forObject(entity);
                setValue(metaObject, UPDATE_DATE, now);
                setValue(metaObject, UPDATE_BY, currentUserId);
                return;
            }
            Object wrapper = getParameter(parameterMap, Constants.WRAPPER);
            Class<?> dataObjectClass = findMapperDataObjectClass(statement);
            if (wrapper instanceof Update && dataObjectClass != null) {
                Update<?, ?> update = (Update<?, ?>) wrapper;
                if (hasField(dataObjectClass, UPDATE_DATE)) {
                    update.setSql(UPDATE_DATE_COLUMN + " = {0}", now);
                }
                if (hasField(dataObjectClass, UPDATE_BY)) {
                    update.setSql(UPDATE_BY_COLUMN + " = {0}", currentUserId);
                }
            }
            return;
        }
        MetaObject metaObject = SystemMetaObject.forObject(parameter);
        setValue(metaObject, UPDATE_DATE, now);
        setValue(metaObject, UPDATE_BY, currentUserId);
    }

    private void setValue(MetaObject metaObject, String property, Object value) {
        if (metaObject.hasSetter(property)) {
            metaObject.setValue(property, value);
        }
    }

    private Object getParameter(Map<?, ?> parameterMap, String key) {
        return parameterMap.containsKey(key) ? parameterMap.get(key) : null;
    }

    private Class<?> findMapperDataObjectClass(MappedStatement statement) {
        String statementId = statement.getId();
        int methodSeparator = statementId.lastIndexOf('.');
        if (methodSeparator < 0) {
            return null;
        }
        String mapperClassName = statementId.substring(0, methodSeparator);
        return mapperDataObjectCache.computeIfAbsent(mapperClassName, this::resolveMapperDataObjectClass);
    }

    private Class<?> resolveMapperDataObjectClass(String mapperClassName) {
        try {
            Class<?> mapperClass = Class.forName(mapperClassName);
            for (Type type : mapperClass.getGenericInterfaces()) {
                if (!(type instanceof ParameterizedType)) {
                    continue;
                }
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (!BaseMapper.class.equals(parameterizedType.getRawType())) {
                    continue;
                }
                Type dataObjectType = parameterizedType.getActualTypeArguments()[0];
                if (dataObjectType instanceof Class) {
                    return (Class<?>) dataObjectType;
                }
            }
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }

    private boolean hasField(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null && !Object.class.equals(current)) {
            for (Field field : current.getDeclaredFields()) {
                if (fieldName.equals(field.getName())) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {}
}
