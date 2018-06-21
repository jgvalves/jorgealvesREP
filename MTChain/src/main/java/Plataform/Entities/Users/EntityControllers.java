package Plataform.Entities.Users;


import Plataform.Cryptography.Certification;
import Plataform.Entities.Users.Model.Users;
import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.jpa.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.io.File;
import java.security.cert.X509Certificate;

@RestController
@RequestMapping("/admin")
public class EntityControllers {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UsersServices usersServices;


    @JsonView(DataTablesOutput.View.class)
    @GetMapping("/lists/users")
    private DataTablesOutput<Users> getUsersList(@Valid DataTablesInput input){

        for(Users u: usersRepository.findAll()){
            u.setEdit(
                    "" +
                            "<center><a href=\"/admin/lists/users/" + u.getUserid() + " \">[ edit ]</a> " +
                            "<a href=\"/admin/lists/users/delete/" + u.getUserid() + " \">[ remove ]</a></center>"
            );
        }

        return usersRepository.findAll(input);
    }


    @GetMapping("/lists/newuser")
    private ModelAndView getNewUserForm(){
        ModelAndView mav = new ModelAndView("userform");
        mav.addObject("username", "");
        mav.addObject("id", "");
        mav.addObject("app", "");
        mav.addObject("newUser", true);
        return mav;
    }

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
                return new RedirectView("/admin/index");
            } catch (Exception e) {
                return new RedirectView("/error");
            }
        }
        else{

            try {
                X509Certificate newCert = Certification.certificateFromByteStream(certificate.getBytes());
                usersServices.addNewUser(name, app, newCert);
                return new RedirectView("/admin/index");
            }
            catch (Exception e){
                e.printStackTrace();
                return new RedirectView("/error");
            }



        }


    }

    @GetMapping("/lists/users/delete/{id}")
    private RedirectView deleteUser(@PathVariable("id") int id){

        Users user = new Users();

        for(Users u: usersRepository.findAll()){
            if(u.getUserid() == id){
                user = u;
            }
        }

        try {
            usersRepository.delete(user);
            return new RedirectView("/admin/index");
        }
        catch(NullPointerException e){
            return new RedirectView("/error");
        }
    }



    //@GetMapping("/lists/chaincode")
    //private



}
