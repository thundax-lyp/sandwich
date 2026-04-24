package com.github.thundax.modules.sys.api.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.query.PageQueryParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @author wdit
 */
@ApiModel(value = "LogQueryParam", description = "日志查询参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogQueryParam extends PageQueryParam {

    private String title;
    private String userLoginName;
    private String userName;
    private String remoteAddr;
    private String requestUri;

    private Date beginDate;
    private Date endDate;

    @ApiModelProperty(name = "title", value = "标题")
    @JsonProperty("title")
    @Size(max = 50, message = "\"标题\"长度不能超过 50")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(name = "userLoginName", value = "用户登录名")
    @JsonProperty("userLoginName")
    @Size(max = 50, message = "\"用户登录名\"长度不能超过 50")
    public String getUserLoginName() {
        return userLoginName;
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName = userLoginName;
    }

    @ApiModelProperty(name = "userName", value = "用户名")
    @JsonProperty("userName")
    @Size(max = 50, message = "\"用户名\"长度不能超过 50")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @ApiModelProperty(name = "remoteAddr", value = "来源")
    @JsonProperty("remoteAddr")
    @Size(max = 50, message = "\"来源\"长度不能超过 50")
    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @ApiModelProperty(name = "requestUri", value = "请求地址")
    @JsonProperty("requestUri")
    @Size(max = 500, message = "\"请求地址\"长度不能超过 500")
    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    @ApiModelProperty(name = "beginDate", value = "开始时间，格式: yyyy-MM-dd HH:mm:ss")
    @JsonProperty("beginDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    @ApiModelProperty(name = "endDate", value = "结束时间，格式: yyyy-MM-dd HH:mm:ss")
    @JsonProperty("endDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
