package com.springapplication.studybuddyapp.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springapplication.studybuddyapp.controller.AuthController;
import com.springapplication.studybuddyapp.exception.BadRequestException;
import com.springapplication.studybuddyapp.exception.ConflictException;
import com.springapplication.studybuddyapp.service.AuthService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for slice test
@Import(AuthControllerTest.TestAdvice.class)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean AuthService authService;
    @MockBean AuthenticationManager authenticationManager;
    @MockBean SecurityContextRepository securityContextRepository;

    // ---------- /auth/signup ----------

    @Test
    void signup_success_created_and_rolesIncludeRoleUser() throws Exception {
        String name = "Alice";
        String email = "alice@example.com";
        String password = "Str0ngP@ss1";

        UserResponse body = new UserResponse(1L, name, email, Set.of("ROLE_USER"));
        when(authService.signup(name, email, password)).thenReturn(body);

        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "name", name,
                                "email", email,
                                "password", password,
                                "passwordConfirm", password
                        ))))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.roles", hasItem("ROLE_USER")));

        verify(authService).signup(name, email, password);
    }

    @Test
    void signup_duplicateEmail_returns409() throws Exception {
        when(authService.signup(anyString(), anyString(), anyString()))
                .thenThrow(new ConflictException("Email already in use"));

        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "name", "Jane",
                                "email", "jane@example.com",
                                "password", "GoodPass1!",
                                "passwordConfirm", "GoodPass1!"
                        ))))
                .andExpect(status().isConflict());

        verify(authService).signup(eq("Jane"), eq("jane@example.com"), eq("GoodPass1!"));
    }

    @Test
    void signup_invalidBody_returns400() throws Exception {
        // invalid payload hits bean validation before service
        mvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " ",
                                  "email": "not-an-email",
                                  "password": "short",
                                  "passwordConfirm": "different"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never()).signup(anyString(), anyString(), anyString());
    }

    // ---------- /auth/login ----------

    @Test
    void login_success_ok_and_contextSaved() throws Exception {
        String email = "alice@example.com";
        String password = "Str0ngP@ss1";

        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Login successful"));

        verify(authenticationManager).authenticate(any());
        verify(securityContextRepository).saveContext(any(), any(), any());
    }

    // ---------- local advice for this slice ----------

    @RestControllerAdvice(assignableTypes = AuthController.class)
    static class TestAdvice {
        @ExceptionHandler(ConflictException.class)
        ResponseEntity<Map<String, Object>> onConflict(ConflictException ex) {
            return ResponseEntity.status(CONFLICT).body(Map.of(
                    "code", CONFLICT.value(),
                    "message", ex.getMessage()
            ));
        }

        @ExceptionHandler(BadRequestException.class)
        ResponseEntity<Map<String, Object>> onBadRequest(BadRequestException ex) {
            return ResponseEntity.status(BAD_REQUEST).body(Map.of(
                    "code", BAD_REQUEST.value(),
                    "message", ex.getMessage()
            ));
        }

        @ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class })
        ResponseEntity<Map<String, Object>> onValidation(Exception ignored) {
            return ResponseEntity.status(BAD_REQUEST).body(Map.of(
                    "code", BAD_REQUEST.value(),
                    "message", "Validation error"
            ));
        }
    }
}
