package icecream;

import org.apache.log4j.Logger;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class DummyUserDetailsService implements UserDetailsService {


    private static final Logger logger = Logger.getLogger(DummyUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.debug("Looking up user with name: '" + username + "'");

        return new User(username, "pwd", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }

}
