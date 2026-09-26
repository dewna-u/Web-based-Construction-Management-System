package com.buildflow.cms.service;

import com.buildflow.cms.dto.SetupForm;
import com.buildflow.cms.dto.SignupForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createFirstAdmin_PersistsAdminWithBcryptHash() {
        SetupForm form = new SetupForm();
        form.setFullName("System Admin");
        form.setEmail("admin@buildflow.test");
        form.setPassword("Password1!");
        form.setConfirmPassword("Password1!");

        AppUser admin = userService.createFirstAdmin(form);

        assertThat(admin.getId()).isNotNull();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getEmail()).isEqualTo("admin@buildflow.test");
        assertThat(admin.getPasswordHash()).isNotEqualTo("Password1!");
        assertThat(passwordEncoder.matches("Password1!", admin.getPasswordHash())).isTrue();
        assertThat(userService.existsUsers()).isTrue();
    }

    @Test
    void createFirstAdmin_RejectsWhenUsersAlreadyExist() {
        createAdmin();

        SetupForm form = new SetupForm();
        form.setFullName("Another Admin");
        form.setEmail("other@buildflow.test");
        form.setPassword("Password1!");
        form.setConfirmPassword("Password1!");

        assertThatThrownBy(() -> userService.createFirstAdmin(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no users exist");
    }

    @Test
    void signup_CreatesClient() {
        createAdmin();

        SignupForm form = new SignupForm();
        form.setFullName("Alice Client");
        form.setEmail("alice@buildflow.test");
        form.setPassword("Password1!");
        form.setConfirmPassword("Password1!");
        form.setRole(Role.CLIENT);

        AppUser user = userService.signup(form);

        assertThat(user.getRole()).isEqualTo(Role.CLIENT);
        assertThat(passwordEncoder.matches("Password1!", user.getPasswordHash())).isTrue();
        assertThat(user.getPasswordHash()).doesNotContain("Password1!");
    }

    @Test
    void signup_RejectsDuplicateEmail() {
        createAdmin();

        SignupForm form = new SignupForm();
        form.setFullName("Alice Client");
        form.setEmail("alice@buildflow.test");
        form.setPassword("Password1!");
        form.setConfirmPassword("Password1!");
        form.setRole(Role.CLIENT);
        userService.signup(form);

        SignupForm duplicate = new SignupForm();
        duplicate.setFullName("Alice Two");
        duplicate.setEmail("Alice@buildflow.test");
        duplicate.setPassword("Password1!");
        duplicate.setConfirmPassword("Password1!");
        duplicate.setRole(Role.CONTRACTOR);

        assertThatThrownBy(() -> userService.signup(duplicate))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void signup_RejectsAdminRole() {
        createAdmin();

        SignupForm form = new SignupForm();
        form.setFullName("Hacker");
        form.setEmail("hacker@buildflow.test");
        form.setPassword("Password1!");
        form.setConfirmPassword("Password1!");
        form.setRole(Role.ADMIN);

        assertThatThrownBy(() -> userService.signup(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CLIENT or CONTRACTOR");
    }

    private void createAdmin() {
        SetupForm form = new SetupForm();
        form.setFullName("System Admin");
        form.setEmail("admin@buildflow.test");
        form.setPassword("Password1!");
        form.setConfirmPassword("Password1!");
        userService.createFirstAdmin(form);
    }
}
