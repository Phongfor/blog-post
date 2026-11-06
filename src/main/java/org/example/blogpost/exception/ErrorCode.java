package org.example.blogpost.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // SYSTEM
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1003, "Validation error", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(1004, "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    RESOURCE_NOT_FOUND(1005, "Resource not found", HttpStatus.NOT_FOUND),
    UNSUPPORTED_MEDIA_TYPE(1006, "Unsupported media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    // USER MODULE
    USER_NOT_FOUND(1101, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1102, "User already exists", HttpStatus.CONFLICT),


    // AUTH MODULE
    UNAUTHENTICATED(1201, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1202, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1203, "Email or password is incorrect", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1204, "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    USERNAME_ALREADY_EXISTS(1103, "Username already exists", HttpStatus.CONFLICT),
    ACCOUNT_DISABLED(1205, "Account is disabled", HttpStatus.FORBIDDEN),

    //POST MODULE
    POST_NOT_FOUND(2001, "Post not found", HttpStatus.NOT_FOUND),
    NOT_POST_OWNER(2002, "You are not the owner of this post", HttpStatus.FORBIDDEN),
    POST_IMAGE_NOT_FOUND(2003, "Post image not found", HttpStatus.NOT_FOUND),
    INVALID_IMAGE_FILE(2004, "Only JPG, PNG or WEBP images are allowed", HttpStatus.BAD_REQUEST),
    TOO_MANY_IMAGES(2005, "Too many images for this post", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(2006, "File too large (max 5MB per image, 20MB per request)", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_STORAGE_ERROR(2007, "Could not store file", HttpStatus.INTERNAL_SERVER_ERROR),

    // Comment
    COMMENT_NOT_FOUND(3001, "Comment not found", HttpStatus.NOT_FOUND),
    NOT_COMMENT_OWNER(3002, "You are not the owner of this comment", HttpStatus.FORBIDDEN),
    COMMENT_EMPTY(3003, "Comment must have content or at least one image", HttpStatus.BAD_REQUEST),
    TOO_MANY_COMMENT_IMAGES(3004, "A comment can have at most 3 images", HttpStatus.BAD_REQUEST),
    COMMENT_CONTENT_TOO_LONG(3005, "Comment content must not exceed 5000 characters", HttpStatus.BAD_REQUEST),

    // ADMIN MODULE
    CANNOT_LOCK_ADMIN(4001, "Admin accounts cannot be locked", HttpStatus.FORBIDDEN);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
