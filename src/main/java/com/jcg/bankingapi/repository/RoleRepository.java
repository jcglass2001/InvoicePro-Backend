package com.jcg.bankingapi.repository;

import com.jcg.bankingapi.domain.Role;

import java.util.Collection;

public interface RoleRepository <T extends Role> {
    /* Basic CRUD Operations */
    T create(T data);

    Collection<T> list();

    T get(Long id);

    T update(T data);

    Boolean delete(Long id);
    /* Complex Operations */
    void addRoleToUser(Long userId, String roleName);
    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String userEmail);
    void updateUserRole(Long userId, String roleName);
}
