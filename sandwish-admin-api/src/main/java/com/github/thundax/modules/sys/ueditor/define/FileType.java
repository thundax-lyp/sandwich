package com.github.thundax.modules.sys.ueditor.define;

import java.util.HashMap;
import java.util.Map;

public class FileType {

    public static final String JPG = "JPG";

    @SuppressWarnings("serial")
    private static final Map<String, String> TYPES = new HashMap<String, String>() {
        {
            put(FileType.JPG, ".jpg");
        }
    };

    public static String getSuffix(String key) {
        return FileType.TYPES.get(key);
    }

    public static String getSuffixByFilename(String filename) {

        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}
