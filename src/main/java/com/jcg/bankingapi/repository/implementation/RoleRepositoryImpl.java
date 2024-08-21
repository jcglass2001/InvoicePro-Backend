package com.jcg.bankingapi.repository.implementation;

import com.jcg.bankingapi.domain.Role;
import com.jcg.bankingapi.exception.ApiException;
import com.jcg.bankingapi.repository.RoleRepository;
import com.jcg.bankingapi.rowMapper.RoleRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static com.jcg.bankingapi.domain.enums.RoleType.*;
import static com.jcg.bankingapi.query.RoleQuery.*;
import static com.jcg.bankingapi.utils.ErrorMessage.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RoleRepositoryImpl implements RoleRepository<Role> {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Role create(Role data) {
        return null;
    }

    @Override
    public Collection<Role> list() {
        log.info("Fetching all roles");
        try {
            return jdbc.query(SELECT_ROLES_QUERY, new RoleRowMapper());
        } catch (Exception exception){
            log.info(exception.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }

    @Override
    public Role get(Long id) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        log.info("Adding role {} to user id: {}", roleName, userId);
        try {
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("roleName", roleName), new RoleRowMapper());
            jdbc.update(INSERT_ROLE_TO_USER_QUERY, Map.of("userId", userId, "roleId", Objects.requireNonNull(role).getId()));
        } catch(EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("No role found by name:" + ROLE_USER.name());
        } catch (Exception exception){
            log.info(exception.getMessage());
            throw new ApiException("An error occurred in adding role to user");
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        log.info("Fetch role for user id: {}", userId);
        try {
            var roles = jdbc.queryForObject(SELECT_ROLE_BY_USER_ID_QUERY, Map.of("userId", userId), new RoleRowMapper());
            log.info("Roles retrieved: {}", roles);
            return roles;
        } catch(EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("No role found by userId:" + userId);
        } catch (Exception exception){
            log.info(exception.getMessage());
            throw new ApiException("An error occurred in fetching role from userId");
        }
    }

    @Override
    public Role getRoleByUserEmail(String userEmail) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {
        log.info("Updating role for user id: {}", userId);
        try {
            var role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("roleName", roleName), new RoleRowMapper());
            assert role != null;
            jdbc.update(UPDATE_USER_ROLE_QUERY, Map.of("roleId", role.getId(), "userId", userId));
        } catch(EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("No role found by name:" + roleName);
        } catch (Exception exception){
            log.info(exception.getMessage());
            throw new ApiException(GENERIC_ERROR.getMessage());
        }
    }
}
