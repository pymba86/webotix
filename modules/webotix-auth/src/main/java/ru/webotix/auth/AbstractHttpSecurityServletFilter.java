package ru.webotix.auth;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpSecurityServletFilter extends AbstractHttpServletFilter {

    private static final Logger log =
            LoggerFactory.getLogger(AbstractHttpSecurityServletFilter.class);

    @Override
    public final void doFilter(
            HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain)
            throws IOException, ServletException {
        log.debug(
                "Request to {} : {} : {}",
                httpRequest.getContextPath(),
                httpRequest.getServletPath(),
                httpRequest.getPathInfo());

        // TODO security-bypassed URI patterns should be provided on a modular
        // plugin basis
        if ("/favicon.ico".equals(httpRequest.getServletPath())
                || "/favicon.ico".equals(httpRequest.getPathInfo())
                || (httpRequest.getPathInfo() != null && httpRequest.getPathInfo().startsWith("/auth"))) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        if (filterHttpRequest(httpRequest, httpResponse)) {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    protected abstract boolean filterHttpRequest(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException;
}
