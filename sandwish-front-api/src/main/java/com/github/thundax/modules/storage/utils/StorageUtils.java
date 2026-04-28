package com.github.thundax.modules.storage.utils;

import com.github.thundax.autoconfigure.VltavaProperties;
import com.github.thundax.common.utils.IdGen;
import com.github.thundax.common.utils.SpringContextHolder;
import com.github.thundax.modules.storage.converter.StorageConverter;
import com.github.thundax.modules.storage.entity.Storage;
import com.github.thundax.modules.storage.vo.StorageVo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Lazy(false)
public class StorageUtils {

    private static final Logger logger = LoggerFactory.getLogger(StorageUtils.class);

    private static VltavaProperties.UploadProperties properties;
    private static StorageConverter converter;

    private static VltavaProperties.UploadProperties getProperties() {
        if (properties == null) {
            properties = SpringContextHolder.getBean(VltavaProperties.UploadProperties.class);
        }
        return properties;
    }

    private static StorageConverter getConverter() {
        if (converter == null) {
            converter = SpringContextHolder.getBean(StorageConverter.class);
        }
        return converter;
    }

    public static final String PNG = "png";
    public static final String JPG = "jpg";
    public static final String JPEG = "jpeg";

    private static final Map<String, String> ALLOW_CONTENT_TYPES;

    static {
        ALLOW_CONTENT_TYPES = new HashMap<>();
        ALLOW_CONTENT_TYPES.put(MimeTypeUtils.IMAGE_JPEG_VALUE, "jpg");
        ALLOW_CONTENT_TYPES.put(MimeTypeUtils.IMAGE_PNG_VALUE, "png");
    }

    public static void saveFile(MultipartFile file, Storage storage) {
        try {
            String originalFilename = file.getOriginalFilename();

            storage.setName(FilenameUtils.getBaseName(originalFilename));

            String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));
            if (StringUtils.isBlank(extendName)) {
                extendName = ALLOW_CONTENT_TYPES.get(StringUtils.lowerCase(file.getContentType()));
            }

            if (StringUtils.isBlank(storage.getId())) {
                storage.setId(IdGen.uuid());
                storage.setCreateDate(new Date());
            }

            storage.setExtendName(extendName);
            storage.setMimeType(file.getContentType());

            saveFile(file.getInputStream(), storage);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void saveFile(File file, Storage storage) {
        try {
            String originalFilename = file.getPath();

            storage.setName(FilenameUtils.getBaseName(originalFilename));

            String extendName = StringUtils.lowerCase(FilenameUtils.getExtension(originalFilename));

            storage.setExtendName(extendName);

            if (StringUtils.equalsIgnoreCase(extendName, PNG)) {
                storage.setMimeType(MimeTypeUtils.IMAGE_PNG_VALUE);
            } else if (StringUtils.equalsIgnoreCase(extendName, JPG)
                    || StringUtils.equalsIgnoreCase(extendName, JPEG)) {
                storage.setMimeType(MimeTypeUtils.IMAGE_JPEG_VALUE);
            }

            if (StringUtils.isBlank(storage.getId())) {
                storage.setId(IdGen.uuid());
                storage.setCreateDate(new Date());
            }

            saveFile(new FileInputStream(file), storage);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void saveFile(InputStream inputStream, Storage storage) {
        File localFile;
        FileOutputStream outputStream = null;

        try {
            localFile = getConverter().toFile(storage);
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

        StorageServiceHolder.getService().add(storage);
    }

    @NonNull
    public static StorageVo entityToVo(Storage entity) {
        if (entity == null) {
            return new StorageVo();
        }

        StorageVo vo = new StorageVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName() + "." + entity.getExtendName());
        vo.setMimeType(entity.getMimeType());
        vo.setUrl(getConverter().toPreviewUrl(entity));

        return vo;
    }
}
