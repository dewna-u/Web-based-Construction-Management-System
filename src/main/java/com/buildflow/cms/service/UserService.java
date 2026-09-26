package com.buildflow.cms.service;

import com.buildflow.cms.dto.ChangePasswordForm;
import com.buildflow.cms.dto.ProfileForm;
import com.buildflow.cms.dto.SetupForm;
import com.buildflow.cms.dto.SignupForm;
import com.buildflow.cms.dto.UserAdminForm;
import com.buildflow.cms.entity.AppUser;
import com.buildflow.cms.enums.Role;
import com.buildflow.cms.exception.BusinessException;
import com.buildflow.cms.exception.DuplicateResourceException;
import com.buildflow.cms.exception.ResourceNotFoundException;
import com.buildflow.cms.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser createFirstAdmin(SetupForm form) {
        if (appUserRepository.count() > 0) {
            throw new BusinessException("Initial admin setup is only allowed when no users exist");
        }
        AppUser user = new AppUser();
        user.setFullName(form.getFullName().trim());
        user.setEmail(normalizeEmail(form.getEmail()));
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser signup(SignupForm form) {
        Role role = form.getRole();
        if (role != Role.CLIENT && role != Role.CONTRACTOR) {
            throw new BusinessException("Signup is only allowed for CLIENT or CONTRACTOR roles");
        }
        String email = normalizeEmail(form.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
        AppUser user = new AppUser();
        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(role);
        user.setEnabled(true);
        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser createUser(UserAdminForm form) {
        if (!StringUtils.hasText(form.getPassword())) {
            throw new BusinessException("Password is required when creating a user");
        }
        String email = normalizeEmail(form.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
        AppUser user = new AppUser();
        applyAdminForm(user, form, true);
        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser updateUser(Long id, UserAdminForm form) {
        AppUser user = findById(id);
        String email = normalizeEmail(form.getEmail());
        if (!user.getEmail().equalsIgnoreCase(email) && appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
        applyAdminForm(user, form, false);
        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser toggleEnabled(Long id) {
        AppUser user = findById(id);
        user.setEnabled(!user.isEnabled());
        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser updateRole(Long id, Role role) {
        if (role == null) {
            throw new BusinessException("Role is required");
        }
        AppUser user = findById(id);
        user.setRole(role);
        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser updateProfile(Long userId, ProfileForm form) {
        AppUser user = findById(userId);
        String email = normalizeEmail(form.getEmail());
        if (!user.getEmail().equalsIgnoreCase(email) && appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }
        user.setFullName(form.getFullName().trim());
        user.setEmail(email);
        return appUserRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordForm form) {
        AppUser user = findById(userId);
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("New passwords must match");
        }
        user.setPasswordHash(passwordEncoder.encode(form.getNewPassword()));
        appUserRepository.save(user);
    }

    public AppUser findById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public Page<AppUser> findAll(Pageable pageable) {
        return appUserRepository.findAll(pageable);
    }

    public Page<AppUser> search(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return findAll(pageable);
        }
        String q = keyword.trim().toLowerCase(Locale.ROOT);
        List<AppUser> filtered = appUserRepository.findAll().stream()
                .filter(u -> (u.getFullName() != null && u.getFullName().toLowerCase(Locale.ROOT).contains(q))
                        || (u.getEmail() != null && u.getEmail().toLowerCase(Locale.ROOT).contains(q))
                        || (u.getRole() != null && u.getRole().name().toLowerCase(Locale.ROOT).contains(q)))
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<AppUser> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    public List<AppUser> findByRole(Role role) {
        return appUserRepository.findByRole(role);
    }

    public List<AppUser> findEnabledUsers() {
        return appUserRepository.findByEnabledTrueOrderByFullNameAsc();
    }

    public boolean existsUsers() {
        return appUserRepository.count() > 0;
    }

    public long count() {
        return appUserRepository.count();
    }

    private void applyAdminForm(AppUser user, UserAdminForm form, boolean creating) {
        user.setFullName(form.getFullName().trim());
        user.setEmail(normalizeEmail(form.getEmail()));
        user.setRole(form.getRole());
        user.setEnabled(form.isEnabled());
        if (creating || StringUtils.hasText(form.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
