package com.github.thundax.modules.submission.persistence.dataobject;

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
@TableName("submission_submission")
public class SubmissionDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String title;
    private String content;
    private String sourceClientId;

    private String status;

    private Integer priority;
    private Date submittedAt;
    private Date lastStatusChangedAt;
}
