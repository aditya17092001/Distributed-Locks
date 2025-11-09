package com.lockmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record AcquireRequest(
	@NotBlank String resource,
	@NotBlank String owner,
	String requestId,
	@NotNull Long ttlMs,
	Long waitMs,
	String mode,
	Boolean reentrant
) {
	public AcquireRequest {
        if (ttlMs <= 0) {
            throw new IllegalArgumentException("ttlMs must be positive");
        }
    }
}
