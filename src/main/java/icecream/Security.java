package icecream;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.web.authentication.www.DigestAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.DigestAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * Created by kosmachevskiy on 04.05.17.
 */

@Configuration
public class Security extends WebSecurityConfigurerAdapter {

    private static final Logger logger = Logger.getLogger(Security.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling()
                .authenticationEntryPoint(digestAuthenticationEntry()).and()

                .authorizeRequests()
                .antMatchers("/secret**").authenticated()
                .antMatchers("/public").anonymous().and()
                .addFilter(digestFilter())
                .addFilterAfter(new MyFilter("/public"), DigestAuthenticationFilter.class)

                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Autowired
    private UserDetailsService userDetailsService;

    DigestAuthenticationFilter digestFilter() {

        DigestAuthenticationFilter filter = new DigestAuthenticationFilter();
        filter.setAuthenticationEntryPoint(digestAuthenticationEntry());
        filter.setUserDetailsService(userDetailsService);

        try {
            filter.setUserCache(digestUserCache());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filter;
    }

    @Bean
    UserCache digestUserCache() throws Exception {
        return new SpringCacheBasedUserCache(new ConcurrentMapCache("digestUserCache"));
    }

    @Bean
    DigestAuthenticationEntryPoint digestAuthenticationEntry() {
        DigestAuthenticationEntryPoint digestAuthenticationEntry = new DigestAuthenticationEntryPoint();
        digestAuthenticationEntry.setRealmName("REALM");
        digestAuthenticationEntry.setKey("GRM");
//        digestAuthenticationEntry.setNonceValiditySeconds(60);
        return digestAuthenticationEntry;
    }

    private class MyFilter extends GenericFilterBean {

        private String path;

        public MyFilter(String path) {
            this.path = path;
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException {

            HttpServletRequest request = (HttpServletRequest) servletRequest;

            if (match(request)) {
                logger.debug("Request to open API: " + request);

                String header = request.getHeader("API-KEY");

                if (Objects.isNull(header)) {
                    HttpServletResponse response = (HttpServletResponse) servletResponse;

                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "API-KEY needed");
                    return;
                }
            }

            filterChain.doFilter(servletRequest, servletResponse);

        }

        private boolean match(HttpServletRequest request) {
            String pathInfo = request.getPathInfo();

            if (pathInfo.startsWith(path))
                return true;
            else
                return false;
        }

        @Override
        public void destroy() {

        }
    }

}
