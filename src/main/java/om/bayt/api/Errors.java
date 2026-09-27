package om.bayt.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class Errors {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> domain(ApiException e) { return error(e.status, e.code); }
    @ExceptionHandler({HttpMessageNotReadableException.class, IllegalArgumentException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class})
    ResponseEntity<?> invalid(Exception e) { return error(400, "INVALID_INPUT"); }
    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class,
        PessimisticLockingFailureException.class})
    ResponseEntity<?> conflict(Exception e) { return error(409, "CONFLICT_RETRY"); }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<?> upload(Exception e) { return error(413, "FILE_TOO_LARGE"); }
    private ResponseEntity<?> error(int status, String code) {
        return ResponseEntity.status(status).body(Map.of("code", code));
    }
}
