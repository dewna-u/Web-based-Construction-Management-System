package com.buildflow.cms.controller;

import com.buildflow.cms.dto.AttendanceForm;
import com.buildflow.cms.entity.AttendanceRecord;
import com.buildflow.cms.enums.AttendanceStatus;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.service.AttendanceService;
import com.buildflow.cms.service.ProjectService;
import com.buildflow.cms.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final WorkerService workerService;
    private final ProjectService projectService;

    public AttendanceController(
            AttendanceService attendanceService,
            WorkerService workerService,
            ProjectService projectService) {
        this.attendanceService = attendanceService;
        this.workerService = workerService;
        this.projectService = projectService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long workerId,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        List<AttendanceRecord> records = attendanceService.filter(date, projectId, workerId);
        model.addAttribute("page", toPage(records, pageable));
        model.addAttribute("date", date);
        model.addAttribute("projectId", projectId);
        model.addAttribute("workerId", workerId);
        model.addAttribute("projects", projectService.findAllActive());
        model.addAttribute("workers", workerService.findActive());
        if (date != null) {
            model.addAttribute("summary", attendanceService.summaryForDate(date));
        }
        return "attendance/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String createForm(Model model) {
        if (!model.containsAttribute("attendanceForm")) {
            AttendanceForm form = new AttendanceForm();
            form.setAttendanceDate(LocalDate.now());
            form.setStatus(AttendanceStatus.PRESENT);
            model.addAttribute("attendanceForm", form);
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Record Attendance");
        return "attendance/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String create(
            @Valid @ModelAttribute("attendanceForm") AttendanceForm attendanceForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Record Attendance");
            return "attendance/form";
        }
        try {
            attendanceService.create(attendanceForm);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance recorded successfully.");
            return "redirect:/attendance";
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("attendanceError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Record Attendance");
            return "attendance/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String editForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("attendanceForm")) {
            model.addAttribute("attendanceForm", toForm(attendanceService.findById(id)));
        }
        addFormLookups(model);
        model.addAttribute("pageTitle", "Edit Attendance");
        return "attendance/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_ENGINEER')")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("attendanceForm") AttendanceForm attendanceForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        attendanceForm.setId(id);
        if (bindingResult.hasErrors()) {
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Attendance");
            return "attendance/form";
        }
        try {
            attendanceService.update(id, attendanceForm);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance updated successfully.");
            return "redirect:/attendance";
        } catch (BusinessException | DuplicateResourceException ex) {
            bindingResult.reject("attendanceError", ex.getMessage());
            addFormLookups(model);
            model.addAttribute("pageTitle", "Edit Attendance");
            return "attendance/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            attendanceService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance record deleted.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/attendance";
    }

    private void addFormLookups(Model model) {
        model.addAttribute("workers", workerService.findActive());
        model.addAttribute("statuses", AttendanceStatus.values());
    }

    private AttendanceForm toForm(AttendanceRecord record) {
        AttendanceForm form = new AttendanceForm();
        form.setId(record.getId());
        form.setWorkerId(record.getWorker() != null ? record.getWorker().getId() : null);
        form.setAttendanceDate(record.getAttendanceDate());
        form.setStatus(record.getStatus());
        form.setNotes(record.getNotes());
        return form;
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
