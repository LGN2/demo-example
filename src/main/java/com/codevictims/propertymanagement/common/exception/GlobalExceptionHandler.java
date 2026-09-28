package com.codevictims.propertymanagement.common.exception;

import com.codevictims.propertymanagement.common.dto.response.ApiErrorResponse;
import org.springframework.dao.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<?> domain(ApiException e) {
    return error(e.status, e.code);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    org.springframework.web.bind.MethodArgumentNotValidException.class,
    org.springframework.validation.BindException.class,
    IllegalArgumentException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class
  })
  ResponseEntity<?> invalid(Exception e) {
    return error(400, "INVALID_INPUT");
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    OptimisticLockingFailureException.class,
    PessimisticLockingFailureException.class
  })
  ResponseEntity<?> conflict(Exception e) {
    return error(409, "CONFLICT_RETRY");
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<?> upload(Exception e) {
    return error(413, "FILE_TOO_LARGE");
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<?> unexpected(Exception e) {
    org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class)
        .error("Unhandled API failure: {}", e.getClass().getSimpleName());
    return error(500, "INTERNAL_ERROR");
  }

  private ResponseEntity<?> error(int status, String code) {
    return ResponseEntity.status(status).body(new ApiErrorResponse(code));
  }
}
