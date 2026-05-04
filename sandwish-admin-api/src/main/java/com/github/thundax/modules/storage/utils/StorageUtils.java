package com.github.thundax.modules.storage.utils;

import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.id.IdGen;
import com.github.thundax.modules.storage.entity.Storage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

public final class StorageUtils {

    private static final Logger logger = LoggerFactory.getLogger(StorageUtils.class);

    public static final String PNG = "png";
    public static final String JPG = "jpg";
    public static final String JPEG = "jpeg";

    private static final Map<String, String> ALLOW_CONTENT_TYPES;

    static {
        ALLOW_CONTENT_TYPES = new HashMap<>();
        ALLOW_CONTENT_TYPES.put(MimeTypeUtils.IMAGE_JPEG_VALUE, "jpg");
        ALLOW_CONTENT_TYPES.put(MimeTypeUtils.IMAGE_PNG_VALUE, "png");
    }

    private StorageUtils() {}

    public static void applyFileMetadata(MultipartFile file, Storage storage) {
        String originalFilename = file.getOriginalFilename();

        storage.setName(FilenameUtils.getBaseName(originalFilename));

        String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
        if (StringUtils.isBlank(extendName)) {
            extendName = ALLOW_CONTENT_TYPES.get(StringUtils.lowerCase(file.getContentType()));
        }

        if (StringUtils.isBlank(EntityIdCodec.toValue(storage.getId()))) {
            storage.setId(EntityIdCodec.toDomain(IdGen.uuid()));
            storage.setCreateDate(new Date());
        }

        storage.setExtendName(extendName);
        storage.setMimeType(file.getContentType());
    }

    public static void saveFile(InputStream inputStream, File localFile) {
        FileOutputStream outputStream = null;

        try {
            if (!localFile.getParentFile().exists()) {
                FileUtils.forceMkdirParent(localFile);
            }

            outputStream = new FileOutputStream(localFile);
            byte[] buffer = new byte[4096];
            int readBytes;
            while ((readBytes = inputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, readBytes);
            }
            outputStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
