package com.github.thundax.common.collect;

import com.github.thundax.common.utils.StringUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * List工具类
 *
 * @author thundax
 */
@SuppressWarnings("rawtypes")
public class ListUtils extends org.apache.commons.collections.ListUtils {

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    @SafeVarargs
    @NonNull
    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<E>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        return (elements instanceof Collection) ? new ArrayList<E>(cast(elements))
                : newArrayList(elements.iterator());
    }

    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
        ArrayList<E> list = newArrayList();
        addAll(list, elements);
        return list;
    }

    public static <E> LinkedList<E> newLinkedList() {
        return new LinkedList<E>();
    }

    public static <E> LinkedList<E> newLinkedList(Iterable<? extends E> elements) {
        LinkedList<E> list = newLinkedList();
        addAll(list, elements);
        return list;
    }

    public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList() {
        return new CopyOnWriteArrayList<E>();
    }

    public static <E> CopyOnWriteArrayList<E> newCopyOnWriteArrayList(Iterable<? extends E> elements) {
        Collection<? extends E> elementsCollection = (elements instanceof Collection)
                ? cast(elements) : newArrayList(elements);
        return new CopyOnWriteArrayList<E>(elementsCollection);
    }

    private static <T> Collection<T> cast(Iterable<T> iterable) {
        return (Collection<T>) iterable;
    }

    private static <T> boolean addAll(Collection<T> addTo, Iterator<? extends T> iterator) {
        boolean wasModified = false;
        while (iterator.hasNext()) {
            wasModified |= addTo.add(iterator.next());
        }
        return wasModified;
    }

    public static <T> boolean addAll(Collection<T> addTo, Iterable<? extends T> elementsToAdd) {
        if (elementsToAdd instanceof Collection) {
            Collection<? extends T> c = cast(elementsToAdd);
            return addTo.addAll(c);
        }
        return addAll(addTo, elementsToAdd.iterator());
    }

    /**
     * 提取集合中的对象的两个属性(通过Getter函数), 组合成Map.
     *
     * @param collection        来源集合.
     * @param keyPropertyName   要提取为Map中的Key值的属性名.
     * @param valuePropertyName 要提取为Map中的Value值的属性名.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> extractToMap(final Collection collection, final String keyPropertyName, final String valuePropertyName) {
        try {
            Map map = new HashMap(collection.size());
            for (Object obj : collection) {
                map.put((K) PropertyUtils.getProperty(obj, keyPropertyName),
                        (V) PropertyUtils.getProperty(obj, valuePropertyName));
            }
            return map;

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成List.
     *
     * @param collection   来源集合.
     * @param propertyName 要提取的属性名.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> extractToList(final Collection collection, final String propertyName) {
        if (collection == null) {
            return newArrayList();
        }
        List list = new ArrayList(collection.size());
        try {
            for (Object obj : collection) {
                list.add(PropertyUtils.getProperty(obj, propertyName));
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        return list;
    }

    /**
     * 转换Collection所有元素(通过toString())为String, 中间以 separator分隔。
     */
    public static String convertToString(final Collection collection, final String separator) {
        return StringUtils.join(collection, separator);
    }

    /**
     * 转换Collection所有元素(通过toString())为String, 每个元素的前面加入prefix，后面加入postfix，如<div>mymessage</div>。
     */
    public static String convertToString(final Collection collection, final String prefix, final String postfix) {
        StringBuilder builder = new StringBuilder();
        for (Object o : collection) {
            builder.append(prefix).append(o).append(postfix);
        }
        return builder.toString();
    }

    /**
     * 判断是否为空.
     */
    public static boolean isEmpty(Collection collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * 判断是否为不为空.
     */
    public static boolean isNotEmpty(Collection collection) {
        return !(isEmpty(collection));
    }

    /**
     * 取得Collection的第一个元素，如果collection为空返回null.
     */
    public static <T> T getFirst(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.iterator().next();
    }

    /**
     * 获取Collection的最后一个元素 ，如果collection为空返回null.
     */
    public static <T> T getLast(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        //当类型为List时，直接取得最后一个元素 。
        if (collection instanceof List) {
            List<T> list = (List<T>) collection;
            return list.get(list.size() - 1);
        }
        //其他类型通过iterator滚动到最后一个元素.
        Iterator<T> iterator = collection.iterator();
        while (true) {
            T current = iterator.next();
            if (!iterator.hasNext()) {
                return current;
            }
        }
    }

    /**
     * 返回a+b的新List.
     */
    @NonNull
    public static <T> List<T> unionExtend(final Collection<T> a, final Collection<T> b) {
        List<T> result = new ArrayList<T>(a);
        result.addAll(b);
        return result;
    }

    /**
     * 返回a-b的新List.
     */
    @NonNull
    public static <T> List<T> subtractExtend(final Collection<T> a, final Collection<T> b) {
        List<T> list = new ArrayList<T>(a);
        for (T element : b) {
            list.remove(element);
        }
        return list;
    }

    /**
     * 返回a与b的交集的新List.
     */
    @NonNull
    public static <T> List<T> intersectionExtend(Collection<T> a, Collection<T> b) {
        List<T> list = new ArrayList<T>();
        for (T element : a) {
            if (b.contains(element)) {
                list.add(element);
            }
        }
        return list;
    }


    public static <T> void forEach(List<T> list, Consumer<T> action) {
        if (ListUtils.isNotEmpty(list)) {
            list.forEach(action);
        }
    }


    @NonNull
    public static <T> List<T> subList(final List<T> list, int fromIndex, int size) {
        if (list == null) {
            return newArrayList();
        }

        List<T> result = new ArrayList<T>();
        int toIndex = Math.min(fromIndex + size, list.size());
        for (int idx = fromIndex; idx < toIndex; idx++) {
            result.add(list.get(idx));
        }
        return result;
    }


    @NonNull
    public static <T, R> List<R> map(List<T> list, Function<T, R> action) {
        List<R> resultList = new ArrayList<>();
        if (ListUtils.isNotEmpty(list)) {
            list.forEach(item -> resultList.add(action.apply(item)));
        }
        return resultList;
    }


    @NonNull
    public static <T> List<T> filter(List<T> list, Predicate<T> action) {
        List<T> resultList = new ArrayList<>();
        if (ListUtils.isNotEmpty(list)) {
            list.forEach(item -> {
                if (action.test(item)) {
                    resultList.add(item);
                }
            });
        }
        return resultList;
    }


    public static <T> T find(List<T> list, Predicate<T> action) {
        if (ListUtils.isNotEmpty(list)) {
            for (T item : list) {
                if (action.test(item)) {
                    return item;
                }
            }
        }
        return null;
    }


    public static <T> boolean contains(List<T> list, @NonNull Predicate<T> action) {
        if (ListUtils.isNotEmpty(list)) {
            for (T item : list) {
                if (action.test(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean contains(List<?> list, @NonNull Object target) {
        if (ListUtils.isNotEmpty(list)) {
            for (Object item : list) {
                if (Objects.equals(item, target)) {
                    return true;
                }
            }
        }
        return false;
    }

}
