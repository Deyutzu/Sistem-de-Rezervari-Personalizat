package com.stayfinder.service;

import com.stayfinder.dto.AuthResponse;
import com.stayfinder.dto.LoginRequest;
import com.stayfinder.dto.RegisterRequest;
import com.stayfinder.exception.BadRequestException;
import com.stayfinder.model.Role;
import com.stayfinder.model.User;
import com.stayfinder.repository.UserRepository;
import com.stayfinder.security.JwtTokenProvider;
import com.stayfinder.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Există deja un cont înregistrat cu adresa " + email);
        }

        User user = new User(
                email,
                passwordEncoder.encode(req.password()),
                req.displayName().trim(),
                Role.USER
        );
        User savedUser = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.fromUser(savedUser);
        String token = tokenProvider.generateToken(principal);

        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getDisplayName(), savedUser.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(principal);

        return new AuthResponse(token, principal.getId(), principal.getUsername(), principal.getDisplayName(), principal.getRole());
    }
}
