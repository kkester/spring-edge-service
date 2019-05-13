package io.pivotal.edge.keys;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("ClientKey")
public class ClientKey {

    @Id
    @JsonProperty("clientId")
    private String id;

    private String secretKey;

    @NotNull
    private ApplicationType applicationType;

    @NotNull
    private List<ClientService> services;

    private LocalDateTime createdOn;

    private LocalDateTime lastUpdated;

}
