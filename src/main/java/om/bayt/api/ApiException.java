package om.bayt.api;

public class ApiException extends RuntimeException {
    public final int status;
    public final String code;
    public ApiException(int status, String code) { super(code); this.status = status; this.code = code; }
    public static ApiException invalid(String code) { return new ApiException(400, code); }
    public static ApiException missing() { return new ApiException(404, "NOT_FOUND"); }
    public static ApiException forbidden() { return new ApiException(403, "FORBIDDEN"); }
    public static ApiException conflict(String code) { return new ApiException(409, code); }
}
