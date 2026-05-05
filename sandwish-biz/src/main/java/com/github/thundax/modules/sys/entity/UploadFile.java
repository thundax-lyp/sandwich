package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadFile {
    private static final String IMG_EXT = "png,jpg,jpeg";

    private EntityId id;

    private String name;
    private String extendName;
    private String mimeType;
    private Long size;
    private String path;
    private byte[] content;
    private Date createDate;

    public boolean isImage() {
        try {
            String[] suffixes = IMG_EXT.split(",");
            String filename = getName().toLowerCase();
            for (String s : suffixes) {
                if (filename.endsWith("." + s)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
