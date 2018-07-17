package com.multicert.mtchain.backoffice.Entities.Chaincodes;

import com.multicert.mtchain.backoffice.Entities.Chaincodes.Model.Chaincodes;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository("chaincodeRepository")
public interface ChaincodeRepository extends DataTablesRepository<Chaincodes, Long> {
}
