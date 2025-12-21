package com.ecommerce.ecommerce_backend.services;

import com.ecommerce.ecommerce_backend.entity.User;

public interface UserService {
    User register(User user);
    User login(String email, String password);
}
