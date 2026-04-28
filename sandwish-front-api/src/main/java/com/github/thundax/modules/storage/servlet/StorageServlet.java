package com.github.thundax.modules.storage.servlet;

import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.Storage;
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
 *
 * @author
 */
public class StorageServlet extends HttpServlet {

    private final StorageConverter storageConverter;

    public StorageServlet(@NonNull StorageConverter storageConverter) {
        super();
        this.storageConverter = storageConverter;
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
