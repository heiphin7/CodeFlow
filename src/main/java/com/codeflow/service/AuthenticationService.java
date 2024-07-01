package com.codeflow.service;

import com.codeflow.models.User;
import com.codeflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                String.format("Пользователь %s не найден", username)
                        )
                );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),

                user.getRoles().stream().map(
                    role -> new SimpleGrantedAuthority(role.getName())
                ).collect(Collectors.toList())
        );

    }
}
