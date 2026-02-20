package com.josewolf.task_api.dto;

import org.springframework.http.HttpStatus;

public record StandardError(
        Integer status,
        String error,
        String message
) {
}
