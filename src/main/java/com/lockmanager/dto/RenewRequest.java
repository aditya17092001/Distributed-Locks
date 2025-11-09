package com.lockmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RenewRequest(
	@NotBlank String resource,
	@NotBlank String owner,
	@NotBlank String token,
	@NotNull Long ttlMs
){}
