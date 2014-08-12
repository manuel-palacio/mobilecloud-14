package org.magnum.dataup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@ControllerAdvice
public class RestExceptionProcessor {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(VideoNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorInfo videoNotFound(HttpServletRequest req, VideoNotFoundException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.no.video.id", null, locale);

        errorMessage += ex.getId();
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

}