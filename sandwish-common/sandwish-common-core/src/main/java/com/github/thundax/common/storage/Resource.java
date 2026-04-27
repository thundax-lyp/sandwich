package com.github.thundax.common.storage;

import java.util.Date;

/**
 * 基础资源类，基于MetaFile
 *
 * @param <T>
 */
public class Resource implements Comparable<Resource> {

    private MetaFile metaFile;

    public Resource() {

    }

    public Resource(MetaFile metaFile) {
        this.setMetaFile(metaFile);
    }

    /**
     * get/set metaFile
     */
    public MetaFile getMetaFile() {
        return this.metaFile;
    }

    public void setMetaFile(MetaFile metaFile) {
        this.metaFile = metaFile;
    }

    /**
     * get name
     */
    public String getName() {
        return this.metaFile.getName();
    }

    public String getExtName() {
        return this.metaFile.getExtName();
    }

    /**
     * get path
     */
    public String getPath() {
        return this.metaFile.getPath();
    }

    /**
     * get pathname
     */
    public String getPathname() {
        return this.metaFile.getPathname();
    }

    /**
     * get lastModified
     */
    public Date getLastModified() {
        return this.metaFile.getLastModified();
    }

    /**
     * get length
     */
    public long getLength() {
        return this.metaFile.getLength();
    }

    /**
     * is file/folder
     */
    public boolean isFile() {
        return this.metaFile.isFile();
    }

    public boolean isFolder() {
        return this.metaFile.isFolder();
    }

    @Override
    public int compareTo(Resource that) {
        return getMetaFile().compareTo(that.getMetaFile());
    }

}
