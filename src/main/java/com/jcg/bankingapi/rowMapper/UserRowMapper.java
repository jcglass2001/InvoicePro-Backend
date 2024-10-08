package com.jcg.bankingapi.rowMapper;

import com.jcg.bankingapi.domain.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .address(rs.getString("address"))
                .phone(rs.getString("phone"))
                .title(rs.getString("title"))
                .bio(rs.getString("bio"))
                .imageUrl(rs.getString("image_url"))
                .isEnabled(rs.getBoolean("enabled"))
                .isNotLocked(rs.getBoolean("non_locked"))
                .isUsingMfa(rs.getBoolean("using_MFA"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
