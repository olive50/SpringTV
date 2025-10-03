package com.tvboot.tivio.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID = "traceId";
    private static final String START_TIME = "startTime";
    private static final String USER_ID = "userId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // Générer ou récupérer trace ID
        String traceId = request.getHeader("X-Trace-Id");
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }

        // Configurer MDC pour le contexte de logging
        MDC.put(TRACE_ID, traceId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("remoteAddr", getClientIpAddress(request));
        MDC.put("userAgent", request.getHeader("User-Agent"));

        // Stocker le temps de début
        request.setAttribute(START_TIME, System.currentTimeMillis());

        // Ajouter trace ID à la réponse
        response.setHeader("X-Trace-Id", traceId);

        log.info("==> {} {} - Started", request.getMethod(), request.getRequestURI());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        // Post-traitement si nécessaire
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {

        try {
            Long startTime = (Long) request.getAttribute(START_TIME);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

            MDC.put("duration", String.valueOf(duration));
            MDC.put("status", String.valueOf(response.getStatus()));

            String logLevel = response.getStatus() >= 400 ? "ERROR" : "INFO";

            if ("ERROR".equals(logLevel)) {
                log.error("<== {} {} - Completed with status {} in {}ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            } else {
                log.info("<== {} {} - Completed with status {} in {}ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            }

            if (ex != null) {
                log.error("Request completed with exception", ex);
            }

        } finally {
            // Nettoyer le MDC
            MDC.clear();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}