package com.lockmanager.redis;

import org.springframework.stereotype.Component;


@Component
public class RedisLuaScripts {


	// Acquire script: KEYS[1]=lockKey, KEYS[2]=fenceKey, KEYS[3]=waitqKey
	// ARGV: owner, requestId, ttlMs, mode, reentrant
	public String acquireScript() {
		return "local lock = redis.call(\"GET\", KEYS[1])\n"
		+ "local now = redis.call(\"TIME\")[1] * 1000\n"
		+ "if not lock then\n"
		+ " local fence = redis.call(\"INCR\", KEYS[2])\n"
		+ " local payload = cjson.encode({owner=ARGV[1], token=fence, expires=now + tonumber(ARGV[3]), reentrancy=1, requestId=ARGV[2], mode=ARGV[4]})\n"
		+ " redis.call(\"SET\", KEYS[1], payload, \"PX\", ARGV[3])\n"
		+ " return {1, tostring(fence)}\n"
		+ "else\n"
		+ " local data = cjson.decode(lock)\n"
		+ " if data.owner == ARGV[1] and ARGV[5] == \"true\" then\n"
		+ " data.reentrancy = data.reentrancy + 1\n"
		+ " data.expires = now + tonumber(ARGV[3])\n"
		+ " redis.call(\"SET\", KEYS[1], cjson.encode(data), \"PX\", ARGV[3])\n"
		+ " return {1, tostring(data.token)}\n"
		+ " end\n"
		+ " return {0, nil}\n"
		+ "end\n";
	}
	
	
	// Release script: KEYS[1]=lockKey, KEYS[2]=waitqKey, KEYS[3]=fenceKey
	// ARGV: owner, token, defaultTtlMs
	public String releaseScript() {
		return "local lock = redis.call(\"GET\", KEYS[1])\n"
		+ "if not lock then return {0, \"not_locked\"} end\n"
		+ "local data = cjson.decode(lock)\n"
		+ "if data.owner ~= ARGV[1] or tostring(data.token) ~= ARGV[2] then return {0, \"invalid_owner\"} end\n"
		+ "if data.reentrancy and data.reentrancy > 1 then\n"
		+ " data.reentrancy = data.reentrancy - 1\n"
		+ " redis.call(\"SET\", KEYS[1], cjson.encode(data), \"PX\", ARGV[3])\n"
		+ " return {1, \"decremented\"}\n"
		+ "else\n"
		+ " redis.call(\"DEL\", KEYS[1])\n"
		+ " local waiter = redis.call(\"LPOP\", KEYS[2])\n"
		+ " if waiter then\n"
		+ " local w = cjson.decode(waiter)\n"
		+ " local fence = redis.call(\"INCR\", KEYS[3])\n"
		+ " local payload = cjson.encode({owner=w.owner, token=fence, expires=tonumber(redis.call(\"TIME\")[1])*1000 + w.ttl, reentrancy=1, requestId=w.requestId, mode=w.mode})\n"
		+ " redis.call(\"SET\", KEYS[1], payload, \"PX\", w.ttl)\n"
		+ " return {1, \"promoted\", tostring(fence), w.owner}\n"
		+ " end\n"
		+ " return {1, \"released\"}\n"
		+ "end\n";
	}
	
	
	// Renew script: KEYS[1]=lockKey ; ARGV: owner, token, ttlMs
	public String renewScript() {
		return "local lock = redis.call(\"GET\", KEYS[1])\n"
		+ "if not lock then return {0, \"not_locked\"} end\n"
		+ "local data = cjson.decode(lock)\n"
		+ "if data.owner ~= ARGV[1] or tostring(data.token) ~= ARGV[2] then return {0, \"invalid_owner\"} end\n"
		+ "local now = redis.call(\"TIME\")[1] * 1000\n"
		+ "data.expires = now + tonumber(ARGV[3])\n"
		+ "redis.call(\"SET\", KEYS[1], cjson.encode(data), \"PX\", ARGV[3])\n"
		+ "return {1, tostring(data.token), data.expires}\n";
	}
}
