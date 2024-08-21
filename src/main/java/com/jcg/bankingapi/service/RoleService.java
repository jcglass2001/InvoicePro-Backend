package com.jcg.bankingapi.service;

import com.jcg.bankingapi.domain.Role;

import java.util.Collection;

public interface RoleService {
    Role getRoleByUserId(Long id);
    Collection<Role> getRoles();
}
