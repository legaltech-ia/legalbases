package com.tys.legalbases.domain.exception;

/** Raised when persisting an uploaded file to object storage fails. */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
