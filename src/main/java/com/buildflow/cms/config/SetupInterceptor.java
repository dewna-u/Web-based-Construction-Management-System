package com.buildflow.cms.config;

import com.buildflow.cms.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SetupInterceptor implements HandlerInterceptor {

    private final AppUserRepository appUserRepository;

    public SetupInterceptor(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (path.isEmpty()) {
            path = "/";
        }

        if (isExcluded(path)) {
            return true;
        }

        long userCount = appUserRepository.count();

        if (userCount == 0) {
            if (!path.startsWith("/setup")) {
                response.sendRedirect(contextPath + "/setup");
                return false;
            }
            return true;
        }

        if (path.startsWith("/setup")) {
            response.sendRedirect(contextPath + "/login");
            return false;
        }

        return true;
    }

    private boolean isExcluded(String path) {
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/webjars/")
                || path.startsWith("/uploads/")
                || path.startsWith("/error");
    }
}
