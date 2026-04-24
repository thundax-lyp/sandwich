package com.github.thundax.modules.storage.converter;

import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.utils.MetaFile;
import com.github.thundax.modules.storage.utils.StorageServiceHolder;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author
 */
@Service
public class StorageConverter {

    private final String servletPath;
    private final String storagePath;

    public StorageConverter(VltavaProperties properties) {
        this.servletPath = properties.getUpload().getServletPath();
        this.storagePath = properties.getUpload().getStoragePath();
    }

    public String toPreviewUrl(Storage entity) {
        return this.servletPath + entity.getFileName();
    }

    public Storage toEntity(String previewUrl) {
        if (!StringUtils.contains(previewUrl, servletPath)) {
            return null;
        }

        String[] names = StringUtils.split(previewUrl, MetaFile.SEPARATOR);
        String filename = names[names.length - 1];

        String[] parts = StringUtils.split(filename, ".");
        return StorageServiceHolder.get(parts[0]);
    }

    public File toFile(Storage storage) {
        return new File(this.storagePath + storage.getPathName());
    }

}
