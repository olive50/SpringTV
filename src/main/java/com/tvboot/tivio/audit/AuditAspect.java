package com.tvboot.tivio.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Informations utilisateur
        String username = getCurrentUsername();

        // Configurer le contexte MDC
        MDC.put("auditAction", auditable.action());
        MDC.put("auditResource", auditable.resource());
        MDC.put("auditUser", username);
        MDC.put("auditMethod", className + "." + methodName);

        try {
            // Log avant exécution
            if (auditable.logParams()) {
                log.info("AUDIT_START - Action: {} | Resource: {} | User: {} | Method: {} | Params: {}",
                        auditable.action(),
                        auditable.resource(),
                        username,
                        className + "." + methodName,
                        formatParameters(joinPoint.getArgs()));
            } else {
                log.info("AUDIT_START - Action: {} | Resource: {} | User: {} | Method: {}",
                        auditable.action(),
                        auditable.resource(),
                        username,
                        className + "." + methodName);
            }

            // Exécuter la méthode
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("auditDuration", String.valueOf(duration));

            // Log après exécution réussie
            if (auditable.logResult() && result != null) {
                log.info("AUDIT_SUCCESS - Action: {} | Duration: {}ms | Result: {}",
                        auditable.action(),
                        duration,
                        formatResult(result));
            } else {
                log.info("AUDIT_SUCCESS - Action: {} | Duration: {}ms",
                        auditable.action(),
                        duration);
            }

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("AUDIT_FAILURE - Action: {} | Duration: {}ms | Error: {} | Message: {}",
                    auditable.action(),
                    duration,
                    e.getClass().getSimpleName(),
                    e.getMessage());

            throw e;

        } finally {
            // Nettoyer le MDC
            MDC.remove("auditAction");
            MDC.remove("auditResource");
            MDC.remove("auditUser");
            MDC.remove("auditMethod");
            MDC.remove("auditDuration");
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        return "anonymous";
    }

    private String formatParameters(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        try {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append(", ");

                if (args[i] == null) {
                    sb.append("null");
                } else if (args[i] instanceof String) {
                    sb.append("\"").append(args[i]).append("\"");
                } else if (args[i] instanceof Number) {
                    sb.append(args[i]);
                } else {
                    // Pour les objets complexes, limiter la taille
                    String json = objectMapper.writeValueAsString(args[i]);
                    if (json.length() > 200) {
                        sb.append(json, 0, 200).append("...");
                    } else {
                        sb.append(json);
                    }
                }
            }
            sb.append("]");
            return sb.toString();
        } catch (Exception e) {
            return "[Error formatting parameters: " + e.getMessage() + "]";
        }
    }

    private String formatResult(Object result) {
        try {
            if (result instanceof String) {
                return "\"" + result + "\"";
            } else if (result instanceof Number || result instanceof Boolean) {
                return result.toString();
            } else {
                String json = objectMapper.writeValueAsString(result);
                if (json.length() > 300) {
                    return json.substring(0, 300) + "...";
                }
                return json;
            }
        } catch (Exception e) {
            return "[Error formatting result: " + e.getMessage() + "]";
        }
    }
}