package io.pivotal.edge.keys.web;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ClientService {

    @NotNull
    private String id;
    private String path;

}
