package org.example.API.controllers;

import com.google.gson.Gson;
import org.example.Domain.dtos.RequestDto;
import org.example.Domain.dtos.ResponseDto;
import org.example.Domain.dtos.auth.LoginRequestDto;
import org.example.Domain.dtos.auth.RegisterRequestDto;
import org.example.Domain.dtos.auth.UserResponseDto;
import org.example.Domain.models.User;
import org.example.DataAccess.services.AuthService;

public class AuthController {
    private final AuthService authService;
    private final Gson gson = new Gson();

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public ResponseDto route(RequestDto request) {
        try {
            switch (request.getRequest()) {
                case "login":
                    return handleLogin(request);
                case "register":
                    return handleRegister(request);
                case "logout":
                    return handleLogout(request);
                default:
                    return new ResponseDto(false, "Unknown request: " + request.getRequest(), null);
            }
        } catch (Exception e) {
            System.out.println("[AuthController] Unhandled exception: " + e.getMessage());
            return new ResponseDto(false, e.getMessage(), null);
        }
    }

    // --- LOGIN ---
    private ResponseDto handleLogin(RequestDto request) {
        try {
            LoginRequestDto loginDto = gson.fromJson(request.getData(), LoginRequestDto.class);
            if (loginDto == null || loginDto.getUsernameOrEmail() == null || loginDto.getPassword() == null) {
                return new ResponseDto(false, "Invalid login payload", null);
            }

            // AuthService.login ahora devuelve User en el backend (null si falla)
            User user = authService.login(loginDto.getUsernameOrEmail(), loginDto.getPassword());
            if (user == null) {
                return new ResponseDto(false, "Invalid credentials", null);
            }

            UserResponseDto userDto = mapToUserResponseDto(user);
            return new ResponseDto(true, "Login successful", gson.toJson(userDto));
        } catch (Exception e) {
            System.out.println("Error in handleLogin: " + e.getMessage());
            return new ResponseDto(false, "Error during login: " + e.getMessage(), null);
        }
    }

    // --- REGISTER ---
    private ResponseDto handleRegister(RequestDto request) {
        try {
            RegisterRequestDto regDto = gson.fromJson(request.getData(), RegisterRequestDto.class);
            if (regDto == null || regDto.getUsername() == null || regDto.getEmail() == null || regDto.getPassword() == null) {
                return new ResponseDto(false, "Invalid register payload", null);
            }

            // AuthService.register devuelve User o null si ya existe
            User user = authService.register(regDto.getUsername(), regDto.getEmail(), regDto.getPassword(), regDto.getRole());
            if (user == null) {
                return new ResponseDto(false, "User already exists or could not be created", null);
            }

            UserResponseDto userDto = mapToUserResponseDto(user);
            return new ResponseDto(true, "User registered successfully", gson.toJson(userDto));
        } catch (Exception e) {
            System.out.println("Error in handleRegister: " + e.getMessage());
            return new ResponseDto(false, "Error during register: " + e.getMessage(), null);
        }
    }

    // --- LOGOUT ---
    private ResponseDto handleLogout(RequestDto request) {
        try {
            return new ResponseDto(true, "Logout successful", null);
        } catch (Exception e) {
            System.out.println("Error in handleLogout: " + e.getMessage());
            return new ResponseDto(false, "Error during logout: " + e.getMessage(), null);
        }
    }

    // --- HELPER: map User to UserResponseDto safely ---
    private UserResponseDto mapToUserResponseDto(User user) {
        if (user == null) return null;
        String createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : "";
        String updatedAt = user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "";

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                createdAt,
                updatedAt
        );
    }
}
