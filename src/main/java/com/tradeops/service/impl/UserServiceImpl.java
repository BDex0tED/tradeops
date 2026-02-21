package com.tradeops.service.impl;


import com.tradeops.exceptions.ResourceNotFoundException;
import com.tradeops.exceptions.UserAlreadyExistsException;
import com.tradeops.exceptions.UserNotFoundException;
import com.tradeops.model.entity.Role;
import com.tradeops.model.entity.UserEntity;
import com.tradeops.model.request.*;
import com.tradeops.model.response.*;
import com.tradeops.model.request.ChangePasswordRequest;
import com.tradeops.model.request.LoginRequest;
import com.tradeops.model.request.RefreshTokenRequest;
import com.tradeops.model.request.RegisterRequest;
import com.tradeops.model.response.JWTResponse;
import com.tradeops.model.response.LoginResponse;
import com.tradeops.model.response.RegistrationResponse;
import com.tradeops.repo.RoleRepo;
import com.tradeops.repo.UserEntityRepo;
import com.tradeops.service.JWTService;
import com.tradeops.service.CustomUserDetailsService;
import com.tradeops.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserEntityRepo userEntityRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    @Transactional
    public UserEntity register(RegisterRequest req, Role role) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userEntityRepo.existsByEmail(req.email())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        UserEntity user = new UserEntity();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setUsername(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setCreatedAt(LocalDateTime.now());

        user.setRoles(List.of(role));
        user.setVerified(true);

        userEntityRepo.save(user);

        return user;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }

        UserEntity user = userEntityRepo.findByUsername(request.username()).orElseThrow(()->new UserNotFoundException(request.username() + " not found"));

        boolean isTrader = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_TRADER"));
        if (isTrader && !user.isApproved()) {
            throw new BadCredentialsException("Your account is pending approval by Admin.");
        }

        if (user.isVerified()) {
            Authentication auth =
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            user.getRoles().stream()
                                    .map(r -> new SimpleGrantedAuthority(r.getName()))
                                    .toList()
                    );

            JWTResponse jwt = issueTokens(auth);

            return new LoginResponse(
                    jwt.accessToken(),
                    jwt.refreshToken(),
                    user.getRoles().getFirst().getName(),
                    user.getId());
        }

        userEntityRepo.save(user);

        return new LoginResponse(
                null,
                null,
                user.getRoles().getFirst().getName(),
                user.getId());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        UserEntity user = getCurrentUser();

        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid old password");
        }
        if (req.newPassword().length() < 8) {
            throw new IllegalArgumentException("Password too short");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userEntityRepo.save(user);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String incomingRefreshToken = request.refreshToken();

        String username;
        try {
            username = jwtService.extractUserName(incomingRefreshToken);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        UserEntity user = userEntityRepo.findByUsername(username).orElseThrow(()->new UserNotFoundException(username + " not found"));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        if (!jwtService.validateToken(incomingRefreshToken, userDetails)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        user.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority(r.getName()))
                                .toList()
                );

        JWTResponse jwt = issueTokens(auth);

        return new LoginResponse(
                jwt.accessToken(),
                jwt.refreshToken(),
                user.getRoles().getFirst().getName(),
                user.getId());
    }

    @Override
    public UserEntity getCurrentUser() {
        String username =
                Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userEntityRepo.findByUsername(username).orElseThrow(()->new UserNotFoundException("User not found with username: " + username));
    }

    private JWTResponse issueTokens(Authentication auth) {
        String access = jwtService.generateToken(auth);
        String refresh = jwtService.generateRefreshToken(auth);

        return new JWTResponse(access,refresh);
    }

    private UserEntity createUserEntity(RegisterRequest req) {
        UserEntity user = new UserEntity();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setUsername(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private void validateRegistration(RegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userEntityRepo.existsByEmail(req.email())) {
            throw new UserAlreadyExistsException("Email already registered");
        }
    }


}
