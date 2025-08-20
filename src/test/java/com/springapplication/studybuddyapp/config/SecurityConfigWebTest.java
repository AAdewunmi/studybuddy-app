// path: src/test/java/com/springapplication/studybuddyapp/config/SecurityConfigWebTest.java
package com.springapplication.studybuddyapp.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Verifies HTTP security behavior at the web layer only:
 * - GET /dashboard redirects unauthenticated to login (302)
 * - POST /dashboard without CSRF → 403
 * - POST /dashboard with CSRF → 302 to login (still unauthenticated)
 */
@WebMvcTest(controllers = SecurityConfigWebTest.DashboardStub.class)
@Import(TestSecurityConfig.class)              // use our minimal rules
@AutoConfigureMockMvc(addFilters = true)       // 🔑 include Spring Security filter chain
class SecurityConfigWebTest {

    @Autowired
    MockMvc mvc;

    // Minimal controller just for attaching rules to real endpoints
    @RestController
    static class DashboardStub {
        @GetMapping("/dashboard")
        public String getDash() { return "ok"; }

        @PostMapping(value = "/dashboard", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        public String postDash() { return "posted"; }
    }

    @Test
    @WithAnonymousUser
    void dashboard_requiresAuth_redirectsToLogin() throws Exception {
        mvc.perform(get("/dashboard"))
                .andExpect(status().isFound()) // 302
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
    }

    @Test
    @WithAnonymousUser
    void dashboard_post_withoutCsrf_forbidden() throws Exception {
        mvc.perform(post("/dashboard").contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden()); // 403 due to missing CSRF token
    }

    @Test
    @WithAnonymousUser
    void dashboard_post_withCsrf_redirectsToLogin() throws Exception {
        mvc.perform(post("/dashboard")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isFound()) // 302 (still unauthenticated)
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
    }
}





