package com.sethsaperstein.flinkcontrolapi;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.flink.annotation.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ReverseProxyFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ReverseProxyFilter.class);
    private static final String SQL_GATEWAY_PORT = "8080";
    private final RestTemplate restTemplate;

    @Autowired
    public ReverseProxyFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (shouldProxy(httpRequest.getRequestURI())) {
            String targetUrl = constructTargetUrl(httpRequest.getRequestURI());
            logger.info("Proxying request to {}", targetUrl);

            // Create a new HTTP request to the target URL
            HttpMethod httpMethod = HttpMethod.valueOf(httpRequest.getMethod());
            HttpHeaders headers = new HttpHeaders();

            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = httpRequest.getHeader(headerName);
                headers.set(headerName, headerValue);
            }

            HttpEntity<?> httpEntity = new HttpEntity<>(headers);

            // Send the request and retrieve the response
            ResponseEntity<String> responseEntity = restTemplate.exchange(targetUrl, httpMethod, httpEntity, String.class);

            // Set the response status and body based on the target response
            httpResponse.setStatus(responseEntity.getStatusCode().value());
            httpResponse.getWriter().write(responseEntity.getBody());
            httpResponse.getWriter().flush();
        } else {
            // Continue the filter chain for non-proxied requests
            chain.doFilter(request, response);
        }
    }

    @VisibleForTesting
    static String constructTargetUrl(String requestUri) {
        String clusterName = extractClusterName(requestUri);
        if (clusterName == null) {
            throw new IllegalArgumentException("Invalid request URI. Cluster name cannot be extracted.");
        }
        String service = String.format("%s-sql-gateway", clusterName);
        String newUri = requestUri.replaceFirst(String.format("^/sql/%s/", clusterName), "");
        return String.format("http://%s.%s:%s/%s", service, clusterName, SQL_GATEWAY_PORT, newUri);
    }

    static private String extractClusterName(String requestUri) {
        // Extract the cluster name from the request URI that should match /sql/{clusterName}/v#/
        String regexPattern = "^/sql/([^/]+)/v\\d+/.*$";

        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(requestUri);

        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private boolean shouldProxy(String requestUri) {
        // path /sql/{cluster_name}/v1/... to proxy to service
        return requestUri.startsWith("/sql");
    }

}

