package com.github.thundax.modules.assist.plugins.koal.sign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 签名响应参数
 *
 * @author wdit
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignResponseParam extends BaseResponse {

    /** B64 签名结果 * */
    private String b64SignedData;
    /** B64 签名证书 * */
    private String b64Cert;

    public SignResponseParam() {}

    public String getB64Cert() {
        return b64Cert;
    }

    public void setB64Cert(String b64Cert) {
        this.b64Cert = b64Cert;
    }

    public String getB64SignedData() {
        return b64SignedData;
    }

    public void setB64SignedData(String b64SignedData) {
        this.b64SignedData = b64SignedData;
    }
}
