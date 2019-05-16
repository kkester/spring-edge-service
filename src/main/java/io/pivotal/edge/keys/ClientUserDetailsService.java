package io.pivotal.edge.keys;

import io.pivotal.edge.keys.domain.ClientDetailsEntity;
import io.pivotal.edge.keys.domain.ClientDetailsEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClientUserDetailsService implements UserDetailsService {

    @Autowired
    private ClientDetailsEntityRepository clientDetailsRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<ClientDetailsEntity> clientDetailsEntityOptional = clientDetailsRepository.findById(username);

        if(!clientDetailsEntityOptional.isPresent()) {
            throw new UsernameNotFoundException(String.format("The username %s doesn't exist", username));
        }

        ClientDetailsEntity clientDetailsEntity = clientDetailsEntityOptional.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        return  new User(clientDetailsEntity.getClientId(), clientDetailsEntity.getClientSecret(), authorities);
    }

}
