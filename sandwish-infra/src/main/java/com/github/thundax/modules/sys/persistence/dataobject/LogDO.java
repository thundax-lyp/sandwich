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

/** 日志持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_log")
public class LogDO {

    @TableId(type = IdType.INPUT)
    private String id;

    @TableField("user_id")
    private String userId;

    private String type;

    @TableField("log_date")
    private Date logDate;

    private String title;

    @TableField("remote_addr")
    private String remoteAddr;

    @TableField("user_agent")
    private String userAgent;

    private String method;

    @TableField("request_uri")
    private String requestUri;

    @TableField("request_params")
    private String requestParams;

    @TableField(exist = false)
    private Integer priority;

    @TableField(exist = false)
    private String remarks;

    @TableField(exist = false)
    private Date createDate;

    @TableField(exist = false)
    private String createUserId;

    @TableField(exist = false)
    private Date updateDate;

    @TableField(exist = false)
    private String updateUserId;

    @TableField(exist = false)
    private String delFlag;
}
