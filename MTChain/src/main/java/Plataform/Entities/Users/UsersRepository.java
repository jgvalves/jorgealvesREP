package Plataform.Entities.Users;

import Plataform.Entities.Users.Model.Users;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends DataTablesRepository<Users, Long> {

}