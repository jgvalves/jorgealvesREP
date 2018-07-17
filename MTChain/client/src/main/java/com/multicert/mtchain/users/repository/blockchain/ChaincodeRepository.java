package com.multicert.mtchain.users.repository.blockchain;

import com.multicert.mtchain.users.repository.blockchain.Model.Chaincodes;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository("chaincodeRepository")
public interface ChaincodeRepository extends DataTablesRepository<Chaincodes, Long> {

}
