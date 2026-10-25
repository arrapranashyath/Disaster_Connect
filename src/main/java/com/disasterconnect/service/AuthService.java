package com.disasterconnect.service;

import com.disasterconnect.dto.*;
import com.disasterconnect.entity.NGOProfile;
import com.disasterconnect.entity.User;
import com.disasterconnect.entity.VolunteerProfile;
import com.disasterconnect.entity.enums.Role;
import com.disasterconnect.exception.BadRequestException;
import com.disasterconnect.exception.DuplicateEmailException;
import com.disasterconnect.exception.ResourceNotFoundException;
import com.disasterconnect.repository.NGOProfileRepository;
import com.disasterconnect.repository.UserRepository;
import com.disasterconnect.repository.VolunteerProfileRepository;
import com.disasterconnect.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final NGOProfileRepository ngoProfileRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       NGOProfileRepository ngoProfileRepository,
                       VolunteerProfileRepository volunteerProfileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.ngoProfileRepository = ngoProfileRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Enforce registration rule: ordinary users cannot self-register as ADMIN or AUTHORITY
        if (request.getRole() == Role.ADMIN || request.getRole() == Role.AUTHORITY) {
            throw new BadRequestException("Self-registration as " + request.getRole() + " is not permitted.");
        }

        // Validate uniqueness of email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }

        // Hash password with BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create and save User
        User user = new User(
                request.getName(),
                request.getEmail(),
                hashedPassword,
                request.getPhone(),
                request.getRole(),
                request.getLocation()
        );
        User savedUser = userRepository.save(user);

        // Create role-specific profiles
        if (savedUser.getRole() == Role.NGO) {
            String orgName = StringUtils.hasText(request.getOrgName()) ? request.getOrgName() : request.getName();
            NGOProfile ngoProfile = new NGOProfile(savedUser, orgName, request.getRegistrationNumber());
            ngoProfileRepository.save(ngoProfile);
        } else if (savedUser.getRole() == Role.VOLUNTEER) {
            VolunteerProfile volunteerProfile = new VolunteerProfile(savedUser, request.getSkills(), request.getAvailability());
            volunteerProfileRepository.save(volunteerProfile);
        }

        // Generate JWT token
        String token = tokenProvider.generateTokenForUser(savedUser.getUserId(), savedUser.getEmail(), savedUser.getRole().name());

        UserResponse userResponse = buildUserResponse(savedUser);
        return new AuthResponse(token, tokenProvider.getExpirationMs() / 1000, userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate credentials against UserDetailsService & PasswordEncoder
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String token = tokenProvider.generateToken(authentication);
        UserResponse userResponse = buildUserResponse(user);

        return new AuthResponse(token, tokenProvider.getExpirationMs() / 1000, userResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return buildUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }

        User updatedUser = userRepository.save(user);
        return buildUserResponse(updatedUser);
    }

    private UserResponse buildUserResponse(User user) {
        UserResponse response = UserResponse.fromEntity(user);

        if (user.getRole() == Role.NGO) {
            Optional<NGOProfile> ngoOpt = ngoProfileRepository.findByUser(user);
            ngoOpt.ifPresent(profile -> {
                response.setOrgName(profile.getOrgName());
                response.setRegistrationNumber(profile.getRegistrationNumber());
                response.setVerificationStatus(profile.getVerificationStatus());
            });
        } else if (user.getRole() == Role.VOLUNTEER) {
            Optional<VolunteerProfile> volOpt = volunteerProfileRepository.findByUser(user);
            volOpt.ifPresent(profile -> {
                response.setSkills(profile.getSkills());
                response.setAvailability(profile.getAvailability());
                response.setVerificationStatus(profile.getVerificationStatus());
            });
        }

        return response;
    }
}
