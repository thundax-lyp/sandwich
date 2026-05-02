package com.github.thundax.common.web;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.EmptyCollectionException;
import com.github.thundax.common.utils.function.ThrowableFunction;
import java.util.ArrayList;
import java.util.List;
import org.springframework.lang.NonNull;

public final class ApiRequestListHelper {

    private ApiRequestListHelper() {}

    @NonNull
    public static <E, V> List<E> mapNotEmpty(
            @NonNull List<V> sourceList, @NonNull ThrowableFunction<V, E> mappingFunction) throws ApiException {
        if (sourceList == null || sourceList.isEmpty()) {
            throw new EmptyCollectionException();
        }

        List<E> result = new ArrayList<>();
        try {
            for (V source : sourceList) {
                if (source != null) {
                    result.add(mappingFunction.apply(source));
                }
            }
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
        return result;
    }
}
