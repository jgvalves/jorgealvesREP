package com.multicert.mtchain.users.repository.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/")
public class EntityControllers {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UsersServices usersServices;


    //TODO talvez seja possivel alterar-se
    /*
    @GetMapping("/lists/users/{id}")
    private ModelAndView  getUserById(@PathVariable("id") int id){

        Users user = new Users();

        for(Users u: usersRepository.findAll()){
            if(u.getUserid() == id){
                user = u;
            }
        }

        try {
            user.getUserid();
            ModelAndView mav = new ModelAndView("userform");
            mav.addObject("username", user.getName());
            mav.addObject("id", user.getUserid());
            mav.addObject("app", user.getApp());
            mav.addObject("newUser", false);
            return mav;
        }
        catch(NullPointerException e) {
            e.printStackTrace();
            return new ModelAndView("error");
        }
    }


    @PostMapping("/lists/users/edit")
    private RedirectView editUser(@RequestParam("name") String name,
                                  @RequestParam("application") String app,
                                  @RequestParam("id") String id,
                                  @RequestParam("certificate") MultipartFile certificate,
                                  @RequestParam("newUser") boolean newUser){


        if(!newUser) {

            int ID = Integer.parseInt(id);
            Users user = new Users();

            for (Users u : usersRepository.findAll()) {
                if (u.getUserid() == ID) {
                    user = u;
                }
            }

            try {



                user.setName(name);
                user.setApp(app);

                if(!certificate.isEmpty()){
                    user.setCertificate(Certification.certificateFromByteStream(certificate.getBytes()));
                }

                usersRepository.save(user);
                return new RedirectView("/mtchain/admin/index");
            } catch (Exception e) {
                return new RedirectView("/error");
            }
        }
        else{

            try {
                X509Certificate newCert = Certification.certificateFromByteStream(certificate.getBytes());
                usersServices.addNewUser(name, app, newCert);
                return new RedirectView("/mtchain/admin/index");
            }
            catch (Exception e){
                e.printStackTrace();
                return new RedirectView("/error");
            }



        }
    }
    */

}
