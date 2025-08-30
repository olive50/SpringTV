package com.tvboot.tivio.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends TvBootException {
    public ResourceNotFoundException(String resource, Object id) {
        super(String.format("%s not found with id: %s", resource, id),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND,
                id);
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}