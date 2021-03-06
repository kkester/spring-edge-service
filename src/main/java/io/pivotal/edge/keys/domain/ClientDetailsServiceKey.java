package io.pivotal.edge.keys.domain;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ClientDetailsServiceKey implements Serializable {

    private String clientId;
    private String serviceId;

}
