package icecream;

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

/**
 * Created by kosmachevskiy on 04.05.17.
 */

@Configuration
public class Security extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling()
                .authenticationEntryPoint(digestAuthenticationEntry()).and()

                .authorizeRequests()
                .antMatchers("/public").anonymous()
                .antMatchers("/**").authenticated().and()

                .addFilter(digestFilter())

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
        digestAuthenticationEntry.setRealmName("GAURAVBYTES.COM");
        digestAuthenticationEntry.setKey("GRM");
        digestAuthenticationEntry.setNonceValiditySeconds(60);
        return digestAuthenticationEntry;
    }

}
