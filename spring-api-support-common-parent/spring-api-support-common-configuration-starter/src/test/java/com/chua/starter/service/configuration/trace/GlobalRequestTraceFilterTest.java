package com.chua.starter.service.configuration.trace;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** @author CH */
class GlobalRequestTraceFilterTest {

    @Test
    void recordsEventForAnnotatedHandlerOnly() throws Exception {
        RequestTraceAnalyticsService analyticsService = mock(RequestTraceAnalyticsService.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.containsBean("requestStatisticsIngestionService")).thenReturn(false);
        when(analyticsService.record(any(RequestTraceEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        GlobalRequestTraceFilter filter = new GlobalRequestTraceFilter(analyticsService, applicationContext);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/demo/42");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36");
        request.addHeader("X-Forwarded-For", "10.10.10.1");
        request.setAttribute(
                HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE,
                new HandlerMethod(new DemoController(), DemoController.class.getMethod("annotated"))
        );
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/demo/{id}");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.setStatus(201);
            }
        });
        filter.doFilter(request, response, chain);

        ArgumentCaptor<RequestTraceEvent> captor = ArgumentCaptor.forClass(RequestTraceEvent.class);
        verify(analyticsService, times(1)).record(captor.capture());
        RequestTraceEvent event = captor.getValue();

        assertThat(event.strategyCode()).isEqualTo("demo:annotated");
        assertThat(event.method()).isEqualTo("GET");
        assertThat(event.path()).isEqualTo("/api/demo/42");
        assertThat(event.routePattern()).isEqualTo("/api/demo/{id}");
        assertThat(event.clientIp()).isEqualTo("10.10.10.1");
        assertThat(event.browser()).isEqualTo("Chrome");
        assertThat(event.durationMs()).isGreaterThanOrEqualTo(0L);
        assertThat(event.status()).isEqualTo(201);
        assertThat(event.userId()).isNull();
        assertThat(event.signals()).isEqualTo(Set.of());
        assertThat(request.getAttribute(RequestTraceConstants.ATTRIBUTE_TRACE_ENABLED)).isEqualTo(Boolean.TRUE);
    }

    @Test
    void skipsEventWhenHandlerHasNoAnnotation() throws Exception {
        RequestTraceAnalyticsService analyticsService = mock(RequestTraceAnalyticsService.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.containsBean("requestStatisticsIngestionService")).thenReturn(false);
        GlobalRequestTraceFilter filter = new GlobalRequestTraceFilter(analyticsService, applicationContext);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/demo/plain");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36");
        request.setAttribute(
                HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE,
                new HandlerMethod(new DemoController(), DemoController.class.getMethod("plain"))
        );
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/demo/plain");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());

        verifyNoInteractions(analyticsService);
        assertThat(request.getAttribute(RequestTraceConstants.ATTRIBUTE_TRACE_ENABLED)).isEqualTo(Boolean.FALSE);
    }

    private static final class DemoController {

        @RequestTraceStrategy("demo:annotated")
        public void annotated() {
        }

        public void plain() {
        }
    }
}

