package com.vermeg.app.services;


import com.vermeg.app.models.User;
import com.vermeg.app.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;
    public User getUserByUserName(String username)
    {
        return userRepository.findUserByUsername(username);
    }
}
