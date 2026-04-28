package com.github.thundax.modules.sys.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 上传文件持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_upload_file")
public class UploadFileDO {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField(exist = false)
    private boolean isNewRecord;

    private String name;

    @TableField("extend_name")
    private String extendName;

    @TableField("mime_type")
    private String mimeType;

    private Long size;

    private String path;

    @TableField(exist = false)
    private byte[] content;

    @TableField(exist = false)
    private Integer priority;

    @TableField(exist = false)
    private String remarks;

    @TableField("create_date")
    private Date createDate;

    @TableField(exist = false)
    private String createUserId;

    @TableField(exist = false)
    private Date updateDate;

    @TableField(exist = false)
    private String updateUserId;

    @TableField(exist = false)
    private String delFlag;

    public UploadFileDO(String id) {
        this.id = id;
    }
}
