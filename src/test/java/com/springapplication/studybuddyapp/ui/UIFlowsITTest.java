package com.springapplication.studybuddyapp.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UIFlowsIT covers a realistic member end-to-end flow:
 * - Signup
 * - Login (capture session/cookie)
 * - Access dashboard and groups (authenticated)
 * - Logout (status flexible for config)
 *
 * <p>GitHub: https://github.com/AAdewunmi/studybuddy-app</p>
 */
// ...imports...
@SpringBootTest
@AutoConfigureMockMvc
class UIFlowsITTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Commit
    @DisplayName("Member signup→login→dashboard→groups→logout: session is maintained and expected responses are returned")
    void memberFlow_endToEnd_succeeds() throws Exception {
        String email = "flowmember+" + System.currentTimeMillis() + "@mail.com";
        String password = "SecretP@ssword1";

        // --- 1. Signup (register new user) ---
        int signupStatus = mockMvc.perform(post("/signup")
                        .param("email", email)
                        .param("password", password)
                        .param("confirmPassword", password)
                        .with(csrf()))
                .andReturn()
                .getResponse()
                .getStatus();
        assertThat(signupStatus)
                .as("Signup should return 200 (OK) or any 3xx redirect")
                .isIn(200, 201, 301, 302, 303, 307, 308);

        // --- 2. Login (capture session) ---
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", password)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn()
                .getRequest()
                .getSession(false);

        assertThat(session)
                .as("Session should be created and maintained after login")
                .isNotNull();

        // --- 3. Dashboard (should return 200 OK) ---
        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk());

        // --- 4. Groups page (should return 200 OK) ---
        mockMvc.perform(get("/groups").session(session))
                .andExpect(status().isOk());

        // --- 5. Logout (status: 200, 204, or 3xx is all fine) ---
        int logoutStatus = mockMvc.perform(post("/logout").with(csrf()).session(session))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(logoutStatus)
                .as("Logout should return 200 (OK), 204 (No Content), or a 3xx redirect, depending on security config")
                .isIn(200, 204, 301, 302, 303, 307, 308);
    }
}

