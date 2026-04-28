package com.github.thundax.modules.sys.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_upload_file")
public class UploadFileDO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String extendName;

    private String mimeType;

    private Long size;

    private String path;




    private Date createDate;




}
