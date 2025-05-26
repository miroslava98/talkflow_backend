package com.example.talflow_backend.register_verification.controller;

import com.example.talflow_backend.entity.User;
import com.example.talflow_backend.register_verification.entity.VerificationToken;
import com.example.talflow_backend.register_verification.repository.VerificationTokenRepository;
import com.example.talflow_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpHeaders;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth/verificationRegister")
public class VerificationController {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    public VerificationController(VerificationTokenRepository verificationTokenRepository,
                                  UserRepository userRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token, Model model) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            return null;
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("message", "Token expirado");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expirado");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);



        model.addAttribute("message", "Cuenta verificada correctamente. Ya puedes regresar a la app.");

        // Opcional: eliminar token para que no se pueda reutilizar
        verificationTokenRepository.delete(verificationToken);

        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).body("<h1>Cuenta verificada correctamente. Ya puedes iniciar sesión.</h1>");
    }
}
