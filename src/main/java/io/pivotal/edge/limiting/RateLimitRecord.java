package io.pivotal.edge.limiting;

import lombok.Data;

@Data
public class RateLimitRecord {

    private String key;
    private int countSeconds;

}
