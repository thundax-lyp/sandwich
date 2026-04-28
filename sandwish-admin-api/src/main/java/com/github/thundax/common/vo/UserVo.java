package com.github.thundax.common.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.modules.sys.api.vo.OfficeVo;
import com.github.thundax.modules.sys.api.vo.RoleVo;
import com.github.thundax.modules.sys.utils.SysApiUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@ApiModel(value = "User", description = "用户")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserVo extends BaseVo {

    public static final String ORDER_BY_NAME_ASC = "name_asc";
    public static final String ORDER_BY_NAME_DESC = "name_desc";
    public static final String ORDER_BY_LOGIN_NAME_ASC = "loginName_asc";
    public static final String ORDER_BY_LOGIN_NAME_DESC = "loginName_desc";
    public static final String ORDER_BY_ENABLE_ASC = "enable_asc";
    public static final String ORDER_BY_ENABLE_DESC = "enable_desc";

    private String loginName;
    private String loginPass;
    private Integer ranks;

    private String name;
    private String email;
    private String mobile;
    private String avatar;

    private Boolean isSuper;
    private Boolean isAdmin;
    private Boolean isEnable;

    private Date registerDate;
    private String registerIp;
    private Date lastLoginDate;
    private String lastLoginIp;
    private String ssoLoginName;
    private String token;

    private OfficeVo office;
    private List<RoleVo> roleList;

    public UserVo() {
        super();
    }

    public UserVo(String id) {
        super(id);
    }

    @ApiModelProperty(name = "loginName", value = "登录名")
    @JsonProperty("loginName")
    @NotEmpty(message = "\"登录名\"不能为空")
    @Size(max = 30, message = "\"登录名\"长度不能超过 30")
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @ApiModelProperty(name = "loginPass", value = "登录密码")
    @JsonProperty("loginPass")
    @Pattern(regexp = SysApiUtils.PASSWORD_VALIDATE_PATTERN, message = SysApiUtils.PASSWORD_VALIDATE_MESSAGE)
    public String getLoginPass() {
        return loginPass;
    }

    public void setLoginPass(String loginPass) {
        this.loginPass = loginPass;
    }

    @ApiModelProperty(name = "ranks", value = "等级")
    @JsonProperty("ranks")
    @Min(value = 0, message = "等级不能小于0")
    @Max(value = 9, message = "等级不能超过9")
    public Integer getRanks() {
        return ranks;
    }

    public void setRanks(Integer ranks) {
        this.ranks = ranks;
    }

    @ApiModelProperty(name = "name", value = "姓名")
    @JsonProperty("name")
    @NotEmpty(message = "\"姓名\"不能为空")
    @Size(max = 50, message = "\"姓名\"长度不能超过 50")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(name = "email", value = "邮箱")
    @JsonProperty("email")
    @Size(max = 50, message = "\"邮箱\"长度不能超过 50")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ApiModelProperty(name = "mobile", value = "手机号")
    @JsonProperty("mobile")
    @Size(max = 30, message = "\"手机号\"长度不能超过 30")
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @ApiModelProperty(name = "avatar", value = "头像链接地址")
    @JsonProperty("avatar")
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @ApiModelProperty(name = "superAdmin", value = "是否系统管理员")
    @JsonProperty("superAdmin")
    public Boolean getSuper() {
        return isSuper;
    }

    public void setSuper(Boolean aSuper) {
        isSuper = aSuper;
    }

    @ApiModelProperty(name = "admin", value = "是否管理员")
    @JsonProperty("admin")
    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    @ApiModelProperty(name = "enable", value = "启用/禁用")
    @JsonProperty("enable")
    public Boolean getEnable() {
        return isEnable;
    }

    public void setEnable(Boolean enable) {
        isEnable = enable;
    }

    @ApiModelProperty(name = "registerDate", value = "注册时间")
    @JsonProperty("registerDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    @ApiModelProperty(name = "registerIp", value = "注册IP")
    @JsonProperty("registerIp")
    public String getRegisterIp() {
        return registerIp;
    }

    public void setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
    }

    @ApiModelProperty(name = "lastLoginDate", value = "最后登录时间")
    @JsonProperty("lastLoginDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @ApiModelProperty(name = "lastLoginIp", value = "最后登录IP")
    @JsonProperty("lastLoginIp")
    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    @ApiModelProperty(name = "office", value = "归属组织机构")
    @JsonProperty("office")
    public OfficeVo getOffice() {
        return office;
    }

    public void setOffice(OfficeVo office) {
        this.office = office;
    }

    @ApiModelProperty(name = "roles", value = "权限")
    @JsonProperty("roles")
    public List<RoleVo> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<RoleVo> roleList) {
        this.roleList = roleList;
    }

    @ApiModelProperty(name = "ssoLoginName", value = "sso登录名")
    @JsonProperty("ssoLoginName")
    public String getSsoLoginName() {
        return ssoLoginName;
    }

    @ApiModelProperty(name = "token", value = "令牌")
    @JsonProperty("token")
    @NotEmpty(message = "\"token\"不能为空")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
