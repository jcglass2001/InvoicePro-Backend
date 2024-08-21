package com.jcg.bankingapi.service;

import com.jcg.bankingapi.domain.User;
import com.jcg.bankingapi.domain.dto.UserDTO;
import com.jcg.bankingapi.domain.dto.request.UpdateRequest;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {
    UserDTO createUser(User user);

    UserDTO getUserByEmail(String email);

    void sendVerificationCode(UserDTO user);

    UserDTO verifyCode(String email, String code);

    void resetPassword(String email);

    UserDTO verifyPasswordKey(String key);

    void updatePassword(Long userId, String password, String confirmPassword);

    UserDTO verifyAccountByKey(String key);

    UserDTO updateUserDetails(UpdateRequest request);

    UserDTO getUserById(Long userId);

    void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword);

    void updateUserRole(Long userId, String roleName);

    void updateAccountSettings(Long id, Boolean enabled, Boolean notLocked);

    UserDTO toggleMfa(String email);

    void updateProfileImage(UserDTO user, MultipartFile image);
}
