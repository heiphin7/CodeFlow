package com.api.codeflow.controller;

import com.api.codeflow.dto.AuthDto;
import com.api.codeflow.dto.RegisterDto;
import com.api.codeflow.exception.EmailIsTakenException;
import com.api.codeflow.exception.UsernameIsTakenException;
import com.api.codeflow.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto dto) {
        log.info("Request to register: " + dto);
        try {
            userService.register(dto);
            return ResponseEntity.ok("User registered successfully");
        } catch (UsernameIsTakenException | EmailIsTakenException | IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while saving new User: " + e.getMessage());
            return new ResponseEntity<>("Server error :(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto dto) {
        log.info("request to login: " + dto);
        try {
            return ResponseEntity.ok(userService.login(dto));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Error while authenticate user: " + e);
            return new ResponseEntity<>("Server error :(", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
