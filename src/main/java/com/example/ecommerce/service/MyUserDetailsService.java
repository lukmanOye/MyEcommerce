package com.example.ecommerce.service;

import com.example.ecommerce.model.User;
import com.example.ecommerce.model.UserPrincipal;
import com.example.ecommerce.repository.User_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private User_Repository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email);

        if (user== null){

       throw new IllegalArgumentException("User Not Found" + email);
    }
        return new UserPrincipal(user);
   }
}
