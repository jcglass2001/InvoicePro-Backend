package com.jcg.bankingapi.controller;

import com.jcg.bankingapi.domain.User;
import com.jcg.bankingapi.domain.UserPrincipal;
import com.jcg.bankingapi.domain.dto.UserDTO;
import com.jcg.bankingapi.domain.dto.request.*;
import com.jcg.bankingapi.domain.dto.response.HttpResponse;
import com.jcg.bankingapi.event.NewUserEvent;
import com.jcg.bankingapi.exception.ApiException;
import com.jcg.bankingapi.provider.TokenProvider;
import com.jcg.bankingapi.service.EventService;
import com.jcg.bankingapi.service.RoleService;
import com.jcg.bankingapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.jcg.bankingapi.constant.Constants.TOKEN_PREFIX;
import static com.jcg.bankingapi.domain.dto.mapper.UserDTOMapper.toUser;
import static com.jcg.bankingapi.domain.enums.EventType.*;
import static com.jcg.bankingapi.utils.ExceptionUtils.processError;
import static com.jcg.bankingapi.utils.UserUtils.getAuthenticatedUser;
import static com.jcg.bankingapi.utils.UserUtils.getLoggedInUser;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    public final EventService eventService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user) {
        UserDTO userDTO = userService.createUser(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDTO))
                        .message(String.format("User Registered Successfully"))
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }

    @PostMapping("/authenticate")
    public ResponseEntity<HttpResponse> authenticateUser(@RequestBody @Valid AuthRequest request) {
        UserDTO userDTO = authenticate(request.getEmail(), request.getPassword());
        eventPublisher.publishEvent(new NewUserEvent(LOGIN_ATTEMPT_SUCCESS, userDTO.getEmail()));
        return userDTO.getIsUsingMfa() ? sendVerificationCode(userDTO) : sendResponse(userDTO);
    }

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> getProfile(Authentication authentication) {
        UserDTO user = userService.getUserByEmail(getAuthenticatedUser(authentication).getEmail());
        System.out.println(authentication.getPrincipal());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user,
                                "roles", roleService.getRoles(),
                                "events", eventService.getEventsByUserId(user.getId()))
                        )
                        .message("Profile Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateRequest request) throws InterruptedException {
        UserDTO updatedUser = userService.updateUserDetails(request);
        eventPublisher.publishEvent(new NewUserEvent(PROFILE_UPDATE, updatedUser.getEmail()));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", updatedUser,
                                "roles", roleService.getRoles(),
                                "events", eventService.getEventsByUserId(updatedUser.getId()))
                        )
                        .message("User updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/password")
    public ResponseEntity<HttpResponse> updatePassword(Authentication authentication, @RequestBody @Valid UpdatePasswordRequest request) {
        UserDTO user = getAuthenticatedUser(authentication);
        userService.updatePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword(), request.getConfirmNewPassword());
        eventPublisher.publishEvent(new NewUserEvent(PASSWORD_UPDATE, user.getEmail()));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password updated")
                        .data(of("user", user,
                                "roles", roleService.getRoles(),
                                "events", eventService.getEventsByUserId(user.getId()))
                        )
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/role/{roleName}")
    public ResponseEntity<HttpResponse> updateRole(Authentication authentication, @PathVariable("roleName") String roleName) {
        UserDTO user = getAuthenticatedUser(authentication);
        userService.updateUserRole(user.getId(), roleName);
        eventPublisher.publishEvent(new NewUserEvent(ROLE_UPDATE, user.getEmail()));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user,
                                "roles", roleService.getRoles(),
                                "events", eventService.getEventsByUserId(user.getId()))
                        )
                        .message("Role updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/settings")
    public ResponseEntity<HttpResponse> updateAccountSettings(Authentication authentication, @RequestBody SettingsForm form) {
        UserDTO user = getAuthenticatedUser(authentication);
        userService.updateAccountSettings(user.getId(), form.getEnabled(), form.getNotLocked());
        eventPublisher.publishEvent(new NewUserEvent(ACCOUNT_SETTINGS_UPDATE, user.getEmail()));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user,
                                "roles", roleService.getRoles(),
                                "events", eventService.getEventsByUserId(user.getId()))
                        )
                        .message("Account settings updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/update/image")
    public ResponseEntity<HttpResponse> updateProfileImage(Authentication authentication, @RequestParam("image") MultipartFile image) {
        UserDTO user = getAuthenticatedUser(authentication);
        userService.updateProfileImage(user, image);
        eventPublisher.publishEvent(new NewUserEvent(PROFILE_PICTURE_UPDATE, user.getEmail()));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user,
                                "roles", roleService.getRoles(),
                                "events", eventService.getEventsByUserId(user.getId()))
                        )
                        .message("Profile Image updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping(value = "/image/{fileName}", produces = IMAGE_PNG_VALUE)
    public byte[] getProfileImage(@PathVariable("fileName") String fileName) throws IOException {
        log.info("getProfileImage() endpoint reached");
        return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/Downloads/images/" + fileName));
    }

    @PatchMapping("/togglemfa")
    public ResponseEntity<HttpResponse> toggleMfa(Authentication authentication) {
        UserDTO user = userService.toggleMfa(getAuthenticatedUser(authentication).getEmail());
        eventPublisher.publishEvent(new NewUserEvent(MFA_UPDATE, user.getEmail()));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user,
                                "roles", roleService.getRoles(),
                                "events", eventService.getEventsByUserId(user.getId()))
                        )
                        .message("Multi-Factor Authentication updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email,
                                                   @PathVariable("code") String code) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        UserDTO user = userService.verifyCode(email, code);
        eventPublisher.publishEvent(new NewUserEvent(LOGIN_ATTEMPT_SUCCESS, user.getEmail()));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of(
                                        "user", user,
                                        "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                                        "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(user))
                                )
                        )
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> getPasswordUrl(@PathVariable("email") String email) {
        userService.resetPassword(email);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Email sent. Please check your email to reset your password")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPassword(@PathVariable("key") String key) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        UserDTO user = userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user))
                        .message("Please enter a new password")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message(userService.verifyAccountByKey(key).getIsEnabled() ? "Account already verified" : "Account Verified")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PutMapping("/reset/password")
    public ResponseEntity<HttpResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.updatePassword(request.getUserId(), request.getPassword(), request.getConfirmPassword());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password reset successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }

    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request) {
        if (isHeaderAndTokenValid(request)) {
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDTO user = userService.getUserById(tokenProvider.getSubject(token, request));
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .data(Map.of(
                                    "user", user,
                                    "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                                    "refresh_token", token)
                            )
                            .message("Token refreshed")
                            .status(OK)
                            .statusCode(OK.value())
                            .build());
        } else {
            return ResponseEntity.badRequest().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .reason("Refresh Token missing or invalid")
                            .status(BAD_REQUEST)
                            .statusCode(BAD_REQUEST.value())
                            .build());
        }
    }

    private boolean isHeaderAndTokenValid(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
        return request.getHeader(AUTHORIZATION) != null
                && request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)
                && tokenProvider.isTokenValid(tokenProvider.getSubject(token, request), token);
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDTO user) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of(
                                "user", user,
                                "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(user))
                        ))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }

    private UserPrincipal getUserPrincipal(UserDTO user) {
        return new UserPrincipal(
                toUser(userService.getUserByEmail(user.getEmail())),
                roleService.getRoleByUserId(user.getId())
        );
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO user) {
        userService.sendVerificationCode(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user))
                        .message("Verification Code Sent")
                        .status(OK)
                        .statusCode(OK.value())
                        .build()
        );
    }

    private UserDTO authenticate(String email, String password) {
        UserDTO userByEmail = userService.getUserByEmail(email);
        try {
            if (null != userByEmail) {
                eventPublisher.publishEvent(new NewUserEvent(LOGIN_ATTEMPT, email));
            }
            Authentication authentication = authenticationManager.authenticate(unauthenticated(email, password));
            System.out.println(authentication);
            System.out.println(((UserPrincipal) authentication.getPrincipal()).getUser());

            UserDTO loggedInUser = getLoggedInUser(authentication);
            if (!loggedInUser.getIsUsingMfa()) {
                eventPublisher.publishEvent(new NewUserEvent(LOGIN_ATTEMPT_SUCCESS, email));
            }
            return loggedInUser;
        } catch (Exception exception) {
            if(null != userByEmail) {
                eventPublisher.publishEvent(new NewUserEvent(LOGIN_ATTEMPT_FAILURE, email));
            }
            processError(request, response, exception);
            throw new ApiException(exception.getMessage());
        }
    }

    private URI getUri() {
        return URI.create(
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/user/get/<userId>")
                        .toUriString()
        );
    }

}
