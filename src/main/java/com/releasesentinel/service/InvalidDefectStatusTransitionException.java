package com.releasesentinel.service;

public class InvalidDefectStatusTransitionException extends RuntimeException {

    public InvalidDefectStatusTransitionException(String message) {
        super(message);
    }
}
