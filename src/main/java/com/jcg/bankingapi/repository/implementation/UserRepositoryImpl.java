package com.jcg.bankingapi.repository.implementation;

import com.jcg.bankingapi.domain.Role;
import com.jcg.bankingapi.domain.User;
import com.jcg.bankingapi.domain.UserPrincipal;
import com.jcg.bankingapi.domain.dto.UserDTO;
import com.jcg.bankingapi.domain.dto.request.UpdateRequest;
import com.jcg.bankingapi.domain.enums.VerificationType;
import com.jcg.bankingapi.exception.ApiException;
import com.jcg.bankingapi.repository.RoleRepository;
import com.jcg.bankingapi.repository.UserRepository;
import com.jcg.bankingapi.rowMapper.UserRowMapper;
import com.jcg.bankingapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.jcg.bankingapi.domain.enums.RoleType.ROLE_USER;
import static com.jcg.bankingapi.domain.enums.VerificationType.ACCOUNT;
import static com.jcg.bankingapi.domain.enums.VerificationType.PASSWORD;
import static com.jcg.bankingapi.query.UserQuery.*;
import static com.jcg.bankingapi.utils.ErrorMessage.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Map.of;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl  implements UserRepository<User>, UserDetailsService {
    private static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public User create(User user) {
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0){
            throw new ApiException("Email already in use. Please enter new email.");
        }
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getSqlParameterSource(user);
            jdbc.update(INSERT_USER_QUERY, parameters, holder);
            user.setId(requireNonNull(holder.getKey()).longValue());
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, of("userId", user.getId(),"url", verificationUrl));
            sendEmail(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);
            //emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT.getType());
            user.setIsEnabled(false);
            user.setIsNotLocked(true);
            System.out.println(verificationUrl);
            return user;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred in creating new user.");
        }
    }



    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        try{
            return jdbc.queryForObject(SELECT_USER_BY_ID_QUERY, of("id", id), new UserRowMapper());
        }catch(EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw new ApiException("No user found by id: " + id);
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = getUserByEmail(email);
        return new UserPrincipal(
                user,
                roleRepository.getRoleByUserId(user.getId())
        );

    }
    @Override
    public User getUserByEmail(String email) {
        try{
            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email",email), new UserRowMapper());
            log.info("User found in database: {}", email);
            return user;
        }catch(EmptyResultDataAccessException e){
            log.error(e.getMessage());
            throw new ApiException("No user found by email: " + email);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }

    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);
        String verificationCode = randomAlphabetic(8).toUpperCase();
        try{
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID_QUERY, of("id", user.getId()));
            jdbc.update(INSERT_VERIFICATION_CODE_QUERY, of(
                    "userId", user.getId(),
                    "code", verificationCode,
                    "expirationDate", expirationDate));
            log.info("Verification Code: {}", verificationCode);
            //sendSMS(user.getPhone(), "From: BankingAPI \n Verification code: \n " + verificationCode);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }

    @Override
    public User verifyCode(String email, String code) {
        if(isVerificationCodeExpired(code)) throw new ApiException(EXPIRED_CODE_ERROR.getMessage());
        try {
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY, of("code", code), new UserRowMapper());
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());
            assert userByCode != null;
            assert userByEmail != null;
            if(userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())){
                return userByCode;
            } else {
                throw new ApiException(INVALID_INPUT.getMessage());
            }
        } catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw new ApiException("Unable to find record.");
        } catch (Exception exception){
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }

    @Override
    public void resetPassword(String email) {
        if(getEmailCount(email.trim().toLowerCase()) <= 0) throw new ApiException("Account not found for this email address");
        try{
            String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);
            User user = getUserByEmail(email);
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
            jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, of("userId", user.getId()));
            jdbc.update(INSERT_PASSWORD_VERIFICATION_QUERY, of(
                    "userId", user.getId(),
                    "url", verificationUrl,
                    "expirationDate", expirationDate));
            sendEmail(user.getFirstName(),email,verificationUrl,PASSWORD);
            log.info("Verification url: {}", verificationUrl);
        } catch (Exception exception){
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if(isLinkExpired(key, PASSWORD)) throw new ApiException(EXPIRED_LINK_ERROR.getMessage());
        try{
            return jdbc.queryForObject(
                    SELECT_USER_BY_PASSWORD_URL_QUERY,
                    of("url", getVerificationUrl(key,PASSWORD.getType())),
                    new UserRowMapper());
        } catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw new ApiException(INVALID_LINK_ERROR.getMessage());
        } catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        if(!password.equals(confirmPassword)) throw new ApiException("Passwords do not match. Please try again.");
        try{
            jdbc.update(UPDATE_USER_PASSWORD_BY_PASSWORD_URL_QUERY, of(
                    "password", passwordEncoder.encode(password),
                    "url", getVerificationUrl(key,PASSWORD.getType())));
            jdbc.update(DELETE_VERIFICATION_BY_URL_QUERY, of("url", getVerificationUrl(key,PASSWORD.getType())));
        } catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }
    @Override
    public void renewPassword(Long userId, String password, String confirmPassword) {
        if(!password.equals(confirmPassword)) throw new ApiException("Passwords do not match. Please try again.");
        try{
            jdbc.update(UPDATE_USER_PASSWORD_BY_USER_ID_QUERY, of(
                    "id", userId,
                    "password", passwordEncoder.encode(password)
            ));
            //jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, Map.of("userId", userId));
        } catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }

    @Override
    public User verifyAccountKey(String key) {
        try{
            var user = jdbc.queryForObject(SELECT_USER_BY_ACCOUNT_URL_QUERY, of("url", getVerificationUrl(key,ACCOUNT.getType())), new UserRowMapper());
            assert user != null;
            jdbc.update(UPDATE_USER_ENABLED_QUERY, of("enabled", true, "id", user.getId()));
            return user;
        } catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw new ApiException(INVALID_LINK_ERROR.getMessage());
        } catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }
    @Override
    public User updateUserDetails(UpdateRequest user) {
        try{
            jdbc.update(UPDATE_USER_DETAILS_QUERY, getUserDetailsSqlParameterSource(user));
            return get(user.getId());
        } catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        }
    }

    @Override
    public void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword)){ throw new ApiException("Passwords do not match. Please try again.");}
        User user = get(id);
        if ( passwordEncoder.matches(currentPassword, user.getPassword()) ){
            try{
                jdbc.update(UPDATE_USER_PASSWORD_BY_ID_QUERY, of("userId", id, "newPassword", passwordEncoder.encode(newPassword)));
            } catch (Exception exception){
                throw new ApiException(GENERIC_ERROR.getMessage());
            }
        } else {
            throw new ApiException("Incorrect current password. Please try again.");
        }

    }

    @Override
    public void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked) {
        try{
            jdbc.update(UPDATE_USER_SETTINGS_QUERY, of("userId", userId, "enabled", enabled, "notLocked", notLocked));
        } catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }

    @Override
    public User toggleMfa(String email) {
        var user = getUserByEmail(email);
        if(isBlank(user.getPhone())) { throw new ApiException("Phone number required to change Multi-Factor Authentication"); }
        user.setIsUsingMfa(!user.getIsUsingMfa());
        try{
            jdbc.update(UPDATE_USER_MFA_QUERY, of("email", email, "isUsingMfa", user.getIsUsingMfa()));
            return user;
        } catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }

    }

    @Override
    public void updateProfileImage(UserDTO user, MultipartFile image) {
        var userImageUrl = setUserImageUrl(user.getEmail());
        user.setImageUrl(userImageUrl);
        saveImage(user.getEmail(), image);
        jdbc.update(UPDATE_USER_IMAGE_QUERY, of("imageUrl", userImageUrl, "userId", user.getId()));
    }

    private Boolean isLinkExpired(String key, VerificationType password) {
        try{
            return jdbc.queryForObject(SELECT_EXPIRATION_BY_URL_QUERY, of("url", getVerificationUrl(key,password.getType())), Boolean.class);
        } catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw new ApiException(INVALID_LINK_ERROR.getMessage());
        }
    }

    private Boolean isVerificationCodeExpired(String code) {
        try{
            return jdbc.queryForObject(SELECT_CODE_EXPIRATION_QUERY, of("code", code), Boolean.class);
        } catch (EmptyResultDataAccessException exception){
            throw new ApiException(INVALID_INPUT.getMessage());
        }
    }

    private int getEmailCount(String email) {
        return jdbc.queryForObject(
                COUNT_USER_EMAIL_QUERY,
                of("email",email),
                Integer.class
        );
    }
    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", passwordEncoder.encode(user.getPassword()));
    }
    private SqlParameterSource getUserDetailsSqlParameterSource(UpdateRequest user) {
        return new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("phone", user.getPhone())
                .addValue("address", user.getAddress())
                .addValue("title", user.getTitle())
                .addValue("bio", user.getBio());
    }
    private void sendEmail(String firstName, String email, String verificationUrl, VerificationType verificationType) {
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendVerificationEmail(firstName, email, verificationUrl, verificationType);
            } catch (Exception exception) {
                throw new ApiException("Unable to send email");
            }
        });
    }

    private String getVerificationUrl(String key, String type){
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/user/verify/" + type + "/" + key)
                .toUriString();
    }
    private String setUserImageUrl(String email){
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/image/" + email + ".png").toUriString();
    }
    private void saveImage(String email, MultipartFile image){
        Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Downloads/images/").toAbsolutePath().normalize();
        if(!Files.exists(fileStorageLocation)){
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException("Unable to create directory to save image");
            }
            log.info("Create directories: {} ", fileStorageLocation );
        }
        try {
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(email + ".png"), REPLACE_EXISTING);
        } catch (IOException exception) {
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        }
        log.info("File saved in: {}", fileStorageLocation);
    }
}
