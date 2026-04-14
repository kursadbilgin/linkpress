package com.kursad.linkpress.exception;

public class UrlInactiveException extends RuntimeException {

    public UrlInactiveException(String shortCode) {
        super("Short URL is inactive: " + shortCode);
    }
}
