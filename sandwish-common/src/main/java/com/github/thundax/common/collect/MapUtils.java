package com.github.thundax.common.collect;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;


/**
 * Map工具类，实现 Map <-> Bean 互相转换
 *
 * @author thundax
 */
public class MapUtils extends org.apache.commons.collections.MapUtils {

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>(16);
    }

    public static <K, V> HashMap<K, V> newHashMap(int initialCapacity) {
        return new HashMap<K, V>(initialCapacity);
    }

    public static <K, V> HashMap<K, V> newHashMap(Map<? extends K, ? extends V> map) {
        return map == null ? new HashMap<K, V>(16) : new HashMap<K, V>(map);
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<K, V>();
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(Map<? extends K, ? extends V> map) {
        return new LinkedHashMap<K, V>(map);
    }

    public static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
        return new ConcurrentHashMap<K, V>(16);
    }

    public static <K extends Comparable<?>, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<K, V>();
    }

    public static <K, V> TreeMap<K, V> newTreeMap(SortedMap<K, ? extends V> map) {
        return new TreeMap<K, V>(map);
    }

    public static <C, K extends C, V> TreeMap<K, V> newTreeMap(Comparator<C> comparator) {
        return new TreeMap<K, V>(comparator);
    }

    public static <K extends Enum<K>, V> EnumMap<K, V> newEnumMap(Class<K> type) {
        return new EnumMap<K, V>((type));
    }

    public static <K extends Enum<K>, V> EnumMap<K, V> newEnumMap(Map<K, ? extends V> map) {
        return new EnumMap<K, V>(map);
    }

    public static <K, V> IdentityHashMap<K, V> newIdentityHashMap() {
        return new IdentityHashMap<K, V>();
    }

    public static <K, V> void forEach(Map<K, V> map, BiConsumer<? super K, ? super V> action) {
        if (map != null) {
            map.forEach(action);
        }
    }

    public static <K, V> void removeIf(Map<K, V> map, Predicate<? super Map.Entry<K, V>> filter) {
        if (map != null) {
            map.entrySet().removeIf(filter);
        }
    }

    /**
     * List<Map<String, V>转换为List<T>
     */
    public static <T, V> List<T> toObjectList(Class<T> clazz, List<HashMap<String, V>> list) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, InstantiationException {
        List<T> retList = new ArrayList<T>();
        if (list != null && !list.isEmpty()) {
            for (HashMap<String, V> m : list) {
                retList.add(toObject(clazz, m));
            }
        }
        return retList;
    }

    /**
     * 将Map转换为Object
     *
     * @param clazz 目标对象的类
     * @param map   待转换Map
     */
    public static <T, V> T toObject(Class<T> clazz, Map<String, V> map)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        T object = clazz.getDeclaredConstructor().newInstance();
        return toObject(object, map);
    }

    /**
     * 将Map转换为Object
     *
     * @param map 待转换Map
     */
    public static <T, V> T toObject(T object, Map<String, V> map) throws IllegalAccessException, InvocationTargetException {
        BeanUtils.populate(object, map);
        return object;
    }

    /**
     * 对象转Map
     *
     * @param object 目标对象
     * @return 转换出来的值都是String
     */
    public static Map<String, String> toMap(Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return BeanUtils.describe(object);
    }

    /**
     * 对象转Map
     *
     * @param object 目标对象
     * @return 转换出来的值类型是原类型
     */
    public static Map<String, Object> toNavMap(Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return PropertyUtils.describe(object);
    }

    /**
     * 转换为Collection<Map<K, V>>
     *
     * @param collection 待转换对象集合
     * @return 转换后的Collection<Map < K, V>>
     */
    public static <T> Collection<Map<String, String>> toMapList(Collection<T> collection)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<Map<String, String>> retList = new ArrayList<>();
        if (collection != null && !collection.isEmpty()) {
            for (Object object : collection) {
                Map<String, String> map = toMap(object);
                retList.add(map);
            }
        }
        return retList;
    }

}
