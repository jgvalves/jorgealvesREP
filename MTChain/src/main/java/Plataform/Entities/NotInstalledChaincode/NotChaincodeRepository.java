package Plataform.Entities.NotInstalledChaincode;

import Plataform.Entities.Chaincodes.Model.Chaincodes;
import Plataform.Entities.NotInstalledChaincode.Model.NotInstalledChaincodes;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotChaincodeRepository extends DataTablesRepository<NotInstalledChaincodes, Long> {
}
