package com.github.thundax.common.storage;

import com.github.thundax.common.utils.FilenameUtils;
import com.github.thundax.common.utils.StringUtils;
import java.io.Serializable;
import java.util.Date;

public class MetaFile implements Comparable<MetaFile>, Serializable {

    private static final long serialVersionUID = 1L;

    /** 路径分隔符，与File相同 */
    public static final char SEPARATOR_CHAR = '/';

    public static final String SEPARATOR = "/";

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_FOLDER = 2;

    private String name; // 文件名
    private String path; // 全路径名，必须以separator结尾
    private long length; // 长度
    private Date lastModified; // 最后更新时间

    private int type; // 类型，TYPE_FILE, TYPE_DIRECTORY

    public MetaFile() {
        this.name = StringUtils.EMPTY;
        this.path = StringUtils.EMPTY;
        this.length = 0;
        this.type = TYPE_UNKNOWN;
    }

    public MetaFile(String path, String name) {
        this(path + MetaFile.SEPARATOR + name);
    }

    public MetaFile(String pathname) {
        this();
        this.setPathname(pathname);
    }

    /** get/set name */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** 获取文件全路径名 */
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /** 获取文件全路径名+文件名 */
    public String getPathname() {
        String pathname = StringUtils.EMPTY;
        if (StringUtils.isNotEmpty(this.getPath())) {
            pathname = pathname + this.getPath();
        }

        if (StringUtils.isNotEmpty(this.getName())) {
            pathname = pathname + SEPARATOR + this.getName();
        }
        return pathname;
    }

    public void setPathname(String pathname) {
        String canonicalPathname = FilenameUtils.getCanonicalPathname(pathname);
        if (canonicalPathname.endsWith(SEPARATOR)) {
            this.setType(TYPE_FOLDER);
            this.setPath(canonicalPathname.substring(0, canonicalPathname.length() - 1));
            this.setName(null);

        } else {
            this.setType(TYPE_FILE);
            int pos = StringUtils.lastIndexOf(pathname, SEPARATOR);
            this.setPath(StringUtils.substring(pathname, 0, pos));
            this.setName(StringUtils.substring(pathname, pos + 1));
        }
    }

    /** 获取文件扩展名 */
    public String getExtName() {
        return FilenameUtils.getExtension(this.name);
    }

    /** 获取文件尺寸 */
    public long getLength() {
        return this.length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    /** 获取最后更新时间 */
    public Date getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /** 获取文件类型 */
    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /** 判断是否是目录 */
    public boolean isFolder() {
        return this.type == TYPE_FOLDER;
    }

    /** 判断是否是文件 */
    public boolean isFile() {
        return this.type == TYPE_FILE;
    }

    @Override
    public int compareTo(MetaFile that) {
        return this.getPathname().compareTo(that.getPathname());
    }
}
