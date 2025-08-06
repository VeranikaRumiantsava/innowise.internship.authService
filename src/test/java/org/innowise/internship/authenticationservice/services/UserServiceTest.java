package org.innowise.internship.authenticationservice.services;

import org.innowise.internship.authenticationservice.dto.JwtResponseDTO;
import org.innowise.internship.authenticationservice.dto.LoginRequestDTO;
import org.innowise.internship.authenticationservice.dto.RegisterRequestDTO;
import org.innowise.internship.authenticationservice.entities.User;
import org.innowise.internship.authenticationservice.exceptions.UserAlreadyExistsException;
import org.innowise.internship.authenticationservice.repositories.UserRepository;
import org.innowise.internship.authenticationservice.security.details.UserDetailsImpl;
import org.innowise.internship.authenticationservice.security.jwt.JwtUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;


class UserServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUserShouldReturnUserCreatedSuccessfully() {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setLogin("user");
        registerRequestDTO.setPassword("password");

        Mockito.when(userRepository.findByLogin("user")).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode("password")).thenReturn("encodedpassword");

        String result = userService.registerUser(registerRequestDTO);

        Assertions.assertEquals("User created successfully", result);
        Mockito.verify(userRepository).save(argThat(user ->
                user.getLogin().equals("user")
                        && user.getPassword().equals("encodedpassword")
                        && user.getForeignUserid() == 2L
        ));
    }

    @Test
    void registerUserShouldThrowUserAlreadyExistsExceptionWhenUserAlreadyExists() {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setLogin("user");
        registerRequestDTO.setPassword("password");

        Mockito.when(userRepository.findByLogin("user"))
                .thenReturn(Optional.of(new User()));

        Assertions.assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser(registerRequestDTO));

        Mockito.verify(userRepository, Mockito.never()).save(any());
        Mockito.verifyNoInteractions(passwordEncoder);
    }

    @Test
    void loginUserShouldReturnRefreshAndAccessTokens() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setLogin("user");
        dto.setPassword("password");

        User user = new User();
        user.setForeignUserid(123L);

        UserDetailsImpl userDetails = Mockito.mock(UserDetailsImpl.class);
        Mockito.when(userDetails.getUser()).thenReturn(user);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getPrincipal()).thenReturn(userDetails);

        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        Mockito.when(jwtUtil.generateAccessToken(123L)).thenReturn("accessToken");
        Mockito.when(jwtUtil.generateRefreshToken(123L)).thenReturn("refreshToken");

        JwtResponseDTO response = userService.loginUser(dto);

        Assertions.assertEquals("accessToken", response.getAccessToken());
        Assertions.assertEquals("refreshToken", response.getRefreshToken());
    }

    @Test
    void loginUserShouldThrowBadCredentialsExceptionWhenCredentialsAreWrong() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin("user");
        loginRequestDTO.setPassword("wrong_password");

        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials") {
                });

        Assertions.assertThrows(BadCredentialsException.class,
                () -> userService.loginUser(loginRequestDTO));

        Mockito.verifyNoInteractions(jwtUtil);
    }
}
