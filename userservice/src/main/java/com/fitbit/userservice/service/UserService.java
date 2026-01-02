package com.fitbit.userservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitbit.userservice.dto.RegisterRequest;
import com.fitbit.userservice.dto.UserResponse;
import com.fitbit.userservice.model.User;
import com.fitbit.userservice.repository.UserRepository;

@Service
public class UserService {

    
    @Autowired
    private UserRepository repository;

    public UserResponse register(RegisterRequest request){

        if(repository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User savedUser = repository.save(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setPassword(savedUser.getPassword());
        userResponse.setFirstName(savedUser.getFirstName());
        userResponse.setLastName(savedUser.getLastName());
        userResponse.setCreatedAt(savedUser.getCreatedAt());
        userResponse.setUpdatedAt(savedUser.getUpdatedAt());
        userResponse.setId(savedUser.getId());
        userResponse.setRole(savedUser.getRole());

        return userResponse;
        
    }

    public UserResponse getUserProfile(String userId){
        User user = repository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(user.getEmail());
        userResponse.setPassword(user.getPassword());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        userResponse.setId(user.getId());
        userResponse.setRole(user.getRole());


        return userResponse;

    }

    public Boolean exitByUserId(String userId) {
        return repository.existsById(userId);
    }
}
