package com.github.thundax.modules.storage.servlet;

import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.entity.enums.StorageOwnerType;
import com.github.thundax.modules.storage.service.StorageService;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

/**
 * 提供附件
 */
public class StorageServlet extends HttpServlet {

    private final StorageConverter storageConverter;
    private final StorageService storageService;

    public StorageServlet(@NonNull StorageConverter storageConverter, @NonNull StorageService storageService) {
        super();
        this.storageConverter = storageConverter;
        this.storageService = storageService;
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

        Storage storage = storageConverter.toEntity(request.getRequestURI());

        if (storage == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        if (!storageService.canAccess(storage, StorageOwnerType.USER, UserAccessHolder.currentUserId())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        File file = storageConverter.toFile(storage);
        if (!file.exists()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(storage.getMimeType());

        response.getOutputStream().write(FileUtils.readFileToByteArray(file));
    }
}
