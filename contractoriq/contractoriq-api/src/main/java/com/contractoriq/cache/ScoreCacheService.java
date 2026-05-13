package com.contractoriq.cache;

import com.contractoriq.dto.ContractorScoreDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class ScoreCacheService {

    private static final Logger log = LoggerFactory.getLogger(ScoreCacheService.class);
    private static final String KEY_PREFIX = "contractor:score:";

    private final RedisTemplate<String, ContractorScoreDTO> redisTemplate;

    @Value("${contractoriq.cache.score-ttl-seconds:3600}")
    private long ttlSeconds;

    public ScoreCacheService(RedisTemplate<String, ContractorScoreDTO> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public ContractorScoreDTO get(UUID contractorId) {
        try {
            return redisTemplate.opsForValue().get(key(contractorId));
        } catch (Exception ex) {
            log.warn("redis get failed for {}: {}", contractorId, ex.getMessage());
            return null;
        }
    }

    public void put(UUID contractorId, ContractorScoreDTO dto) {
        try {
            redisTemplate.opsForValue().set(key(contractorId), dto, Duration.ofSeconds(ttlSeconds));
        } catch (Exception ex) {
            log.warn("redis put failed for {}: {}", contractorId, ex.getMessage());
        }
    }

    public void invalidate(UUID contractorId) {
        try {
            redisTemplate.delete(key(contractorId));
        } catch (Exception ex) {
            log.warn("redis invalidate failed for {}: {}", contractorId, ex.getMessage());
        }
    }

    private static String key(UUID contractorId) {
        return KEY_PREFIX + contractorId;
    }
}
