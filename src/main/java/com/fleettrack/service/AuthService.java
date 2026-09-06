package com.fleettrack.service;

import com.fleettrack.dao.entity.User;
import com.fleettrack.dao.repository.UserRepository;
import com.fleettrack.dto.request.ElevatedUserRequest;
import com.fleettrack.dto.request.LoginRequest;
import com.fleettrack.dto.request.RefreshTokenRequest;
import com.fleettrack.dto.request.RegisterRequest;
import com.fleettrack.dto.response.AuthResponse;
import com.fleettrack.exception.BusinessException;
import com.fleettrack.mapper.AuthMapper;
import com.fleettrack.security.JwtService;
import com.fleettrack.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.DRIVER);

         userRepository.save(user);
        return "User registered successfully! Please log in.";
    }

    @Transactional
    public void createElevatedUser(ElevatedUserRequest request) {
        if (request.getRole() == Role.DRIVER) {
            throw new BusinessException("Drivers must register via the public /auth/register API.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        return generateAuthTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BusinessException("Refresh token is invalid or expired");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("User not found"));

        return generateAuthTokens(user);
    }

    private AuthResponse generateAuthTokens(User user) {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        Map<String, Object> claims = Map.of("role", user.getRole().name());

        String accessToken = jwtService.generateToken(principal, claims);
        String refreshToken = jwtService.generateRefreshToken(principal);

        return authMapper.toResponse(user, accessToken, refreshToken);
    }
}