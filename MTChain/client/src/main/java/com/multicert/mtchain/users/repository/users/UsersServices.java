package com.multicert.mtchain.users.repository.users;

import com.multicert.mtchain.users.repository.users.Model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service("usersServices")
public class UsersServices {


    @Autowired
    private UsersRepository usersRepository;


    @Transactional
    public void registerUserLogin(Users user) throws Exception {
        user.setState(Users.State.UP);
        user.setLastLogin(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
        usersRepository.save(user);
    }




}
