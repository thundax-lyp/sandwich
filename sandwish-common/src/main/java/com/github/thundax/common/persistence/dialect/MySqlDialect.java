package com.github.thundax.common.persistence.dialect;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.MetaObjectUtil;
import com.github.thundax.common.utils.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author thundax
 */
public class MySqlDialect extends com.github.pagehelper.dialect.helper.MySqlDialect {

    private static final String QUERY = "query";

    /**
     * copy from com.github.pagehelper.dialect.AbstractHelperDialect
     * 原有代码在处理执行参数的时候，先把所有"get"属性读入到map中，再传递给执行器执行。这样的方式会导致惰性加载的属性被误加进参数表。
     * 这里如果属性中包含Query对象，则只添加该对象；否则，保持原有逻辑
     */
    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
        //处理参数
        Page<?> page = getLocalPage();
        //如果只是 order by 就不必处理参数
        if (page.isOrderByOnly()) {
            return parameterObject;
        }
        Map<String, Object> paramMap = new HashMap<>(16);
        if (parameterObject instanceof Map) {
            //解决不可变Map的情况
            paramMap.putAll((Map) parameterObject);

        } else if (parameterObject != null) {
            //动态sql时的判断条件不会出现在ParameterMapping中，但是必须有，所以这里需要收集所有的getter属性
            //TypeHandlerRegistry可以直接处理的会作为一个直接使用的对象进行处理
            boolean hasTypeHandler = ms.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());
            MetaObject metaObject = MetaObjectUtil.forObject(parameterObject);
            //需要针对注解形式的MyProviderSqlSource保存原值
            if (!hasTypeHandler) {
                boolean existQuery = false;
                for (String name : metaObject.getGetterNames()) {
                    if (StringUtils.equals(QUERY, name)) {
                        paramMap.put(QUERY, metaObject.getValue(name));
                        existQuery = true;
                        break;
                    }
                }
                if (!existQuery) {
                    for (String name : metaObject.getGetterNames()) {
                        paramMap.put(name, metaObject.getValue(name));
                    }
                }

                // 放入静态变量
                Object originalObject = metaObject.getOriginalObject();
                for (Class<?> superClass = originalObject.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
                    for (Field field : superClass.getFields()) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            try {
                                paramMap.put(field.getName(), field.get(originalObject));
                            } catch (IllegalArgumentException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
            //下面这段方法，主要解决一个常见类型的参数时的问题
            if (boundSql.getParameterMappings() != null && boundSql.getParameterMappings().size() > 0) {
                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    String name = parameterMapping.getProperty();
                    if (!name.equals(PAGEPARAMETER_FIRST)
                            && !name.equals(PAGEPARAMETER_SECOND)
                            && paramMap.get(name) == null) {
                        if (hasTypeHandler || parameterMapping.getJavaType().equals(parameterObject.getClass())) {
                            paramMap.put(name, parameterObject);
                            break;
                        }
                    }
                }
            }
        }
        return processPageParameter(ms, paramMap, page, boundSql, pageKey);
    }

}
