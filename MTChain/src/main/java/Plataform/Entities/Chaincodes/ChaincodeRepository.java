package Plataform.Entities.Chaincodes;

import Plataform.Entities.Chaincodes.Model.Chaincodes;
import Plataform.Entities.Users.Model.Users;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChaincodeRepository extends DataTablesRepository<Chaincodes, Long> {
}
