package com.multicert.mtchain.users.repository.users;

import com.multicert.mtchain.users.repository.users.Model.Users;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository("usersRepository")
public interface UsersRepository extends DataTablesRepository<Users, Long> {
    Users findByuserid(Long id);
}