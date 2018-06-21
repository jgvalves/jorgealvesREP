package Plataform.Entities.Chaincodes;

import Plataform.Entities.Users.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ChaincodeRepository")
public class ChaincodesImpl {

    @Autowired
    private ChaincodeRepository chaincodeRepository;
}
