package com.github.thundax.common.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public interface MetaFileService {

    /**
     * 确认对象，并装载
     */
    boolean ensureObject(MetaFile file);

    /**
     * 检查文件/目录是否存在
     */
    boolean exists(MetaFile file);

    /**
     * 获取文件列表（文件和目录）
     */
    List<MetaFile> list(MetaFile folder);

    /**
     * 获取文件列表（目录）
     */
    List<MetaFile> listFolders(MetaFile folder);

    /**
     * 获取文件列表（文件）
     */
    List<MetaFile> listFiles(MetaFile folder);

    /**
     * 移动文件夹/文件（重新命名）
     */
    boolean rename(MetaFile src, MetaFile dest);

    /**
     * 创建文件夹
     */
    boolean createFolder(MetaFile folder);

    /**
     * 删除文件夹，包括子文件夹，文件
     */
    boolean deleteFolder(MetaFile folder);

    /**
     * 拷贝文件夹，包括子文件，子目录
     */
    boolean copyFolder(MetaFile src, MetaFile dest);

    /**
     * 删除文件
     */
    boolean deleteFile(MetaFile file);

    /**
     * 拷贝文件夹，包括子文件，子目录
     */
    boolean copyFile(MetaFile src, MetaFile dest);

    /**
     * 写文件，如果文件不存在则先创建文件
     */
    boolean writeFile(MetaFile file, byte[] b, int off, int len);

    boolean writeFile(MetaFile file, InputStream is);

    /**
     * 读取文件
     */
    String readFileAsString(MetaFile file, Charset charset) throws IOException;

    byte[] readFileAsByteArray(MetaFile file) throws IOException;

    InputStream readFileAsStream(MetaFile file) throws IOException;
}
