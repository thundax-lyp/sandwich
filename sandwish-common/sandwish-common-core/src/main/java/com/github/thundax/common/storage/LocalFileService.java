package com.github.thundax.common.storage;

import com.github.thundax.common.utils.StringUtils;
import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.io.FileUtils;
import org.springframework.lang.NonNull;

public class LocalFileService implements MetaFileService {

    private static final int CHUNK_SIZE = 4096;

    private String pathPrefix = StringUtils.EMPTY;

    public LocalFileService() {}

    /** get/set pathPrefix */
    public String getPathPrefix() {
        return this.pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        try {
            this.pathPrefix = new File(pathPrefix).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private File metaFileToFile(MetaFile metaFile) {
        File file = new File(this.pathPrefix + metaFile.getPathname());
        try {
            if (file.getCanonicalPath().startsWith(this.pathPrefix)) {
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(this.pathPrefix);
    }

    @NonNull
    private MetaFile fileToMetaFile(File file) {
        MetaFile metaFile = new MetaFile();
        fileToMetaFile(file, metaFile);
        return metaFile;
    }

    private void fileToMetaFile(File file, MetaFile metaFile) {
        String pathname = file.getAbsolutePath();
        if (!StringUtils.startsWith(pathname, pathPrefix)) {
            metaFile.setPathname(null);
            metaFile.setLength(0);
            metaFile.setLastModified(null);

        } else {
            metaFile.setPathname(StringUtils.substring(pathname, pathPrefix.length()));
            metaFile.setLength(file.length());
            metaFile.setLastModified(new Date(file.lastModified()));
            if (file.isDirectory()) {
                metaFile.setType(MetaFile.TYPE_FOLDER);
            } else if (file.isFile()) {
                metaFile.setType(MetaFile.TYPE_FILE);
            }
        }
    }

    @Override
    public boolean ensureObject(MetaFile metaFile) {
        File file = this.metaFileToFile(metaFile);
        if (!file.exists()) {
            return false;
        } else {
            fileToMetaFile(file, metaFile);
            return true;
        }
    }

    @Override
    public boolean exists(MetaFile metaFile) {
        File file = this.metaFileToFile(metaFile);
        return file.exists();
    }

    private List<MetaFile> list(MetaFile metaFolder, Function<File, Boolean> filter) {
        List<MetaFile> mfList = Lists.newArrayList();

        if (!metaFolder.isFolder()) {
            return mfList;
        }

        File folder = metaFileToFile(metaFolder);

        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (filter == null || filter.apply(file)) {
                    mfList.add(this.fileToMetaFile(file));
                }
            }
            Collections.sort(mfList);
        }

        return mfList;
    }

    @Override
    public List<MetaFile> list(MetaFile metaFolder) {
        return list(metaFolder, null);
    }

    @Override
    public List<MetaFile> listFolders(MetaFile metaFolder) {
        return list(metaFolder, File::isDirectory);
    }

    @Override
    public List<MetaFile> listFiles(MetaFile metaFolder) {
        return list(metaFolder, File::isFile);
    }

    @Override
    public boolean rename(MetaFile src, MetaFile dest) {
        File srcFile = this.metaFileToFile(src);
        File destFile = this.metaFileToFile(dest);
        try {
            if (srcFile.isDirectory()) {
                FileUtils.moveDirectory(srcFile, destFile);
            } else {
                FileUtils.moveFile(srcFile, destFile);
            }
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean createFolder(MetaFile metaFolder) {
        File file = this.metaFileToFile(metaFolder);
        try {
            return file.mkdirs();

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteFolder(MetaFile metaFolder) {
        try {
            return FileUtils.deleteQuietly(this.metaFileToFile(metaFolder));

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean copyFolder(MetaFile src, MetaFile dest) {
        try {
            FileUtils.copyDirectory(this.metaFileToFile(src), this.metaFileToFile(dest));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteFile(MetaFile metaFile) {
        try {
            FileUtils.deleteQuietly(this.metaFileToFile(metaFile));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean copyFile(MetaFile src, MetaFile dest) {
        try {
            FileUtils.copyFile(this.metaFileToFile(src), this.metaFileToFile(dest));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean writeFile(MetaFile metaFile, byte[] b, int off, int len) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(b, off, len);
        try {
            return writeFile(metaFile, inputStream);

        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean writeFile(MetaFile metaFile, InputStream is) {
        FileOutputStream outputStream = null;
        try {
            File file = this.metaFileToFile(metaFile);
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    return false;
                }
                if (!file.createNewFile()) {
                    return false;
                }
            }

            outputStream = new FileOutputStream(file);

            byte[] chunkBuffer = new byte[CHUNK_SIZE];
            int readBytes;
            do {
                readBytes = is.read(chunkBuffer, 0, chunkBuffer.length);
                if (readBytes > 0) {
                    outputStream.write(chunkBuffer, 0, readBytes);
                }
            } while (readBytes >= 0);

            return true;

        } catch (IOException e) {
            return false;

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String readFileAsString(MetaFile metaFile, Charset charset) throws IOException {
        return FileUtils.readFileToString(this.metaFileToFile(metaFile), charset);
    }

    @Override
    public byte[] readFileAsByteArray(MetaFile metaFile) throws IOException {
        return FileUtils.readFileToByteArray(this.metaFileToFile(metaFile));
    }

    @Override
    public InputStream readFileAsStream(MetaFile metaFile) throws IOException {
        return new FileInputStream(this.metaFileToFile(metaFile));
    }
}
