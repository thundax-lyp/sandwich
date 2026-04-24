package com.github.thundax.modules.sys.entity;

import com.github.thundax.modules.sys.entity.base.BaseUploadFile;

public class UploadFile extends BaseUploadFile {

    public UploadFile() {
    }

    public UploadFile(String id) {
        super(id);
    }

    private static final String imgExt = "png,jpg,jpeg";

    public boolean isImage(){
        try {
            String[] suffixes = imgExt.split(",");
            String filename = getName().toLowerCase();
            for (String s : suffixes) {
                if (filename.endsWith("." + s)) {
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}
