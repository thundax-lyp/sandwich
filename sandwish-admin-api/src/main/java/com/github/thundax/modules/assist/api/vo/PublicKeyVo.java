package com.github.thundax.modules.assist.api.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.common.vo.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author wdit
 */
@ApiModel(value = "PublicKeyVo", description = "公钥")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicKeyVo extends BaseVo {

    private String publicKey;

    public PublicKeyVo() {
        super();
    }

    public PublicKeyVo(String id) {
        super(id);
    }


    @ApiModelProperty(name = "publicKey", value = "公钥")
    @JsonProperty("publicKey")
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

}
