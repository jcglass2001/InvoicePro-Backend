package com.jcg.bankingapi.utils;

import com.jcg.bankingapi.domain.UserPrincipal;
import com.jcg.bankingapi.domain.dto.UserDTO;
import org.springframework.security.core.Authentication;

public class UserUtils {

    public static UserDTO getAuthenticatedUser(Authentication authentication) {
        return ((UserDTO) authentication.getPrincipal());
    }
    public static UserDTO getLoggedInUser(Authentication authentication){
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }
}
