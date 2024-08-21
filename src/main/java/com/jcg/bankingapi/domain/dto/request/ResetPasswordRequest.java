package com.jcg.bankingapi.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotNull(message = "ID cannot be null or empty")
    private Long userId;
    @NotNull(message = "Password cannot be null or empty")
    private String password;
    @NotNull(message = "ConfirmPassword cannot be null or empty")
    private String confirmPassword;
}
