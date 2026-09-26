package com.buildflow.cms.security;

import com.buildflow.cms.dto.SetupForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void setupPage_LoadsWhenNoUsers() throws Exception {
        mockMvc.perform(get("/setup"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/setup"));
    }

    @Test
    void loginPage_Loads() throws Exception {
        ensureAdminExists();

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void protectedRoute_RedirectsAnonymousToLogin() throws Exception {
        ensureAdminExists();

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void protectedRoute_AllowsAuthenticatedUser() throws Exception {
        AppUser admin = ensureAdminExists();

        mockMvc.perform(get("/dashboard").with(user(new CustomUserDetails(admin))))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"));
    }

    @Test
    void setupSubmit_CreatesAdminAndRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/setup")
                        .with(csrf())
                        .param("fullName", "Demo Admin")
                        .param("email", "demo.admin@buildflow.test")
                        .param("password", "Password1!")
                        .param("confirmPassword", "Password1!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    private AppUser ensureAdminExists() {
        if (userService.existsUsers()) {
            return userService.findByRole(com.buildflow.cms.enums.Role.ADMIN).getFirst();
        }
        SetupForm form = new SetupForm();
        form.setFullName("Admin");
        form.setEmail("admin@buildflow.test");
        form.setPassword("Password1!");
        form.setConfirmPassword("Password1!");
        return userService.createFirstAdmin(form);
    }
}
