package org.innowise.internship.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RefreshRequestDTO {

    @NotBlank
    private String refreshToken;
}
