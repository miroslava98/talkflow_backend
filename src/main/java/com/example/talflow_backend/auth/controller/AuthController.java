package com.example.talflow_backend.auth.controller;


import com.example.talflow_backend.entity.User;
import com.example.talflow_backend.repository.UserRepository;
import com.example.talflow_backend.auth.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/talkflow/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtil;

    // Login
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) {
        if (!userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe usuario con este correo");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            user.getPassword()
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
    public ResponseEntity<?> registerUser(@RequestBody @Valid User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El correo ya está en uso.");
        }

        User newUser = new User(
                null,
                user.getNombre(),
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()),
                user.getFechaNacimiento(),
                user.getAvatar()
        );

        userRepository.save(newUser);

        return ResponseEntity.ok("Usuario registrado correctamente.");
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

        public String getNombre(){
            return nombre;
        }
    }
}
