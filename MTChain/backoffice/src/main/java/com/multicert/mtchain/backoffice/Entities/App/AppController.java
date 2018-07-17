package com.multicert.mtchain.backoffice.Entities.App;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AppController {


    @Autowired
    private AppService appService;

    @Autowired
    private AppRepository appRepository;

    @JsonView(DataTablesOutput.View.class)
    @GetMapping("/lists/applications")
    public DataTablesOutput<App> listApplications(@Valid DataTablesInput input){
        return appRepository.findAll(input);
    }

    @PostMapping("/lists/application/new")
    public RedirectView newApplication(@RequestParam("name") String name,
                                       @RequestParam("chaincode") String chaincode
    ){
        try {
            appService.addNewApp(name, chaincode);
            return new RedirectView("/mtchain/admin/index");
        }
        catch(Exception e){
            return new RedirectView("/error");
        }
    }

    //TODO LOAD NEW APP FORM
    @GetMapping("/lists/applicationsform")
    public ModelAndView listApplications(){
        return new ModelAndView("applicationform");
    }

    @GetMapping("/lists/applications/remove/{id}")
    private RedirectView deleteApp(@PathVariable("id") int id){

        App app = new App();

        for(App u: appRepository.findAll()){
            if(u.getAdminid() == id){
                app = u;
            }
        }

        try {
            appRepository.delete(app);
            return new RedirectView("/mtchain/admin/index");
        }
        catch(NullPointerException e){
            return new RedirectView("/error");
        }
    }



        //"<a href=\"/mtchain/admin/lists/users/delete/" + u.getUserid() + " \">[ remove ]</a></center>"
}
