package com.github.thundax.modules.sys.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.BaseVo;
import com.github.thundax.common.vo.UserVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/** @author wdit */
@ApiModel(value = "UserTreeNode", description = "组织机构-用户树节点")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserTreeNodeVo extends BaseVo {

    private static final long serialVersionUID = 1L;

    private String parentId;
    private String name;

    private UserVo user;

    public UserTreeNodeVo() {
        super();
    }

    public UserTreeNodeVo(String id) {
        super(id);
    }

    @ApiModelProperty(name = "parentId", value = "父节点ID")
    @JsonProperty("parentId")
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @ApiModelProperty(name = "name", value = "名称")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(name = "user", value = "用户，为空则是部门")
    @JsonProperty("user")
    public UserVo getUser() {
        return user;
    }

    public void setUser(UserVo user) {
        this.user = user;
    }
}
