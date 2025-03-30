package com.io.spring_boot_archetype.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
@Component
public class HttpAuditFilter extends OncePerRequestFilter {

    /**
     * This method is called for each request to log the audit information.
     * <p>
     * Logs both the incoming request and the outgoing response
     * in a structured format, suitable for Elasticsearch/Kibana.
     *
     * @param request     the HttpServletRequest object
     * @param response    the HttpServletResponse object
     * @param filterChain the FilterChain object
     * @throws ServletException if an error occurs during filtering
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // Request log
        log.info(""" 
                        { "type": "REQUEST", "method": "{}", "path": "{}", "query": "{}", "remoteIp": "{}" } """,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                request.getRemoteAddr()
        );

        try {
            filterChain.doFilter(request, wrappedResponse);
        } finally {

            // Response log
            log.info(""" 
                            { "type": "RESPONSE", "status": {}, "path": "{}" } """,
                    wrappedResponse.getStatus(),
                    request.getRequestURI()
            );

            wrappedResponse.copyBodyToResponse();
        }
    }
}
