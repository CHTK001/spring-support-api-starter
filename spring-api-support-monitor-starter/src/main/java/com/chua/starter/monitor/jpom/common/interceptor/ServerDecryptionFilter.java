/// *
// * Copyright (c) 2019 Of Him Code Technology Studio
// * Jpom is licensed under Mulan PSL v2.
// * You can use this software according to the terms and conditions of the Mulan PSL v2.
// * You may obtain a copy of Mulan PSL v2 at:
// * 			http://license.coscl.org.cn/MulanPSL2
// * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
// * See the Mulan PSL v2 for more details.
// */
//package com.chua.starter.monitor.jpom.common.interceptor;
//
//import cn.hutool.core.util.ObjectUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.extra.servlet.JakartaServletUtil;
//import cn.hutool.http.ContentType;
//import cn.hutool.http.Method;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.tomcat.util.http.fileupload.FileUploadBase;
//import com.chua.starter.monitor.jpom.common.i18n.I18nMessageUtil;
//import com.chua.starter.monitor.jpom.common.transport.BodyRewritingRequestWrapper;
//import com.chua.starter.monitor.jpom.common.transport.MultipartRequestWrapper;
//import com.chua.starter.monitor.jpom.common.transport.ParameterRequestWrapper;
//import com.chua.starter.monitor.jpom.configuration.WebConfig;
//import com.chua.starter.monitor.jpom.encrypt.EncryptFactory;
//import com.chua.starter.monitor.jpom.encrypt.Encryptor;
//import com.chua.starter.monitor.jpom.system.ServerConfig;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.MediaType;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletRequestWrapper;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
///**
// * @author bwcx_jzy
// * @since 2024/2/22
// */
//@Configuration
//@Slf4j
//@Order(1)
//public class ServerDecryptionFilter implements Filter {
//
//    private final Encryptor encryptor;
//
//    public ServerDecryptionFilter(ServerConfig serverConfig) {
//        Encryptor encryptor1;
//        WebConfig config = serverConfig.getWeb();
//        String transportEncryption = config.getTransportEncryption();
//        transportEncryption = ObjectUtil.defaultIfNull(transportEncryption, StrUtil.EMPTY).toUpperCase();
//        switch (transportEncryption) {
//            case "NONE":
//                encryptor1 = null;
//                break;
//            case "BASE64":
//                try {
//                    encryptor1 = EncryptFactory.createEncryptor(1);
//                } catch (Exception e) {
//                    log.error(I18nMessageUtil.get("i18n.get_decrypt_implementation_failure.e77a"), e);
//                    encryptor1 = null;
//                }
//                break;
//            default:
//                log.warn(I18nMessageUtil.get("i18n.unsupported_encoding_with_placeholder.3bd9"), transportEncryption);
//                encryptor1 = null;
//                break;
//        }
//        encryptor = encryptor1;
//    }
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        HttpServletRequest request = (HttpServletRequest) servletRequest;
//        if (encryptor == null) {
//            chain.doFilter(servletRequest, response);
//            return;
//        }
//        log.debug(I18nMessageUtil.get("i18n.request_needs_decoding.d4d7"), encryptor.name());
//        String contentType = request.getContentType();
//        String method = request.getMethod();
//        if (ContentType.isDefault(contentType)) {
//            // 普通表单
//            HttpServletRequestWrapper wrapper = new ParameterRequestWrapper(request, encryptor);
//            chain.doFilter(wrapper, response);
//        } else if (StrUtil.startWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE)) {
//            String body = JakartaServletUtil.getBody(request);
//            String temp;
//            try {
//                temp = encryptor.decrypt(body);
//            } catch (Exception e) {
//                log.error(I18nMessageUtil.get("i18n.decode_failure.822e"), e);
//                temp = body;
//            }
//            BodyRewritingRequestWrapper requestWrapper = new BodyRewritingRequestWrapper(request, temp.getBytes(StandardCharsets.UTF_8));
//            chain.doFilter(requestWrapper, response);
//        } else if (StrUtil.equalsIgnoreCase(Method.POST.name(), method) && StrUtil.startWith(contentType, FileUploadBase.MULTIPART)) {
//            // 文件上传
//            HttpServletRequestWrapper wrapper = new MultipartRequestWrapper(request, encryptor);
//            chain.doFilter(wrapper, response);
//        } else {
//            log.warn(I18nMessageUtil.get("i18n.request_type_not_supported_for_decoding.ea2e"), contentType);
//            chain.doFilter(servletRequest, response);
//        }
//    }
//}
