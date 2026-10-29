package org.example.blogpost.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // SYSTEM
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // USER MODULE
    USER_NOT_FOUND(1101, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1102, "User already exists", HttpStatus.CONFLICT),


    // AUTH MODULE
    UNAUTHENTICATED(1201, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1202, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1203, "Email or password is incorrect", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1204, "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    USERNAME_ALREADY_EXISTS(1103, "Username already exists", HttpStatus.CONFLICT),
    ACCOUNT_DISABLED(1205, "Account is disabled", HttpStatus.FORBIDDEN),;



    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
