package com.multicert.mtchain.backoffice.Entities.Users;

import com.multicert.mtchain.backoffice.Entities.Users.Model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.cert.X509Certificate;

@Service("usersServices")

public class UsersServices {


    @Autowired
    private UsersRepository usersRepository;


    @Transactional
    public void addNewUser(String name, String app, X509Certificate certificate) throws Exception {
        Users user = new Users();
        user.setName(name);
        user.setApp(app);
        user.setCertificate(certificate);
        user.setState(Users.State.UP);
        user.setLastLogin("-");

        usersRepository.save(user);
    }




}
