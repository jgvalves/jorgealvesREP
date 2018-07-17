package com.multicert.mtchain.backoffice.Entities.Users;

import com.multicert.mtchain.backoffice.Entities.Users.Model.Users;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends DataTablesRepository<Users, Long> {

}