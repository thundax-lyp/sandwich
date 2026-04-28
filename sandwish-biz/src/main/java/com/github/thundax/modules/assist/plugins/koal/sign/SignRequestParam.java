package com.github.thundax.modules.assist.plugins.koal.sign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * 签名请求参数
 *
 * @author wdit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignRequestParam implements Serializable {

    private String b64OriginData;

    private String certAlias;

    private String serviceName;

    public SignRequestParam(String b64OriginData) {
        this.b64OriginData = b64OriginData;
    }

    public SignRequestParam(String b64OriginData, String certAlias) {
        this.b64OriginData = b64OriginData;
        this.certAlias = certAlias;
    }

    public SignRequestParam(String b64OriginData, String certAlias, String serviceName) {
        this.b64OriginData = b64OriginData;
        this.certAlias = certAlias;
        this.serviceName = serviceName;
    }

    public String getB64OriginData() {
        return b64OriginData;
    }

    public void setB64OriginData(String b64OriginData) {
        this.b64OriginData = b64OriginData;
    }

    public String getCertAlias() {
        return certAlias;
    }

    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
