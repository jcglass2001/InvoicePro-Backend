package com.jcg.bankingapi.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventType {
    LOGIN_ATTEMPT("Login attempted"),
    LOGIN_ATTEMPT_FAILURE("Login attempted and failed"),
    LOGIN_ATTEMPT_SUCCESS("Login attempted and succeeded"),
    PROFILE_UPDATE("Profile information updated"),
    PROFILE_PICTURE_UPDATE("Profile image updated"),
    ROLE_UPDATE("Role updated"),
    ACCOUNT_SETTINGS_UPDATE("Account settings updated"),
    PASSWORD_UPDATE("Password updated"),
    MFA_UPDATE("MFA settings updated");

    private final String description;
}
