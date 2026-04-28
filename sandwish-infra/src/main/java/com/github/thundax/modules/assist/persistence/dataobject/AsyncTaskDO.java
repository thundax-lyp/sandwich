package com.github.thundax.modules.assist.persistence.dataobject;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 异步任务持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
public class AsyncTaskDO {

    private String id;
    private boolean isNewRecord;

    private String title;
    private String status;
    private String message;
    private String data;
    private Boolean isPrivate;
    private Integer expiredSeconds;
    private Integer priority;
    private String remarks;
    private Date createDate;
    private String createUserId;
    private Date updateDate;
    private String updateUserId;
    private String delFlag;
}
