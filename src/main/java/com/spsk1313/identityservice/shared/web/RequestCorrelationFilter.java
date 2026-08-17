package com.spsk1313.identityservice.shared.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestCorrelationFilter extends OncePerRequestFilter {

    private static final String REQUEST_HEADER = "X-Request-ID";
    private static final String REQUEST_ID = "requestId";
    private static final Logger log = LoggerFactory.getLogger(RequestCorrelationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String xRequestId = request.getHeader(REQUEST_HEADER);
            if (xRequestId == null || xRequestId.isBlank()) {
                xRequestId = UUID.randomUUID().toString();
            }
            MDC.put(REQUEST_ID, xRequestId);
            log.debug("Processing HTTP request: {} {}", request.getMethod(), request.getRequestURI());
            response.addHeader(REQUEST_HEADER, xRequestId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID);
        }
    }
}
