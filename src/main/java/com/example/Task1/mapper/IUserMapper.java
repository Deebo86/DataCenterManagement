package com.example.Task1.mapper;

import com.example.Task1.dto.UserRequestDto;
import com.example.Task1.model.User;


public interface IUserMapper {
    public User requestDtoToUser (UserRequestDto requestDto);
}
