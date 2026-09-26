package com.buildflow.cms.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        ModelAndView mav = new ModelAndView("error/403");
        mav.addObject("message", "You do not have permission to perform this action.");
        mav.addObject("status", 403);
        return mav;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", ex.getMessage());
        mav.addObject("status", 404);
        return mav;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleDuplicate(DuplicateResourceException ex) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", ex.getMessage());
        mav.addObject("status", 409);
        return mav;
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBusiness(BusinessException ex) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", ex.getMessage());
        mav.addObject("status", 400);
        return mav;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleValidation(Exception ex) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", "Validation failed. Please check your input.");
        mav.addObject("status", 400);
        return mav;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoResource(NoResourceFoundException ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", "The requested page was not found.");
        mav.addObject("status", 404);
        return mav;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleDataIntegrity(DataIntegrityViolationException ex) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject(
                "message",
                "This record cannot be deleted because other data still depends on it. "
                        + "Archive, deactivate, or remove related records first.");
        mav.addObject("status", 409);
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneric(Exception ex) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", "An unexpected error occurred.");
        mav.addObject("status", 500);
        return mav;
    }
}
