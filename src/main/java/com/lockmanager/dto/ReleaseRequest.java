package com.lockmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record ReleaseRequest(
	@NotBlank String resource,
	@NotBlank String owner,
	@NotBlank String token
) {}
