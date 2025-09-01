package fur.bunnyland.bunnylandapi.domain;

public enum ErrorCode {
    EMAIL_TAKEN(409),
    INTERNAL_SERVER_ERROR(505);

    ErrorCode(Integer status) {
    }
}
