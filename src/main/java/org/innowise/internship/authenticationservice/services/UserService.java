package org.innowise.internship.authenticationservice.services;

import lombok.RequiredArgsConstructor;
import org.innowise.internship.authenticationservice.dto.JwtResponseDTO;
import org.innowise.internship.authenticationservice.dto.LoginRequestDTO;
import org.innowise.internship.authenticationservice.dto.RegisterRequestDTO;
import org.innowise.internship.authenticationservice.entities.User;
import org.innowise.internship.authenticationservice.exceptions.UserAlreadyExistsException;
import org.innowise.internship.authenticationservice.repositories.UserRepository;
import org.innowise.internship.authenticationservice.security.details.UserDetailsImpl;
import org.innowise.internship.authenticationservice.security.jwt.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Transactional
    public String registerUser(RegisterRequestDTO registerRequestDTO) {
        if (userRepository.findByLogin(registerRequestDTO.getLogin()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        String encodedPassword = passwordEncoder.encode(registerRequestDTO.getPassword());
        User user = new User();
        user.setLogin(registerRequestDTO.getLogin());
        user.setPassword(encodedPassword);
        user.setForeignUserid(2L);
        userRepository.save(user);
        return "User created successfully";
    }

    public JwtResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getLogin(),
                        loginRequestDTO.getPassword()
                )
        );
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        User user = userDetails.getUser();

        return new JwtResponseDTO(
                jwtUtil.generateAccessToken(user.getForeignUserid()),
                jwtUtil.generateRefreshToken(user.getForeignUserid())
        );
    }
}
