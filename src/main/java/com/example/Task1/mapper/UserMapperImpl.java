package com.example.Task1.mapper;

import com.example.Task1.dto.UserRequestDto;
import com.example.Task1.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserMapperImpl implements IUserMapper{

    @Override
    public User requestDtoToUser(UserRequestDto requestDto) {
        if(requestDto == null)
        {
            return null;
        }
        User u = new User();
        u.setUsername(requestDto.getUsername());
        u.setPassword(requestDto.getPassword());
        u.setRoles(new HashSet<>(List.of("ROLE_USER")));
        return u;
    }
}
