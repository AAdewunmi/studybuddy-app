package com.springapplication.studybuddyapp.config;

import com.springapplication.studybuddyapp.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration-style slice test for SecurityFilterChain.
 *
 * Uses a minimal test controller to give Security a few concrete endpoints:
 *  - "/" (public)
 *  - "/auth/login" (custom login page; permitted)
 *  - "/dashboard" (protected)
 *
 * Expectations:
 *  - GET "/" is permitted for anonymous
 *  - GET "/auth/login" is permitted for anonymous
 *  - GET "/dashboard" redirects anonymous users to "/auth/login"
 *  - POST "/dashboard" without CSRF is 403
 *  - POST "/dashboard" with CSRF still redirects anonymous to "/auth/login"
 */
@WebMvcTest(controllers = SecurityConfigWebTest.TestEndpoints.class)
@Import(SecurityConfig.class) // bring in your security configuration
class SecurityConfigWebTest {

    @Autowired
    private MockMvc mvc;

    @Controller
    static class TestEndpoints {
        @GetMapping(path = "/")
        public String home() {
            // return simple view name; we don't care about view resolution here
            return "ok";
        }

        @GetMapping(path = "/auth/login")
        public String loginPage() {
            return "login";
        }

        @GetMapping(path = "/dashboard")
        public String dashboard() {
            return "dashboard";
        }

        @PostMapping(path = "/dashboard")
        public String dashboardPost() {
            return "dashboard";
        }
    }

//    @Test
//    @DisplayName("Anonymous GET / is permitted (200 OK)")
//    void root_isPermitted() throws Exception {
//        mvc.perform(get("/"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("Anonymous GET /auth/login is permitted (200 OK)")
//    void login_isPermitted() throws Exception {
//        mvc.perform(get("/auth/login"))
//                .andExpect(status().isOk());
//    }

    @Test
    @DisplayName("Anonymous GET /dashboard requires auth (302 to /auth/login)")
    void dashboard_requiresAuth_redirectsToLogin() throws Exception {
        mvc.perform(get("/dashboard"))
                .andExpect(status().isFound()) // 302
                .andExpect(header().string("Location", containsString("/auth/login")));
    }

    @Test
    @DisplayName("Anonymous POST /dashboard without CSRF -> 403 Forbidden")
    void dashboard_post_withoutCsrf_forbidden() throws Exception {
        mvc.perform(post("/dashboard")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Anonymous POST /dashboard with CSRF -> 302 redirect to /auth/login")
    void dashboard_post_withCsrf_redirectsToLogin() throws Exception {
        mvc.perform(post("/dashboard")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("/auth/login")));
    }
}


