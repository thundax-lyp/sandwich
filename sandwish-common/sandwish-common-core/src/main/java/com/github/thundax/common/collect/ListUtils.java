package com.github.thundax.common.collect;

import com.github.thundax.common.collect.ext.ListUtilEx;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.springframework.lang.NonNull;

/**
 * List工具类
 *
 * @author thundax
 */
@SuppressWarnings("rawtypes")
public class ListUtils extends ListUtilEx {

    /** 判断是否为空. */
    public static boolean isEmpty(Collection collection) {
        return (collection == null || collection.isEmpty());
    }

    /** 判断是否为不为空. */
    public static boolean isNotEmpty(Collection collection) {
        return !(isEmpty(collection));
    }

    public static <T> void forEach(List<T> list, Consumer<T> action) {
        if (ListUtils.isNotEmpty(list)) {
            list.forEach(action);
        }
    }

    @NonNull
    public static <T> List<T> subList(final List<T> list, int fromIndex, int size) {
        if (list == null) {
            return new ArrayList<>();
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
            list.forEach(
                    item -> {
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
