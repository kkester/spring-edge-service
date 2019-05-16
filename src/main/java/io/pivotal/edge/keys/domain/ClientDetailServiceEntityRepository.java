package io.pivotal.edge.keys.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientDetailServiceEntityRepository extends JpaRepository<ClientDetailServiceEntity,ClientDetailServiceKey> {

    List<ClientDetailServiceEntity> findAllByKeyClientId(String clientId);

    void deleteAllByKeyClientId(String clientId);

}
