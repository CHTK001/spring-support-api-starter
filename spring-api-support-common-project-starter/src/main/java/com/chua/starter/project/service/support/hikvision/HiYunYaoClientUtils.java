package com.chua.starter.project.service.support.hikvision;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.chua.common.support.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author CH
 */
@Slf4j
public class HiYunYaoClientUtils {
    public static final String DEFAULT_CHARSET = "UTF-8";
    private static final int CONNECT_TIMEOUT = 4000;
    private static final int SOCKET_TIMEOUT = 15000;
    private static final int REQUESTCONNECT_TIMEOUT = 2000;
    private static final int CONNECT_TOTAL = 400;
    private static final int CONNECT_ROUTE = 5;
    private static PoolingHttpClientConnectionManager connManager = null;
    private static CloseableHttpClient httpClient = null;

    static {
        log.info("create httpClient start");
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        SSLConnectionSocketFactory sslsf = null;
        SSLContextBuilder builder = new SSLContextBuilder();

        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (Exception var8) {
            log.error("", var8);
        }

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainsf).register("https", sslsf).build();
        connManager = new PoolingHttpClientConnectionManager(registry);
        if (ObjectUtils.isNotEmpty(connManager)) {
            connManager.setMaxTotal(400);
            connManager.setDefaultMaxPerRoute(5);
            SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(15000).build();
            connManager.setDefaultSocketConfig(socketConfig);
            RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(2000).setConnectTimeout(4000).setSocketTimeout(15000).build();
            HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    if (executionCount >= 3) {
                        return false;
                    } else if (exception instanceof NoHttpResponseException) {
                        log.info("出现NoHttpResponseException异常");
                        return true;
                    } else if (exception instanceof SSLHandshakeException) {
                        return false;
                    } else if (exception instanceof InterruptedIOException) {
                        return false;
                    } else if (exception instanceof UnknownHostException) {
                        return false;
                    } else {
                        return false;
                    }
                }
            };
            HttpClientBuilder httpBuilder = HttpClients.custom().setConnectionManager(connManager).setDefaultRequestConfig(requestConfig).setRetryHandler(httpRequestRetryHandler);
            httpClient = httpBuilder.build();
            if (connManager.getTotalStats() != null) {
                log.info("now client pool " + connManager.getTotalStats().toString());
            }

            log.info("create httpClient finished");
        }

    }


    public static HiYunMouHttpClientResult sendPostResponse(String reqURL, Object param) {
        return sendPostResponse(reqURL, null, param, false, null, null);
    }

    public static HiYunMouHttpClientResult sendPostResponse(String reqURL, Map<String, String> headers, Object param) {
        return sendPostResponse(reqURL, headers, param, false, null, null);
    }

    public static HiYunMouHttpClientResult sendPostResponse(String reqURL, Map<String, String> headers, Object param, boolean isFormReq, String charset, String urlParam) {
        if (connManager != null && connManager.getTotalStats() != null) {
            log.info("sendPostResponseWithTokent pool " + connManager.getTotalStats().toString());
        }

        HiYunMouHttpClientResult result = null;
        CloseableHttpResponse response = null;

        try {
            if (ObjectUtils.isNotEmpty(urlParam)) {
                reqURL = reqURL + "?" + urlParam;
            }

            HttpPost httpPost = new HttpPost(reqURL);
            if (ObjectUtils.isNotEmpty(headers)) {
                Iterator var10 = headers.entrySet().iterator();

                while (var10.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry) var10.next();
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            charset = getCharset(charset);
            HttpEntity httpEntity = getEntity(param, isFormReq, charset);
            if (httpEntity != null) {
                httpPost.setEntity(httpEntity);
            }

            log.info("开始执行请求：" + reqURL);
            response = httpClient.execute(httpPost, HttpClientContext.create());
            result = new HiYunMouHttpClientResult(response);
        } catch (Exception var14) {
            log.error("请求通信[" + reqURL + "]时异常,堆栈轨迹如下", var14);
        } finally {
            closeHttpRequest(response);
        }

        return result;
    }

    public static HiYunMouHttpClientResult sendGetResponse(String reqURL, Map<String, String> headers, String urlParam) {
        if (connManager != null && connManager.getTotalStats() != null) {
            log.info("sendPostResponseWithTokent pool " + connManager.getTotalStats().toString());
        }

        HiYunMouHttpClientResult result = null;
        CloseableHttpResponse response = null;

        try {
            if (ObjectUtils.isNotEmpty(urlParam)) {
                reqURL = reqURL + "?" + urlParam;
            }

            HttpGet httpGet = new HttpGet(reqURL);
            if (ObjectUtils.isNotEmpty(headers)) {
                Iterator var10 = headers.entrySet().iterator();

                while (var10.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry) var10.next();
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }

            log.info("开始执行请求：" + reqURL);
            response = httpClient.execute(httpGet, HttpClientContext.create());
            result = new HiYunMouHttpClientResult(response);
        } catch (Exception var14) {
            log.error("请求通信[" + reqURL + "]时异常,堆栈轨迹如下", var14);
        } finally {
            closeHttpRequest(response);
        }

        return result;
    }

    private static HttpEntity getEntity(Object param, boolean isFormReq, String charset) throws UnsupportedEncodingException {
        if (param == null) {
            log.info("当前未传入参数信息，无法生成HttpEntity");
            return null;
        } else {
            String paramsStr = null;
            if (param instanceof Map) {
                Map<String, String> paramsMap = (Map) param;
                if (isFormReq) {
                    List<NameValuePair> list = getNameValuePairs(paramsMap);
                    return new UrlEncodedFormEntity(list, charset);
                }

                paramsStr = Json.toJson(param);
            } else if (param instanceof String) {
                paramsStr = (String) param;
            } else {
                paramsStr = Json.toJson(param);
            }

            StringEntity httpEntity = new StringEntity(paramsStr, charset);
            if (!paramsStr.startsWith("{") && !paramsStr.startsWith("[")) {
                if (paramsStr.startsWith("<")) {
                    httpEntity.setContentType(ContentType.APPLICATION_XML.getMimeType());
                } else {
                    String contentType = "application/x-www-form-urlencoded";
                    if (charset != null) {
                        contentType = contentType + ";charset=" + charset;
                    }

                    httpEntity.setContentType(contentType);
                }
            } else {
                httpEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            }

            return httpEntity;
        }
    }

    private static List<NameValuePair> getNameValuePairs(Map<String, String> paramsMap) {
        List<NameValuePair> list = new ArrayList();
        if (paramsMap != null && !paramsMap.isEmpty()) {
            Iterator var3 = paramsMap.entrySet().iterator();

            while (var3.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry) var3.next();
                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            return list;
        } else {
            return list;
        }
    }

    private static String getCharset(String charset) {
        return charset == null ? "UTF-8" : charset;
    }

    private static void closeHttpRequest(CloseableHttpResponse response) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }

        response = null;
    }

    public static void releaseHttpClient() {
        log.info("***********关闭httpClient开始*********");
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception var1) {
                log.error("关闭httpClient异常" + var1);
            }
        }

        if (connManager != null) {
            connManager.shutdown();
        }

        log.info("***********关闭httpClient结束*********");
    }
}
