package com.lockmanager.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lockmanager.dto.AcquireRequest;
import com.lockmanager.dto.AcquireResponse;
import com.lockmanager.dto.ReleaseRequest;
import com.lockmanager.dto.RenewRequest;
import com.lockmanager.service.LockService;


@RestController
@RequestMapping("/lock")
public class LockController {

	private final LockService lockService;
	
	
	public LockController(LockService lockService) {
		this.lockService = lockService;
	}
	
	
	@PostMapping("/acquire")
	public ResponseEntity<AcquireResponse> acquire(@RequestBody @Valid AcquireRequest req) throws InterruptedException {
		var res = lockService.acquire(req.resource(), req.owner(), req.requestId(), req.ttlMs(), req.waitMs() == null ? 0L : req.waitMs(), req.mode() == null ? "EXCLUSIVE" : req.mode(), req.reentrant() == null ? false : req.reentrant());
		return (ResponseEntity<AcquireResponse>) ResponseEntity.ok(res);
	}
	
	
	@PostMapping("/release")
	public ResponseEntity<?> release(@RequestBody @Valid ReleaseRequest req) {
		var res = lockService.release(req.resource(), req.owner(), req.token());
		return ResponseEntity.ok(res);
	}
	
	
	@PostMapping("/renew")
	public ResponseEntity<?> renew(@RequestBody @Valid RenewRequest req) {
			var res = lockService.renew(req.resource(), req.owner(), req.token(), req.ttlMs());
			return ResponseEntity.ok(res);
	}
	
	
	@GetMapping("/status")
	public ResponseEntity<?> status(@RequestParam String resource) {
		return ResponseEntity.ok(lockService.status(resource));
	}
}