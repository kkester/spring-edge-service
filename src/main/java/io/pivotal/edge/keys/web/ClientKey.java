package io.pivotal.edge.keys.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientKey {

    private String clientId;

    private String secretKey;

    @NotNull
    private ApplicationType applicationType;

    @NotNull
    private List<ClientService> services;

    private LocalDateTime createdOn;

    private LocalDateTime lastUpdated;

}
