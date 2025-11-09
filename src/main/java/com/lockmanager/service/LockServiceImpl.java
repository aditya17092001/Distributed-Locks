package com.lockmanager.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lockmanager.dto.AcquireResponse;
import com.lockmanager.redis.RedisLuaScripts;


@Service
public class LockServiceImpl implements LockService{
	
	private final StringRedisTemplate redis;
	private final RedisLuaScripts scripts;
	private final ObjectMapper mapper = new ObjectMapper();

	private String lockKey(String resource) { return "lock:" + resource; }
	private String fenceKey(String resource) { return "fence:" + resource; }
	private String waitqKey(String resource) { return "waitq:" + resource; }

	public LockServiceImpl(StringRedisTemplate redis, RedisLuaScripts scripts) {
		this.redis = redis;
		this.scripts = scripts;
	}

	@Override
	public AcquireResponse acquire(String resource, String owner, String requestId, long ttlMs, long waitMs,
			String mode, boolean reentrant) throws InterruptedException {
		if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString();
		
		// Execute acquire lua script
        RedisScript<List> acquireScript = RedisScript.of(scripts.acquireScript(), List.class);

        List<Object> res = redis.execute(acquireScript,
        		List.of(lockKey(resource), fenceKey(resource), waitqKey(resource)),
                owner, requestId, Long.toString(ttlMs), mode, Boolean.toString(reentrant));

        if (res == null || res.isEmpty()) {
            return new AcquireResponse(false, false, null, 0L, null, "failed");
        }

        Object granted = res.get(0);
        if ("1".equals(granted.toString()) || (granted instanceof Long && ((Long) granted) == 1L)) {
            String token = res.size() > 1 && res.get(1) != null ? res.get(1).toString() : null;
            long expiresAt = Instant.now().toEpochMilli() + ttlMs;
            return new AcquireResponse(true, false, token, expiresAt, null, "granted");
        }

        return new AcquireResponse(false, true, null, 0L, 0, "queued");
	}

	@Override
	public Object release(String resource, String owner, String token) {
		RedisScript<List> releaseScript = RedisScript.of(scripts.releaseScript(), List.class);

        List<Object> res = redis.execute(releaseScript,
                List.of(lockKey(resource), waitqKey(resource), fenceKey(resource)),
                owner, token, Long.toString(5000));

        return Map.of("result", res);
	}

	@Override
	public Object renew(String resource, String owner, String token, long ttlMs) {
		RedisScript<List> renewScript = RedisScript.of(scripts.renewScript(), List.class);

        List<Object> res = redis.execute(renewScript,
                List.of(lockKey(resource)),
                owner, token, Long.toString(ttlMs));

        return Map.of("result", res);
	}

	@Override
	public Object status(String resource) {
		String val = redis.opsForValue().get(lockKey(resource));
        if (val == null) return Map.of("locked", false);
        try {
            Map<?, ?> obj = mapper.readValue(val, Map.class);
            return Map.of(
                    "locked", true,
                    "owner", obj.get("owner"),
                    "token", obj.get("token"),
                    "expiresAt", obj.get("expires")
            );
        } catch (Exception e) {
            return Map.of("locked", true, "raw", val);
        }
	}

}
