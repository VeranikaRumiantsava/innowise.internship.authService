package org.innowise.internship.authenticationservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.innowise.internship.authenticationservice.dto.JwtResponseDTO;
import org.innowise.internship.authenticationservice.dto.LoginRequestDTO;
import org.innowise.internship.authenticationservice.dto.RefreshRequestDTO;
import org.innowise.internship.authenticationservice.dto.RegisterRequestDTO;
import org.innowise.internship.authenticationservice.security.jwt.JwtUtil;
import org.innowise.internship.authenticationservice.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;
    private final JwtUtil jwtUtil;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(registerRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok().body(userService.loginUser(loginRequestDTO));
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validate(@RequestParam("token") String token) {
        return ResponseEntity.ok().body("Token is valid. UserId: " + jwtUtil.validateToken(token).getPayload().getSubject());
    }

    @PostMapping("/refresh")

    public ResponseEntity<?> refresh(@RequestBody @Valid RefreshRequestDTO refreshRequestDTO) {
        Long foreignUserId = jwtUtil.getUserIdFromToken(refreshRequestDTO.getRefreshToken());

        return ResponseEntity.ok().body(
                new JwtResponseDTO(
                        jwtUtil.generateAccessToken(foreignUserId),
                        refreshRequestDTO.getRefreshToken()
                )
        );
    }
}