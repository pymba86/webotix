package ru.webotix.common;

import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import io.dropwizard.setup.Environment;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.commons.io.IOUtils;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

public class UrlRewriteEnvironment implements EnvironmentInitialiser {
    @Override
    public void init(Environment environment) {
        FilterRegistration.Dynamic urlRewriteFilter =
                environment.servlets().addFilter("UrlRewriteFilter", new UrlRewriteFilterFixed());
        urlRewriteFilter.addMappingForUrlPatterns(null, true, "/*");
        urlRewriteFilter.setInitParameter("confPath", "urlrewrite.xml");
    }

    /**
     * https://github.com/paultuckey/urlrewritefilter/pull/225 and can be removed if a 4.0.2+ version
     * of UrlRewriteFilter is released.
     */
    private static final class UrlRewriteFilterFixed extends UrlRewriteFilter {

        @Override
        protected void loadUrlRewriter(FilterConfig filterConfig) throws ServletException {
            String confPath = filterConfig.getInitParameter("confPath");
            ServletContext context = filterConfig.getServletContext();
            try {
                InputStream config =
                        IOUtils.toInputStream(
                                "<urlrewrite>\n"
                                        + "    <rule>\n"
                                        + "        <from>^/?(addCoin|scripts|job|coin).*$</from>\n"
                                        + "        <to type=\"forward\">/index.html</to>\n"
                                        + "    </rule>\n"
                                        + "</urlrewrite>",
                                StandardCharsets.UTF_8);
                Conf conf = new Conf(context, config, confPath, "HARDCODED", false);
                checkConf(conf);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }
}
