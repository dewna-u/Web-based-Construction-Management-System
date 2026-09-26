package com.buildflow.cms.service;

import com.buildflow.cms.dto.ProjectForm;
import com.buildflow.cms.dto.SetupForm;
import com.buildflow.cms.dto.SignupForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.enums.ProjectStatus;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    private AppUser client;

    @BeforeEach
    void setUp() {
        SetupForm setup = new SetupForm();
        setup.setFullName("Admin");
        setup.setEmail("admin@buildflow.test");
        setup.setPassword("Password1!");
        setup.setConfirmPassword("Password1!");
        userService.createFirstAdmin(setup);

        SignupForm signup = new SignupForm();
        signup.setFullName("Client User");
        signup.setEmail("client@buildflow.test");
        signup.setPassword("Password1!");
        signup.setConfirmPassword("Password1!");
        signup.setRole(Role.CLIENT);
        client = userService.signup(signup);
    }

    @Test
    void create_PersistsProject() {
        ProjectForm form = baseForm();
        form.setProjectCode("PRJ-001");
        form.setName("Harbour Tower");
        form.setStartDate(LocalDate.now());
        form.setExpectedEndDate(LocalDate.now().plusMonths(6));
        form.setBudget(new BigDecimal("1500000.00"));

        Project project = projectService.create(form);

        assertThat(project.getId()).isNotNull();
        assertThat(project.getProjectCode()).isEqualTo("PRJ-001");
        assertThat(project.getClient().getId()).isEqualTo(client.getId());
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.PLANNING);
    }

    @Test
    void create_RejectsEndDateBeforeStart() {
        ProjectForm form = baseForm();
        form.setProjectCode("PRJ-BAD");
        form.setName("Invalid Dates");
        form.setStartDate(LocalDate.of(2026, 6, 1));
        form.setExpectedEndDate(LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> projectService.create(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("end date");
    }

    private ProjectForm baseForm() {
        ProjectForm form = new ProjectForm();
        form.setClientId(client.getId());
        form.setStatus(ProjectStatus.PLANNING);
        form.setProgressPercentage(0);
        return form;
    }
}
