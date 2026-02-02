package com.example.Task1.service;

import com.example.Task1.dto.UserRequestDto;
import com.example.Task1.mapper.*;
import com.example.Task1.model.User;
import com.example.Task1.model.UserPrincipal;
import com.example.Task1.repository.UserRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private IUserMapper userMapper;

    public void loginUser(@Valid UserRequestDto userRequestDto) {
    }

    public void registerUser(@Valid UserRequestDto userRequestDto) {
        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        userRepo.save(userMapper.requestDtoToUser(userRequestDto));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepo.findByUsername(username);
        if (u == null) {
            System.out.println("User 404");
            throw new UsernameNotFoundException("User 404");
        }
        return new UserPrincipal(u);
    }
}
