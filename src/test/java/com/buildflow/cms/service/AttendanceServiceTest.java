package com.buildflow.cms.service;

import com.buildflow.cms.dto.AttendanceForm;
import com.buildflow.cms.dto.SetupForm;
import com.buildflow.cms.entity.Worker;
import com.buildflow.cms.enums.AttendanceStatus;
import com.buildflow.cms.enums.EmploymentType;
import com.buildflow.cms.enums.WorkerStatus;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.repository.WorkerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttendanceServiceTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private UserService userService;

    private Worker worker;

    @BeforeEach
    void setUp() {
        SetupForm setup = new SetupForm();
        setup.setFullName("Admin");
        setup.setEmail("admin@buildflow.test");
        setup.setPassword("Password1!");
        setup.setConfirmPassword("Password1!");
        userService.createFirstAdmin(setup);

        Worker w = new Worker();
        w.setWorkerCode("W-100");
        w.setFullName("Kamal Perera");
        w.setEmploymentType(EmploymentType.DAILY);
        w.setStatus(WorkerStatus.ACTIVE);
        worker = workerRepository.save(w);
    }

    @Test
    void create_PreventsDuplicateWorkerAndDate() {
        AttendanceForm form = new AttendanceForm();
        form.setWorkerId(worker.getId());
        form.setAttendanceDate(LocalDate.now());
        form.setStatus(AttendanceStatus.PRESENT);

        assertThat(attendanceService.create(form).getId()).isNotNull();

        AttendanceForm duplicate = new AttendanceForm();
        duplicate.setWorkerId(worker.getId());
        duplicate.setAttendanceDate(LocalDate.now());
        duplicate.setStatus(AttendanceStatus.ABSENT);

        assertThatThrownBy(() -> attendanceService.create(duplicate))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }
}
