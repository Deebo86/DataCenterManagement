package com.example.Task1.controller;

import com.example.Task1.dto.UserRequestDto;
import com.example.Task1.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login (@RequestBody @Valid UserRequestDto userRequestDto)
    {
        userService.loginUser(userRequestDto);
        return new ResponseEntity<>("", HttpStatus.ACCEPTED);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register (@RequestBody @Valid UserRequestDto userRequestDto)
    {
        userService.registerUser(userRequestDto);
        return new ResponseEntity<>("", HttpStatus.ACCEPTED);
    }
}
