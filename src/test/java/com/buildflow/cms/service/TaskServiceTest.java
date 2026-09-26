package com.buildflow.cms.service;

import com.buildflow.cms.dto.ProjectForm;
import com.buildflow.cms.dto.SetupForm;
import com.buildflow.cms.dto.SignupForm;
import com.buildflow.cms.dto.TaskForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Project;
import com.buildflow.cms.entity.TaskItem;
import com.buildflow.cms.enums.ProjectStatus;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.enums.TaskPriority;
import com.buildflow.cms.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    private Project project;

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
        AppUser client = userService.signup(signup);

        ProjectForm projectForm = new ProjectForm();
        projectForm.setName("Site A");
        projectForm.setProjectCode("SITE-A");
        projectForm.setClientId(client.getId());
        projectForm.setStatus(ProjectStatus.ACTIVE);
        project = projectService.create(projectForm);
    }

    @Test
    void create_AndDetectOverdue() {
        TaskForm overdueForm = new TaskForm();
        overdueForm.setTitle("Pour foundation");
        overdueForm.setProjectId(project.getId());
        overdueForm.setPriority(TaskPriority.HIGH);
        overdueForm.setStatus(TaskStatus.IN_PROGRESS);
        overdueForm.setDueDate(LocalDate.now().minusDays(2));
        overdueForm.setCompletionPercentage(40);

        TaskItem overdue = taskService.create(overdueForm);

        assertThat(taskService.isOverdue(overdue)).isTrue();
        assertThat(taskService.findOverdue()).extracting(TaskItem::getId).contains(overdue.getId());
    }

    @Test
    void completedTask_IsNotOverdue() {
        TaskForm form = new TaskForm();
        form.setTitle("Install temporary fencing");
        form.setProjectId(project.getId());
        form.setPriority(TaskPriority.MEDIUM);
        form.setStatus(TaskStatus.COMPLETED);
        form.setDueDate(LocalDate.now().minusDays(5));
        form.setCompletionPercentage(100);

        TaskItem task = taskService.create(form);

        assertThat(taskService.isOverdue(task)).isFalse();
    }
}
