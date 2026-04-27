package com.github.thundax.modules.utils;

import com.github.thundax.common.storage.MetaFile;
import com.github.thundax.common.utils.FileUtils;
import com.github.thundax.common.utils.StringUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.coobird.thumbnailator.Thumbnails;

/** @author wdit */
public class AvatarUtils {

    private static final String AVATAR_PATH = "user";
    private static final String AVATAR_FILENAME = "avatar.jpg";

    private static final int MAX_AVATAR_WIDTH = 400;
    private static final int MAX_AVATAR_HEIGHT = 400;

    private static final float IMAGE_QUALITY = 0.8f;

    private static File storageParent;

    public static void setStoragePath(String storagePath) {
        if (StringUtils.isBlank(storagePath)) {
            storageParent = new File(File.pathSeparator, AVATAR_PATH);

        } else {
            storageParent = new File(storagePath, AVATAR_PATH);
        }
    }

    public static File getAvatarFile(String userId) {
        return new File(new File(storageParent, userId), AVATAR_FILENAME);
    }

    public static String getAvatarFilename(String userId) {
        return MetaFile.SEPARATOR + userId + MetaFile.SEPARATOR + AVATAR_FILENAME;
    }

    public static boolean existAvatar(String userId) {
        return getAvatarFile(userId).exists();
    }

    public static void saveAvatar(String userId, InputStream inputStream) throws IOException {
        File avatarFile = getAvatarFile(userId);
        File avatarFolder = avatarFile.getParentFile();

        if (!avatarFolder.exists() && !avatarFolder.mkdirs()) {
            throw new FileNotFoundException();
        }

        // 压缩图片
        Thumbnails.Builder<?> builder = Thumbnails.of(inputStream);
        BufferedImage image = builder.scale(1.0f).asBufferedImage();

        int originWidth = image.getWidth();
        int originHeight = image.getHeight();
        if (originWidth <= 0 || originHeight <= 0) {
            throw new FileNotFoundException();
        }

        if (originWidth > MAX_AVATAR_WIDTH || originHeight > MAX_AVATAR_HEIGHT) {
            // 缩放图片
            double scale =
                    Math.min(
                            (double) MAX_AVATAR_WIDTH / (double) originWidth,
                            (double) MAX_AVATAR_HEIGHT / (double) originHeight);
            builder = Thumbnails.of(image);
            builder.scale(scale);
        } else {
            builder = Thumbnails.of(image);
            builder.size(originWidth, originHeight);
        }

        builder.outputFormat("jpg");
        builder.outputQuality(IMAGE_QUALITY);

        builder.toOutputStream(new FileOutputStream(avatarFile));
    }

    public static void deleteAvatar(String userId) {
        FileUtils.deleteQuietly(getAvatarFile(userId));
    }
}
