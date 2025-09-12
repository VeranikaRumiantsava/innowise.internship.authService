package org.innowise.internship.authenticationservice.controllers;

import org.innowise.internship.authenticationservice.dto.JwtResponseDTO;
import org.innowise.internship.authenticationservice.dto.LoginRequestDTO;
import org.innowise.internship.authenticationservice.dto.RefreshRequestDTO;
import org.innowise.internship.authenticationservice.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerIT extends BaseIT {

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void registerUserShouldReturnStatus201Created() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setLogin("user");
        registerRequestDTO.setPassword("password");
        registerRequestDTO.setIdUser(2L);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("User created successfully")));
    }

    @Test
    void registerUserShouldReturnStatus409ConflictWhenCreateUserWhenUserAlreadyExists() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setLogin("user");
        registerRequestDTO.setPassword("password");
        registerRequestDTO.setIdUser(2L);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User already exists"));
    }

    @Test
    void registerUserShouldReturnStatus400BadRequestWithInvalidDate() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setLogin("user");
        registerRequestDTO.setPassword("");
        registerRequestDTO.setIdUser(2L);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUserShouldReturnStatus200AndRefreshAndAccessTokens() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setLogin("user");
        registerRequestDTO.setPassword("qwerty");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin("user");
        loginRequestDTO.setPassword("qwerty");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void loginUserShouldReturnStatus401WithBedCredentials() throws Exception {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin("user");
        loginRequestDTO.setPassword("wrong_password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Incorrect login or password"));
    }

    @Test
    void validateTokenShouldReturnStatus200WhenTokenIsValid() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setLogin("user");
        registerRequestDTO.setPassword("password");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isCreated());

        LoginRequestDTO login = new LoginRequestDTO();
        login.setLogin("user");
        login.setPassword("password");
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JwtResponseDTO tokens = objectMapper.readValue(response, JwtResponseDTO.class);

        mockMvc.perform(get("/api/v1/auth/validate")
                        .param("token", tokens.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Token is valid")));
    }

    @Test
    void validateTokenShouldReturnStatus401WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/auth/validate")
                        .param("token", "random_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }

    @Test
    void refreshTokenShouldReturnStatus200AndRefreshAndAccessTokens() throws Exception {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setLogin("user");
        registerRequestDTO.setPassword("password");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin("user");
        loginRequestDTO.setPassword("password");
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andReturn().getResponse().getContentAsString();

        JwtResponseDTO jwtResponseDTO = objectMapper.readValue(response, JwtResponseDTO.class);

        RefreshRequestDTO refresh = new RefreshRequestDTO(jwtResponseDTO.getRefreshToken());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(jwtResponseDTO.getRefreshToken()));
    }

    @Test
    void refreshTokenShouldReturnStatus401WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString("bad_token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }

}
