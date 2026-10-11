package com.college.attendance.service;

import com.college.attendance.dto.Dtos.LoginRequest;
import com.college.attendance.dto.Dtos.LoginResponse;
import com.college.attendance.entity.User;
import com.college.attendance.exception.ApiException;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest req) {
        User user = users.findByUsername(req.username().trim())
                .filter(u -> encoder.matches(req.password(), u.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        String token = jwtUtil.generate(user.getUsername(), user.getRole().name());
        return new LoginResponse(token, user.getRole().name(), user.getUsername(), user.getFullName());
    }
}
