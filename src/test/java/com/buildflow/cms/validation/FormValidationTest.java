package com.buildflow.cms.validation;

import com.buildflow.cms.dto.ProjectForm;
import com.buildflow.cms.dto.SignupForm;
import com.buildflow.cms.enums.ProjectStatus;
import com.buildflow.cms.enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FormValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    void signupForm_RejectsMismatchedPasswords() {
        SignupForm form = new SignupForm();
        form.setFullName("Test User");
        form.setEmail("user@buildflow.test");
        form.setPassword("Password1!");
        form.setConfirmPassword("Different1!");
        form.setRole(Role.CLIENT);

        Set<ConstraintViolation<SignupForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getMessage().toLowerCase().contains("password"));
    }

    @Test
    void signupForm_RejectsAdminRole() {
        SignupForm form = new SignupForm();
        form.setFullName("Test User");
        form.setEmail("user@buildflow.test");
        form.setPassword("Password1!");
        form.setConfirmPassword("Password1!");
        form.setRole(Role.ADMIN);

        Set<ConstraintViolation<SignupForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("CLIENT or CONTRACTOR"));
    }

    @Test
    void projectForm_RejectsEndBeforeStart() {
        ProjectForm form = new ProjectForm();
        form.setName("Bad Dates");
        form.setProjectCode("BAD-1");
        form.setClientId(1L);
        form.setStatus(ProjectStatus.PLANNING);
        form.setStartDate(LocalDate.of(2026, 12, 1));
        form.setExpectedEndDate(LocalDate.of(2026, 1, 1));

        Set<ConstraintViolation<ProjectForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getMessage().toLowerCase().contains("end date"));
    }

    @Test
    void projectForm_AcceptsValidDates() {
        ProjectForm form = new ProjectForm();
        form.setName("Good Dates");
        form.setProjectCode("GOOD-1");
        form.setClientId(1L);
        form.setStatus(ProjectStatus.PLANNING);
        form.setStartDate(LocalDate.of(2026, 1, 1));
        form.setExpectedEndDate(LocalDate.of(2026, 12, 1));

        Set<ConstraintViolation<ProjectForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }
}
