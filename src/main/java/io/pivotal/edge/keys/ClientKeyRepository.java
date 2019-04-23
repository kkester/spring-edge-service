package io.pivotal.edge.keys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientKeyRepository extends JpaRepository<ClientKey,String> {

}
