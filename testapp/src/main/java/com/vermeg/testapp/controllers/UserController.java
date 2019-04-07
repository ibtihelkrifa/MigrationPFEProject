package com.vermeg.testapp.controllers;


import com.vermeg.testapp.models.User;
import com.vermeg.testapp.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class UserController {

@Autowired
    UserService userService;


    @GetMapping("/users/{email}")
     public User getUser(@PathVariable String email)
    {
        return userService.getUserBymail(email);
    }

    @PostMapping("/users")
    public  User createUser(@RequestBody User user)
    {
      return  this.userService.createUser(user);
    }


}
