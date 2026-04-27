package com.github.thundax.modules.sys.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/** @author wdit */
@ApiModel(value = "Role", description = "权限")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleVo extends BaseVo {

    private String name;
    private Boolean isAdmin;
    private Boolean isEnable;

    private List<MenuVo> menuList;

    public RoleVo() {
        super();
    }

    public RoleVo(String id) {
        super(id);
    }

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    @NotEmpty(message = "\"名称\"不能为空")
    @Size(max = 50, message = "\"名称\"长度不能超过 50")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(name = "admin", value = "是否管理权限")
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
        this.isEnable = enable;
    }

    @ApiModelProperty(name = "menus", value = "菜单列表")
    @JsonProperty("menus")
    public List<MenuVo> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<MenuVo> menuList) {
        this.menuList = menuList;
    }
}
