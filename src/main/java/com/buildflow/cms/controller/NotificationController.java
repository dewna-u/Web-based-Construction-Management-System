package com.buildflow.cms.controller;

import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.entity.Notification;
import com.buildflow.cms.service.NotificationService;
import com.buildflow.cms.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String list(@PageableDefault(size = 10) Pageable pageable, Model model) {
        AppUser currentUser = SecurityUtils.requireCurrentUser();
        List<Notification> notifications = notificationService.listForUser(currentUser.getId());
        model.addAttribute("page", toPage(notifications, pageable));
        model.addAttribute("unreadCount", notificationService.countUnread(currentUser.getId()));
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        AppUser currentUser = SecurityUtils.requireCurrentUser();
        Notification owned = notificationService.listForUser(currentUser.getId()).stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (owned == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Notification not found.");
            return "redirect:/notifications";
        }
        Notification notification = notificationService.markRead(id);
        redirectAttributes.addFlashAttribute("successMessage", "Notification marked as read.");
        if (notification.getLink() != null && !notification.getLink().isBlank()) {
            return "redirect:" + notification.getLink();
        }
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllRead(RedirectAttributes redirectAttributes) {
        AppUser currentUser = SecurityUtils.requireCurrentUser();
        int updated = notificationService.markAllRead(currentUser.getId());
        redirectAttributes.addFlashAttribute(
                "successMessage",
                updated > 0 ? "All notifications marked as read." : "No unread notifications.");
        return "redirect:/notifications";
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        List<T> content = start >= items.size() ? List.of() : items.subList(start, end);
        return new PageImpl<>(content, pageable, items.size());
    }
}
