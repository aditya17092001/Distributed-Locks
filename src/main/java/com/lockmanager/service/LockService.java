package com.lockmanager.service;

import com.lockmanager.dto.AcquireResponse;

public interface LockService {
	AcquireResponse acquire(String resource, String owner, String requestId, long ttlMs, long waitMs, String mode, boolean reentrant) throws InterruptedException;
	Object release(String resource, String owner, String token);
	Object renew(String resource, String owner, String token, long ttlMs);
	Object status(String resource);
}
