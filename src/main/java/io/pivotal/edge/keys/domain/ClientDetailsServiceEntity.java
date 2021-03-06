package io.pivotal.edge.keys.domain;

import lombok.Data;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "oauth_client_details_services")
public class ClientDetailsServiceEntity {

    @EmbeddedId
    private ClientDetailsServiceKey key;

    private String path;

}
