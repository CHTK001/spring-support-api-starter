package com.chua.starter.project.service.support.hikvision;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author CH
 */
public class HiYunMouHttpClientResult implements Serializable {
    private static final long serialVersionUID = -8206843202914904822L;
    private static final int SUCCESS = 200;
    private Integer code;
    private String data;
    private String cause;

    public HiYunMouHttpClientResult() {
    }

    public HiYunMouHttpClientResult(CloseableHttpResponse response) throws IOException {
        this.code = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        this.data = EntityUtils.toString(entity, "UTF-8");
        if (this.code != 200) {
            this.cause = response.getStatusLine().getReasonPhrase();
        }

    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCause() {
        return this.cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Boolean isSuccess() {
        return this.code == 200;
    }

    public String toString() {
        return "HttpClientResult{code=" + this.code + ", data=" + this.data + ", cause='" + this.cause + '\'' + '}';
    }
}
