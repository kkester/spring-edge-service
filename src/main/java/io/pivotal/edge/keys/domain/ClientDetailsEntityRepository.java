package io.pivotal.edge.keys.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientDetailsEntityRepository extends JpaRepository<ClientDetailsEntity,String> {

}
