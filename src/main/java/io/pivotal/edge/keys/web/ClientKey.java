package io.pivotal.edge.keys.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientKey {

    private String clientId;

    private String secretKey;

    @NotNull
    private ApplicationType applicationType;

    @NotNull
    private List<ClientService> services;

    private Integer accessTokenValidity;

    private Integer refreshTokenValidity;

}
