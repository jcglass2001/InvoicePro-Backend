package com.jcg.bankingapi.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorMessage {
    GENERIC_ERROR("An error occurred. Please try again"),
    INVALID_LINK_ERROR("Link is not valid. Please reset your password again."),
    INVALID_INPUT("Invalid input. Please try again."),
    EXPIRED_LINK_ERROR("Link has expired. Please reset your password again."),
    EXPIRED_CODE_ERROR("Code has expired. Please login again.");

    private final String message;
}
