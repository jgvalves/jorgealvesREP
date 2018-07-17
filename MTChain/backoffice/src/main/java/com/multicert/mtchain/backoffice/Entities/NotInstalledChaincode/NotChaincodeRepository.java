package com.multicert.mtchain.backoffice.Entities.NotInstalledChaincode;

import com.multicert.mtchain.backoffice.Entities.NotInstalledChaincode.Model.NotInstalledChaincodes;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotChaincodeRepository extends DataTablesRepository<NotInstalledChaincodes, Long> {
}
