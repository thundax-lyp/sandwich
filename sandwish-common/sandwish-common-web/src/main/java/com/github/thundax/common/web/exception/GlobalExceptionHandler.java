package com.github.thundax.common.web.exception;

import com.github.thundax.common.web.i18n.I18nMessageResolver;
import com.github.thundax.common.web.response.ApiResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final I18nMessageResolver i18nMessageResolver;
    private final List<ExceptionTranslator> exceptionTranslators;

    public GlobalExceptionHandler(
            I18nMessageResolver i18nMessageResolver, List<ExceptionTranslator> exceptionTranslators) {
        this.i18nMessageResolver = i18nMessageResolver;
        this.exceptionTranslators = sortedTranslators(exceptionTranslators);
    }

    @ExceptionHandler(SandwishException.class)
    public ResponseEntity<ApiResponse<Object>> handleSandwishException(SandwishException exception) {
        return toResponseEntity(exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception exception) {
        SandwishException translatedException = translate(exception);
        if (translatedException != null) {
            return toResponseEntity(translatedException);
        }
        return toResponseEntity(new SystemException());
    }

    private SandwishException translate(Exception exception) {
        for (ExceptionTranslator translator : exceptionTranslators) {
            SandwishException translatedException = translator.translate(exception);
            if (translatedException != null) {
                return translatedException;
            }
        }
        return null;
    }

    private ResponseEntity<ApiResponse<Object>> toResponseEntity(SandwishException exception) {
        return ResponseEntity.status(exception.getHttpStatus())
                .body(ApiResponse.failure(
                        exception.getCode(),
                        i18nMessageResolver.resolve(
                                exception.getMessageKey(), exception.getDefaultMessage(), exception.getMessageArgs())));
    }

    private List<ExceptionTranslator> sortedTranslators(List<ExceptionTranslator> translators) {
        if (translators == null || translators.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExceptionTranslator> sortedTranslators = new ArrayList<>(translators);
        AnnotationAwareOrderComparator.sort(sortedTranslators);
        return sortedTranslators;
    }
}
