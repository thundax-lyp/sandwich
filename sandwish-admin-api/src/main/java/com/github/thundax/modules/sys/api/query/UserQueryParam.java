package com.github.thundax.modules.sys.api.query;

import static com.github.thundax.common.vo.UserVo.ORDER_BY_ENABLE_ASC;
import static com.github.thundax.common.vo.UserVo.ORDER_BY_ENABLE_DESC;
import static com.github.thundax.common.vo.UserVo.ORDER_BY_LOGIN_NAME_ASC;
import static com.github.thundax.common.vo.UserVo.ORDER_BY_LOGIN_NAME_DESC;
import static com.github.thundax.common.vo.UserVo.ORDER_BY_NAME_ASC;
import static com.github.thundax.common.vo.UserVo.ORDER_BY_NAME_DESC;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.query.PageQueryParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Size;

/**
 * UserQueryParam
 *
 * @author wdit
 */
@ApiModel(value = "UserQueryParam", description = "用户查询参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserQueryParam extends PageQueryParam {

    private String officeId;
    private String loginName;
    private String name;
    private Boolean enable;
    private String orderBy;

    @ApiModelProperty(name = "officeId", value = "组织机构ID")
    @JsonProperty("officeId")
    @Size(max = 64, message = "\"组织机构ID\"长度不能超过64")
    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    @ApiModelProperty(name = "loginName", value = "登录名，模糊查询")
    @JsonProperty("loginName")
    @Size(max = 30, message = "\"登录名\"长度不能超过 30")
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @ApiModelProperty(name = "name", value = "姓名，模糊查询")
    @JsonProperty("name")
    @Size(max = 30, message = "\"姓名\"长度不能超过 30")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(name = "enable", value = "启用/禁用")
    @JsonProperty("enable")
    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    @ApiModelProperty(
            name = "orderBy",
            value = "排序规则。\n"
                    + ORDER_BY_NAME_ASC
                    + ": 名称正序\n"
                    + ORDER_BY_NAME_DESC
                    + ": 名称倒序\n"
                    + ORDER_BY_LOGIN_NAME_ASC
                    + ": 登录名正序\n"
                    + ORDER_BY_LOGIN_NAME_DESC
                    + ": 登录名倒序\n"
                    + ORDER_BY_ENABLE_ASC
                    + ": 启用/禁用正序\n"
                    + ORDER_BY_ENABLE_DESC
                    + ": 启用/禁用倒序\n"
                    + "",
            allowableValues = ""
                    + ORDER_BY_NAME_ASC
                    + ","
                    + ORDER_BY_NAME_DESC
                    + ","
                    + ORDER_BY_LOGIN_NAME_ASC
                    + ","
                    + ORDER_BY_LOGIN_NAME_DESC
                    + ","
                    + ORDER_BY_ENABLE_ASC
                    + ","
                    + ORDER_BY_ENABLE_DESC
                    + ","
                    + "",
            example = ORDER_BY_NAME_ASC)
    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
