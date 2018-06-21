package Plataform.Entities.NotInstalledChaincode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("NotChaincodeRepository")
public class NotChaincodesImpl {

    @Autowired
    private NotChaincodeRepository notChaincodeRepository;
}
