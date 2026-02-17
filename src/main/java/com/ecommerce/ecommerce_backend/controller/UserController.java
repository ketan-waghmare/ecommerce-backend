package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.security.JwtUtil;
import com.ecommerce.ecommerce_backend.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/register")
    public User register(@RequestBody User user) {
        log.error("User register = " +user);
        return userService.register(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
         User user = userService.login(
                request.get("email"),
                request.get("password")
        );

         String token = jwtUtil.generateToken(user);

         Map<String,String> response = new HashMap<>();
         response.put("token",token);
         response.put("role", user.getRole());
         response.put("name",user.getName());
         response.put("userId", String.valueOf(user.getId()));

         return response;
    }

}
