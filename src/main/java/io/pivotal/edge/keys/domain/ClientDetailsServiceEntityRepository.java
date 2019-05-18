package io.pivotal.edge.keys.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientDetailsServiceEntityRepository extends JpaRepository<ClientDetailsServiceEntity, ClientDetailsServiceKey> {

    List<ClientDetailsServiceEntity> findAllByKeyClientId(String clientId);

    void deleteAllByKeyClientId(String clientId);

}
