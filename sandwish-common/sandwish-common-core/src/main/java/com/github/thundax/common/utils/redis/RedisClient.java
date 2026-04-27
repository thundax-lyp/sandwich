package com.github.thundax.common.utils.redis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.thundax.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

/** @author thundax */
@Service
public class RedisClient {

    private final StringRedisTemplate template;

    @Autowired
    public RedisClient(StringRedisTemplate template) {
        this.template = template;
    }

    /** Object get/set/delete/exists */
    public void set(String key, Object value) {
        if (value == null) {
            this.template.delete(key);

        } else {
            ValueOperations<String, String> ops = this.template.opsForValue();
            if (value instanceof String) {
                ops.set(key, (String) value);

            } else {
                ops.set(key, JsonUtils.toJson(value));
            }
        }
    }

    public void set(String key, Object value, int expiredSeconds) {
        if (value == null || expiredSeconds <= 0) {
            this.set(key, null);

        } else {
            this.set(key, value);
            this.template.expire(key, expiredSeconds, TimeUnit.SECONDS);
        }
    }

    public void setIfAbsent(String key, @NonNull Object value, int expiredSeconds) {
        ValueOperations<String, String> ops = this.template.opsForValue();
        if (value instanceof String) {
            ops.setIfAbsent(key, (String) value);

        } else {
            ops.setIfAbsent(key, JsonUtils.toJson(value));
        }
        this.template.expire(key, expiredSeconds, TimeUnit.SECONDS);
    }

    public Long increment(String key, long delta) {
        ValueOperations<String, String> ops = this.template.opsForValue();

        return ops.increment(key, delta);
    }

    public String get(String key) {
        ValueOperations<String, String> ops = this.template.opsForValue();
        return ops.get(key);
    }

    public String computeIfAbsent(String key, Function<String, String> mappingFunction) {
        if (mappingFunction == null) {
            throw new NullPointerException();
        }

        ValueOperations<String, String> ops = this.template.opsForValue();
        String value = ops.get(key);
        if (value == null) {
            value = mappingFunction.apply(key);
            this.set(key, value);
        }

        return value;
    }

    public <T> T get(String key, TypeReference<T> valueTypeRef) {
        ValueOperations<String, String> ops = this.template.opsForValue();
        String jsonString = ops.get(key);
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        return JsonUtils.fromJson(jsonString, valueTypeRef);
    }

    public <T> T computeIfAbsent(
            String key, TypeReference<T> valueTypeRef, Function<String, T> mappingFunction) {
        ValueOperations<String, String> ops = this.template.opsForValue();
        String jsonString = ops.get(key);

        T value =
                StringUtils.isEmpty(jsonString)
                        ? null
                        : JsonUtils.fromJson(jsonString, valueTypeRef);
        if (value == null) {
            value = mappingFunction.apply(key);
            this.set(key, value);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        ValueOperations<String, String> ops = this.template.opsForValue();
        String jsonString = ops.get(key);
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        if (type == String.class) {
            return (T) jsonString;
        }
        return JsonUtils.fromJson(jsonString, type);
    }

    public <T> T computeIfAbsent(String key, Class<T> type, Function<String, T> mappingFunction) {
        ValueOperations<String, String> ops = this.template.opsForValue();
        String jsonString = ops.get(key);

        T value = StringUtils.isEmpty(jsonString) ? null : JsonUtils.fromJson(jsonString, type);
        if (value == null) {
            value = mappingFunction.apply(key);
            this.set(key, value);
        }

        return value;
    }

    public void delete(String key) {
        this.template.delete(key);
    }

    //    /**
    //     *  获取指定前缀的一系列key
    //     *  使用scan命令代替keys, Redis是单线程处理，keys命令在KEY数量较多时，
    //     *  操作效率极低【时间复杂度为O(N)】，该命令一旦执行会严重阻塞线上其它命令的正常请求
    //     * @param keyPrefix
    //     * @return
    //     */
    //    public Set<String> keys(String keyPrefix) {
    //        String realKey = keyPrefix + "*";
    //
    //        try {
    //            return template.execute((RedisCallback<Set<String>>) connection -> {
    //                Set<String> binaryKeys = new HashSet<>();
    //                Cursor<byte[]> cursor = connection.scan(new
    // ScanOptions.ScanOptionsBuilder().match(realKey).count(Integer.MAX_VALUE).build());
    //                while (cursor.hasNext()) {
    //                    binaryKeys.add(new String(cursor.next()));
    //                }
    //
    //                return binaryKeys;
    //            });
    //        } catch (Throwable e) {
    //        }
    //
    //        return null;
    //    }

    //    /**
    //     *  删除指定前缀的一系列key
    //     * @param keyPrefix
    //     */
    //    public void removeAll(String keyPrefix) {
    //        try {
    //            Set<String> keys = keys(keyPrefix);
    //            template.delete(keys);
    //        } catch (Throwable e) {
    //            logger.warn(e.getMessage(), e);
    //        }
    //    }

    public void delete(Collection<String> keys) {
        this.template.delete(keys);
    }

    public void deleteByPattern(String pattern) {
        Set<String> keys = this.keys(pattern);
        if (keys != null && keys.size() > 0) {
            this.template.delete(keys);
        }
    }

    public boolean exists(String key) {
        Boolean hasKey = this.template.hasKey(key);
        return hasKey != null && hasKey;
    }

    public Set<String> keys(String pattern) {
        String realKey = pattern + "*";

        try {
            return template.execute(
                    (RedisCallback<Set<String>>)
                            connection -> {
                                Set<String> binaryKeys = new HashSet<>();
                                Cursor<byte[]> cursor =
                                        connection.scan(
                                                new ScanOptions.ScanOptionsBuilder()
                                                        .match(realKey)
                                                        .count(Integer.MAX_VALUE)
                                                        .build());
                                while (cursor.hasNext()) {
                                    binaryKeys.add(new String(cursor.next()));
                                }

                                return binaryKeys;
                            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    /** hashMap get/set/delete/exists */
    public void setHash(String key, String hashKey, Object hashValue) {
        HashOperations<String, String, String> ops = this.template.opsForHash();
        if (hashValue == null) {
            ops.delete(key, hashKey);

        } else if (hashValue instanceof String) {
            ops.put(key, hashKey, (String) hashValue);

        } else {
            ops.put(key, hashKey, JsonUtils.toJson(hashValue));
        }
    }

    /*
    public <T> T getHash(String key, String hashKey) {
        HashOperations<String, String, String> ops = this.template.opsForHash();
        String jsonString = ops.get(key, hashKey);
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        return JsonUtils.fromJson(jsonString, new TypeReference<T>() {
        });
    }
     */

    public String getHash(String key, String hashKey) {
        HashOperations<String, String, String> ops = this.template.opsForHash();
        return ops.get(key, hashKey);
    }

    public <T> T getHash(String key, String hashKey, Class<T> clazz) {
        HashOperations<String, String, String> ops = this.template.opsForHash();
        String jsonString = ops.get(key, hashKey);
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        return JsonUtils.fromJson(jsonString, clazz);
    }

    public void deleteHash(String key, String hashKey) {
        HashOperations<String, String, String> ops = this.template.opsForHash();
        ops.delete(key, hashKey);
    }

    public Set<String> hashKeys(String key) {
        HashOperations<String, String, String> ops = this.template.opsForHash();
        return ops.keys(key);
    }

    public boolean exists(String key, String hashKey) {
        HashOperations<String, String, String> ops = this.template.opsForHash();
        return ops.hasKey(key, hashKey);
    }

    /** 设置超时秒 */
    public void expire(String key, int expiredSeconds) {
        this.template.expire(key, expiredSeconds, TimeUnit.SECONDS);
    }

    /** 返回剩余秒数 */
    public Long getExpire(String key) {
        return this.template.getExpire(key, TimeUnit.SECONDS);
    }
}
