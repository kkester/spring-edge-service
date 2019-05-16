package io.pivotal.edge.keys.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "oauth_client_details")
public class ClientDetailsEntity {

    @Id
    private String clientId;

    private String clientSecret;

    private String scope;

    private String authorizedGrantTypes;

    private String authorities;

    private Integer accessTokenValidity;

    private Integer refreshTokenValidity;

}
