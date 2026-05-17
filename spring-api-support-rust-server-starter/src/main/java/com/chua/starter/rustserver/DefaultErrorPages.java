package com.chua.starter.rustserver;

/**
 * 内置错误页 HTML 模板。
 */
public class DefaultErrorPages {

    public static String renderHtml(int statusCode, String message) {
        String title = getTitle(statusCode);
        String description = message != null ? message : getDescription(statusCode);

        return "<!DOCTYPE html>" +
                "<html lang=\"zh-CN\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>" + statusCode + " - " + title + "</title>" +
                "<style>" +
                "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; " +
                "  display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; " +
                "  background: #f5f5f5; color: #333; }" +
                ".error-box { text-align: center; padding: 2rem; }" +
                ".error-code { font-size: 6rem; font-weight: 700; color: #e74c3c; margin: 0; " +
                "  line-height: 1; }" +
                ".error-title { font-size: 1.5rem; margin: 0.5rem 0; color: #555; }" +
                ".error-desc { font-size: 1rem; color: #888; margin: 1rem 0; }" +
                "hr { border: none; border-top: 1px solid #ddd; margin: 1.5rem 0; }" +
                ".server-info { font-size: 0.75rem; color: #aaa; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"error-box\">" +
                "<div class=\"error-code\">" + statusCode + "</div>" +
                "<div class=\"error-title\">" + title + "</div>" +
                "<div class=\"error-desc\">" + description + "</div>" +
                "<hr>" +
                "<div class=\"server-info\">RustServer (tokio+hyper)</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private static String getTitle(int statusCode) {
        switch (statusCode) {
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 408: return "Request Timeout";
            case 429: return "Too Many Requests";
            case 500: return "Internal Server Error";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            default:  return "Error";
        }
    }

    private static String getDescription(int statusCode) {
        switch (statusCode) {
            case 400: return "服务器无法理解请求的格式。";
            case 401: return "请求未包含身份验证信息。";
            case 403: return "没有权限访问此资源。";
            case 404: return "请求的资源不存在。";
            case 405: return "不支持的请求方法。";
            case 408: return "服务器等待请求时超时。";
            case 429: return "请求过于频繁，请稍后重试。";
            case 500: return "服务器内部错误，请稍后重试。";
            case 502: return "上游服务器返回无效响应。";
            case 503: return "服务器暂时无法处理请求。";
            case 504: return "上游服务器响应超时。";
            default:  return "请求处理时发生错误。";
        }
    }
}
