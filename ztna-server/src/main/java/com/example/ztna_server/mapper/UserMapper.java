package com.example.ztna_server.mapper;

import com.example.ztna_server.dto.internal.UserInfo;
import com.example.ztna_server.dto.response.UserResponse;
import com.example.ztna_server.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserInfo toUserInfo(User user);
    UserResponse toUserResponse(User user);
}
