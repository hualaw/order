package com.interview.order.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class SimpleUserDetailsService implements UserDetailsService {

    @Value("${security.test-user.name:testuser}")
    private String username;

    @Value("${security.test-user.password:testpass}")
    private String password;

    @Override
    public UserDetails loadUserByUsername(String user) throws UsernameNotFoundException {
        if (!username.equals(user)) throw new UsernameNotFoundException("User not found");
        return new User(username, "{noop}" + password, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}

