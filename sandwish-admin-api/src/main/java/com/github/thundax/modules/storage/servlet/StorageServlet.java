package com.github.thundax.modules.storage.servlet;

import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.storage.backend.StorageBackend;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.service.StorageService;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

/**
 * 提供附件
 */
public class StorageServlet extends HttpServlet {

    private static final int BUFFER_SIZE = 4096;

    private final StorageConverter storageConverter;
    private final StorageService storageService;
    private final StorageBackend storageBackend;

    public StorageServlet(
            @NonNull StorageConverter storageConverter,
            @NonNull StorageService storageService,
            @NonNull StorageBackend storageBackend) {
        super();
        this.storageConverter = storageConverter;
        this.storageService = storageService;
        this.storageBackend = storageBackend;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StoredObject storage = storageConverter.toEntity(request.getRequestURI());

        if (storage == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        if (!storageService.canAccess(storage, StorageOwnerType.USER, UserAccessHolder.currentUserId())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        if (!storageBackend.exists(storage)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(storage.getMimeType());

        try (InputStream inputStream = storageBackend.open(storage)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) > -1) {
                response.getOutputStream().write(buffer, 0, readBytes);
            }
        }
    }
}
