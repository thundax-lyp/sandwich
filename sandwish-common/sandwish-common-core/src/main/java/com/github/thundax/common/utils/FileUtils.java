package com.github.thundax.common.utils;

import com.github.thundax.common.utils.ext.FileUtilEx;
import java.io.File;
import java.io.IOException;

/** @author thundax */
public class FileUtils extends FileUtilEx {

    /** copy a file to a new location */
    public static void replaceCopyFile(String srcFilename, String destFilename, boolean replace)
            throws IOException {
        File srcFile = new File(srcFilename);
        File destFile = new File(destFilename);

        replaceCopyFile(srcFile, destFile, replace);
    }

    public static void replaceCopyFile(File srcFile, File destFile, boolean replace)
            throws IOException {
        if (replace && destFile.exists() && destFile.isFile()) {
            destFile.delete();
        }
        copyFile(srcFile, destFile);
    }
}
