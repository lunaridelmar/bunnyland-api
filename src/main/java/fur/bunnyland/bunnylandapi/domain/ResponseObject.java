package fur.bunnyland.bunnylandapi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseObject<T>(
        T body,
        MessageError error,
        boolean hasError
) {
    public static <T> ResponseObject<T> ok(T body) {
        return new ResponseObject<>(body, null, false);
    }
    public static <T> ResponseObject<T> fail(MessageError error) {
        return new ResponseObject<>(null, error, true);
    }
}
