package com.example.talflow_backend.auth.controller;


import com.example.talflow_backend.entity.User;
import com.example.talflow_backend.entity.VerificationToken;
import com.example.talflow_backend.repository.UserRepository;
import com.example.talflow_backend.auth.security.JwtUtils;
import com.example.talflow_backend.repository.VerificationTokenRepository;
import com.example.talflow_backend.service.ResendEmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/talkflow/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private ResendEmailService resendEmailService;

    // Login
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        if (!userRepository.existsByEmail(loginRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe usuario con este correo");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());

            return ResponseEntity.ok(new JwtResponse(token, userDetails.getUsername()));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    // Registro
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El correo ya está en uso.");
        }

        User newUser = new User(
                null,
                registerRequest.getNombre(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFecha_nacimiento(),
                registerRequest.getAvatar()
        );
        newUser.setEnabled(false);


        userRepository.save(newUser);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, newUser, LocalDateTime.now().plusDays(1));
        verificationTokenRepository.save(verificationToken);

        resendEmailService.sendVerificationEmail(newUser.getEmail(), token);

        return ResponseEntity.ok("Te hemos enviado un correo de verificación.");
    }


    // DTOs internos
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        private String nombre;
        private String email;
        private LocalDate fecha_nacimiento;
        private String password;
        private String avatar;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public LocalDate getFecha_nacimiento() {
            return fecha_nacimiento;
        }

        public void setFecha_nacimiento(LocalDate fecha_nacimiento) {
            this.fecha_nacimiento = fecha_nacimiento;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class JwtResponse {
        private String token;
        private String nombre;

        public JwtResponse(String token, String nombre) {
            this.token = token;
            this.nombre = nombre;
        }

        public String getToken() {
            return token;
        }

        public String getNombre() {
            return nombre;
        }
    }
}

