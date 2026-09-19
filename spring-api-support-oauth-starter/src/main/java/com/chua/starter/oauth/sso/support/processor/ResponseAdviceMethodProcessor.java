package com.chua.starter.oauth.sso.support.processor;

import com.chua.starter.oauth.sso.support.advice.AdviceView;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.io.IOException;
import java.util.List;

/**
 * 响应加载器
 *
 * @author CH
 * @since 4.0.0
 */
public class ResponseAdviceMethodProcessor extends RequestResponseBodyMethodProcessor {

    /**
     * Redirectpatterns
    */
    private String redirectPatterns;

    /**
     * 创建 响应advice方法处理器 实例
     * @param converters 转换器
     */
    public ResponseAdviceMethodProcessor(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    /**
     * 支持参数
    */
    public boolean supportsParameter(MethodParameter returnType) {
        Class<?> paramType = returnType.getParameterType();
        return AdviceView.class.isAssignableFrom(paramType);
    }

    @Override
    /**
     * 支持返回类型
    */
    public boolean supportsReturnType(MethodParameter returnType) {
        Class<?> paramType = returnType.getParameterType();
        return AdviceView.class.isAssignableFrom(paramType);
    }

    @Override
    /**
     * 处理返回值
    */
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
        AdviceView adviceView = (AdviceView) returnValue;
        Object data = adviceView.getData();

        String accept = webRequest.getHeader("accept");
        if (!accept.contains("text/html")) {
            mavContainer.setRequestHandled(true);
            ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
            ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

 // 尝试 even with 空 返回 值. 响应主体advice could 获取 involved.
            writeWithMessageConverters(data, returnType, inputMessage, outputMessage);
            return;
        }

        if (data instanceof CharSequence) {
            String viewName = data.toString();
            mavContainer.setViewName(viewName);
            if (isRedirectViewName(viewName)) {
                mavContainer.setRedirectModelScenario(true);
            }
        } else if (returnValue != null) {
            // should not happen
            throw new UnsupportedOperationException("Unexpected return type: " +
                    returnType.getParameterType().getName() + " in method: " + returnType.getMethod());
        }
    }


    /**
     * Whether the given view 名称 是否 a redirect view 引用.
     * The 默认 implementation 检查 the configured redirect 模式 和
     * also if the view 名称 启动 with the "redirect:" 前缀.
     *
     * @param viewName the view 名称 转为 检查, 从不 {@code null}
     * @return "true" if the given view 名称 是否 recognized as a redirect view
     * 引用; "false" otherwise.
     */
    protected boolean isRedirectViewName(String viewName) {
        return (PatternMatchUtils.simpleMatch(this.redirectPatterns, viewName) || viewName.startsWith("redirect:"));
    }
}
