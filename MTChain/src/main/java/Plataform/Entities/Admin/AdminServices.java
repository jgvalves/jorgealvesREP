package Plataform.Entities.Admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("adminServices")
public class AdminServices {

    @Autowired
    private AdminRepository adminRepository;


}
