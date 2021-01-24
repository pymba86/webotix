package ru.webotix.auth;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractHttpServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No-op
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(response instanceof HttpServletResponse)) {
            return;
        }
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public abstract void doFilter(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException;

    @Override
    public void destroy() {
        // No-op
    }
}
