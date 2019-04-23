package io.pivotal.edge.limiting;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RateLimitingService {

    private Map<String, RateLimitRecord> clientKeyRecords;

}
