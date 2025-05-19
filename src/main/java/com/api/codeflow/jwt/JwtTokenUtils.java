package com.api.codeflow.jwt;

import com.api.codeflow.repository.RoleRepository;
import com.api.codeflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class JwtTokenUtils {
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @Value("jwt.secret")
    private String jwtSecret;

    @Value("jwt.lifetime")
    private Duration lifetime;


}
