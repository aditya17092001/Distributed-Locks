package com.lockmanager.dto;

public record AcquireResponse(
		boolean success, 
		boolean queued, 
		String token, 
		long expiresAt, 
		Integer position, 
		String message
) {}
