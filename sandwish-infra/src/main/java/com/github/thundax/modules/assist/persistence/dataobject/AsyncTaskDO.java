package com.github.thundax.modules.assist.persistence.dataobject;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AsyncTaskDO {

    private Long id;
    private String title;
    private String status;
    private String message;
    private String data;
    private Boolean isPrivate;
    private Integer expiredSeconds;
    private Integer priority;
    private String remarks;
}
