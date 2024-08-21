package com.jcg.bankingapi.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VerificationType {
    ACCOUNT("account"),
    PASSWORD("password");

    private final String type;
}
