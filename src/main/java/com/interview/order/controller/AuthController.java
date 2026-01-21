package com.interview.order.controller;

import com.interview.order.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body == null ? null : body.get("username");
        try {
            String password = body == null ? null : body.get("password");
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            String token = jwtUtil.generateToken(username);
            logger.info("login: user '{}' authenticated successfully", username);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException ex) {
            logger.error("login: authentication failed for user '{}': {}", username, ex.toString(), ex);
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        } catch (Exception ex) {
            // catch any unexpected error and log it
            logger.error("login: unexpected error for user '{}': {}", username, ex.toString(), ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_error"));
        }
    }
}
