package com.jcg.bankingapi.domain.dto.mapper;

import com.jcg.bankingapi.domain.Role;
import com.jcg.bankingapi.domain.User;
import com.jcg.bankingapi.domain.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


public class UserDTOMapper {
    public static UserDTO fromUser(User user){
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        return userDTO;
    }
    public static UserDTO fromUser(User user, Role role) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user,userDTO);
        userDTO.setRoleName(role.getName());
        userDTO.setPermissions(role.getPermission());
        return userDTO;
    }
    public static User toUser(UserDTO userDTO){
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);
        return user;
    }
}
