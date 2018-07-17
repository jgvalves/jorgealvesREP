package com.multicert.mtchain.backoffice.Security;

import com.multicert.mtchain.backoffice.Entities.Admin.AdminRepository;
import com.multicert.mtchain.backoffice.Entities.Admin.Model.AdminEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider{


        @Autowired
        private AdminRepository adminRepository;


        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {

            if (authentication.getCredentials() instanceof X509Certificate) {
                X509Certificate cert = (X509Certificate) authentication.getCredentials();

                try {

                    for(AdminEntity admin: adminRepository.findAll()) {
                        if (admin.getCertificate().getPublicKey().equals(cert.getPublicKey())) {
                            List<GrantedAuthority> authorities = buildUserAuthority("ROLE_ADMIN");
                            return new UsernamePasswordAuthenticationToken(admin, cert, authorities);
                        }
                    }
                } catch (Exception e) {
                    return null;
                }
            }


            return null;

        }


        @Override
        public boolean supports(Class<?> arg0) {
            return true;
        }

        private List<GrantedAuthority> buildUserAuthority(String role) {
            Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();
            setAuths.add(new SimpleGrantedAuthority(role));
            return new ArrayList<>(setAuths);
        }

    }
