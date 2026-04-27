package com.github.thundax.common.storage;

import com.github.thundax.common.storage.filter.ResourceFilter;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

public class ResourceService {

    private MetaFileService metaFileService;
    private ResourceFilter filter;

    public ResourceService() {}

    public ResourceService(MetaFileService metaFileService) {
        this.setMetaFileService(metaFileService);
    }

    /** get/set MetaFileService */
    public MetaFileService getMetaFileService() {
        return this.metaFileService;
    }

    public void setMetaFileService(MetaFileService metaFileService) {
        this.metaFileService = metaFileService;
    }

    /** get/set filter */
    public ResourceFilter getFilter() {
        return this.filter;
    }

    public void setFilter(ResourceFilter filter) {
        this.filter = filter;
    }

    /** 获得列表（文件和目录） */
    public List<Resource> list(String path) {
        return this.list(path, this.filter);
    }

    public List<Resource> list(String path, ResourceFilter filter) {
        return this.createObjectList(metaFileService.list(createMetaFolder(path)), filter);
    }

    /** 获得列表（目录） */
    public List<Resource> listFolders(String path) {
        return this.listFolders(path, this.filter);
    }

    public List<Resource> listFolders(String path, ResourceFilter filter) {
        return this.createObjectList(metaFileService.listFolders(createMetaFolder(path)), filter);
    }

    /** 获得文件列表 */
    public List<Resource> listFiles(String path) {
        return this.listFiles(path, this.filter);
    }

    public List<Resource> listFiles(String path, ResourceFilter filter) {
        return this.createObjectList(metaFileService.listFiles(createMetaFolder(path)), filter);
    }

    /** 创建目录 */
    public Resource createFolder(String path) {
        MetaFile metaFolder = createMetaFolder(path);
        if (metaFileService.createFolder(metaFolder)) {
            return new Resource(metaFolder);
        }
        return null;
    }

    /** 删除目录 */
    public boolean deleteFolder(String path) {
        return metaFileService.deleteFolder(createMetaFolder(path));
    }

    /** 拷贝目录 */
    public boolean copyFolder(String src, String dest) {
        return metaFileService.copyFolder(createMetaFolder(src), createMetaFolder(dest));
    }

    /** 目录/文件重命名 */
    public boolean rename(String src, String dest) {
        return metaFileService.rename(new MetaFile(src), new MetaFile(dest));
    }

    public boolean exists(Resource file) {
        return metaFileService.exists(file.getMetaFile());
    }

    public Resource createFile(String pathname, boolean createIfNotExist) {
        MetaFile metaFile = new MetaFile(pathname);
        if (createIfNotExist && !metaFileService.exists(metaFile)) {
            metaFileService.writeFile(metaFile, new byte[0], 0, 0);
        }
        return new Resource(metaFile);
    }

    public Resource ensureObject(String pathname) {
        MetaFile metaFile = new MetaFile(pathname);
        if (metaFileService.ensureObject(metaFile)) {
            return new Resource(metaFile);
        } else {
            return null;
        }
    }

    public boolean write(Resource file, byte[] b, int off, int len) {
        return metaFileService.writeFile(file.getMetaFile(), b, off, len);
    }

    public boolean write(Resource file, String content, Charset charset) {
        byte[] b = content.getBytes(charset);
        return metaFileService.writeFile(file.getMetaFile(), b, 0, b.length);
    }

    public String readFileAsString(Resource file, Charset charset) {
        try {
            return metaFileService.readFileAsString(file.getMetaFile(), charset);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] readFileAsByteArray(Resource file) {
        try {
            return metaFileService.readFileAsByteArray(file.getMetaFile());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** 删除文件 */
    public boolean deleteFile(String pathname) {
        MetaFile metaFile = new MetaFile(pathname);
        if (metaFileService.ensureObject(metaFile)) {
            if (metaFile.isFolder()) {
                return metaFileService.deleteFolder(metaFile);
            } else {
                return metaFileService.deleteFile(metaFile);
            }
        }
        return true;
    }

    public int deleteFiles(String[] pathnames) {
        int count = 0;
        if (pathnames != null) {
            for (String pathname : pathnames) {
                if (deleteFile(pathname)) {
                    count++;
                }
            }
        }
        return count;
    }

    private MetaFile createMetaFolder(String path) {
        return new MetaFile(path + MetaFile.SEPARATOR);
    }

    /** 根据T的类型，创建对象列表，并赋予metaFile列表 */
    private List<Resource> createObjectList(List<MetaFile> metaFileList, ResourceFilter filter) {
        List<Resource> fileList = Lists.newArrayList();

        for (MetaFile metaFile : metaFileList) {
            Resource file = new Resource(metaFile);
            if (filter == null || filter.test(file)) {
                fileList.add(file);
            }
        }
        Collections.sort(fileList);

        return fileList;
    }
}
