package fur.bunnyland.bunnylandapi.domain;

public enum ErrorCode {
    USER_NOT_FOUND(401),
    INVALID_CREDENTIALS(401),
    INVALID_REFRESH_TOKEN(401),
    EMAIL_TAKEN(409),
    INTERNAL_SERVER_ERROR(505);

    ErrorCode(Integer status) {
    }
}
