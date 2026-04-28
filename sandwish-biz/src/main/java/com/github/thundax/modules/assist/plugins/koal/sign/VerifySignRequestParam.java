package com.github.thundax.modules.assist.plugins.koal.sign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * 验签请求参数
 *
 * @author wdit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifySignRequestParam implements Serializable {

    /** B64 签名原文（必传） * */
    private String b64OriginData;
    /** B64 签名结果（必传） * */
    private String b64SignedData;
    /** B64 签名证书（非必传） * */
    private String b64Cert;
    /** 证书别名（非必传） * */
    private String certAlias;
    /** 服务名称（非必传） * */
    private String serviceName;

    public VerifySignRequestParam() {}

    public VerifySignRequestParam(String b64OriginData, String b64SignedData) {
        this(b64OriginData, b64SignedData, "", "", "");
    }

    public VerifySignRequestParam(String b64OriginData, String b64SignedData, String certAlias, String serviceName) {
        this(b64OriginData, b64SignedData, "", certAlias, serviceName);
    }

    public VerifySignRequestParam(
            String b64OriginData, String b64SignedData, String b64Cert, String certAlias, String serviceName) {
        this.b64OriginData = b64OriginData;
        this.b64SignedData = b64SignedData;
        this.b64Cert = b64Cert;
        this.certAlias = certAlias;
        this.serviceName = serviceName;
    }

    public String getB64OriginData() {
        return b64OriginData;
    }

    public void setB64OriginData(String b64OriginData) {
        this.b64OriginData = b64OriginData;
    }

    public String getB64SignedData() {
        return b64SignedData;
    }

    public void setB64SignedData(String b64SignedData) {
        this.b64SignedData = b64SignedData;
    }

    public String getB64Cert() {
        return b64Cert;
    }

    public void setB64Cert(String b64Cert) {
        this.b64Cert = b64Cert;
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
