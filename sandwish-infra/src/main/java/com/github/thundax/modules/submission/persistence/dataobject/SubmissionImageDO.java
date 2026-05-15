package com.github.thundax.modules.submission.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("submission_image")
public class SubmissionImageDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long submissionId;
    private Long storageObjectId;
    private Integer sortOrder;
}
