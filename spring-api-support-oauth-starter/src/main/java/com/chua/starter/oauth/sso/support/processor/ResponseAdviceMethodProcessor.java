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
     * Whether the given view name is a redirect view reference.
     * The default implementation checks the configured redirect patterns and
     * also if the view name starts with the "redirect:" prefix.
     *
     * @param viewName the view name to check, never {@code null}
     * @return "true" if the given view name is recognized as a redirect view
     * reference; "false" otherwise.
     */
    protected boolean isRedirectViewName(String viewName) {
        return (PatternMatchUtils.simpleMatch(this.redirectPatterns, viewName) || viewName.startsWith("redirect:"));
    }
}
