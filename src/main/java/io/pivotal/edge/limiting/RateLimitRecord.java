package io.pivotal.edge.limiting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("RateLimitRecord")
public class RateLimitRecord {

    private String id;
    private List<LocalDateTime> requestTimes;

}
