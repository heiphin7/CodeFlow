package com.api.codeflow.configuration;

import com.api.codeflow.jwt.JwtTokenUtils;
import com.api.codeflow.model.User;
import com.api.codeflow.model.UserDetails;
import com.api.codeflow.repository.UserRepository;
import com.api.codeflow.service.UserDetailsService;
import com.api.codeflow.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenUtils jwtTokenUtils;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        log.info("METHOD: {}, PATH: {}", request.getMethod(), request.getRequestURI());

        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);

            try {
                username = jwtTokenUtils.getUsername(jwt);
            } catch (ExpiredJwtException e) { // todo: log exceptions
                username = e.getClaims().getSubject();

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                User user = userRepository.findByUsername(username).orElse(null);

                if (user == null || userDetails == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("Unauthorized - subject not founded");
                    response.getWriter().flush();
                    return;
                }

                // Если accessToken истек но при этом RefreshToken валиден
                if (jwtTokenUtils.validateToken(user.getRefreshToken())) {
                    String newAccessToken = jwtTokenUtils.generateAccessToken(userDetails);
                    response.setHeader("Authorization", "Bearer " + newAccessToken);

                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(token);

                    filterChain.doFilter(request, response);
                    return;
                } else {
                    // Если и refresh и access истекли -> 401
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("Unauthorized - token expired");
                    response.getWriter().flush();
                    return;
                }

            } catch (SignatureException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Unauthorized - signature exception");
                response.getWriter().flush();
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<String> roles = jwtTokenUtils.getRoles(jwt);
            List<GrantedAuthority> authorities = roles
                    .stream()
                    .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null, // password
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(token);
        }

        filterChain.doFilter(request, response);
    }
}
