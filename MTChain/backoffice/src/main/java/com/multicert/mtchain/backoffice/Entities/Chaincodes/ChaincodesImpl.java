package com.multicert.mtchain.backoffice.Entities.Chaincodes;

import com.multicert.mtchain.backoffice.Application.Utils;
import com.multicert.mtchain.backoffice.Entities.Chaincodes.Model.Chaincodes;
import com.multicert.mtchain.core.Blockchain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("chaincodeImplementation")
public class ChaincodesImpl{

    @Autowired
    private ChaincodeRepository chaincodeRepository;

    @Transactional
    public void newChaincode(Blockchain bc){
        Chaincodes c = new Chaincodes();
        c.setName(bc.getName());
        c.setCurrentVersion(bc.getCurrentVersion());
        c.setBlockchain(bc);
        c.setUpgradingPattern(bc.getUpgradingPattern());
        c.setChaincodeVersions(Utils.formatList(bc.getVersions()));

        chaincodeRepository.save(c);
    }

    public Iterable<Chaincodes> getAllChaincodes(){
        return chaincodeRepository.findAll();
    }

    public Chaincodes findByBlockchain(Blockchain blockchain){
        for(Chaincodes c: chaincodeRepository.findAll()){
            if(c.getName().equals(blockchain.getName())){
                return c;
            }
        }
        return null;
    }

    @Transactional
    public void saveChaincodeChanges(Blockchain b){
        Chaincodes c = findByBlockchain(b);
        c.setCurrentVersion(b.getCurrentVersion());
        c.setName(b.getName());
        c.setBlockchain(b);
        c.setChaincodeVersions(Utils.formatList(b.getVersions()));
        chaincodeRepository.save(c);
    }
}
