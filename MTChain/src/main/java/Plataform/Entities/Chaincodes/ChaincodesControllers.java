package Plataform.Entities.Chaincodes;

import Plataform.Application.Blockchain;
import Plataform.Application.Logs;
import Plataform.Entities.Chaincodes.Model.Chaincodes;
import Plataform.Entities.NotInstalledChaincode.Model.NotInstalledChaincodes;
import Plataform.Entities.NotInstalledChaincode.NotChaincodeRepository;
import Plataform.Entities.Users.Model.Users;
import Plataform.Entities.Users.UsersRepository;
import Plataform.FireStarter;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.jws.WebParam;
import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class ChaincodesControllers {

    @Autowired
    private ChaincodeRepository chaincodeService;

    @JsonView(DataTablesOutput.View.class)
    @GetMapping("/lists/chaincode")
    private DataTablesOutput<Chaincodes> getChaincodeList(@Valid DataTablesInput input){

        updateDatabase();
        return chaincodeService.findAll(input);
    }


    @GetMapping("/chaincode/{chaincode}")
    private ModelAndView editChaincode(@PathVariable("chaincode") String chaincodeName) {

        ModelAndView mav = new ModelAndView("editchaincode");

        Blockchain blockchain = FireStarter.getChaincode(chaincodeName);

        mav.addObject("name", blockchain.getName());
        mav.addObject("version", blockchain.getCurrentVersion());
        mav.addObject("nextVersion", blockchain.getCurrentVersion() + 0.1);
        mav.addObject("downVersions", getVersions(chaincodeName));

        return mav;
    }

    @PostMapping("/lists/chaincode/upgrade")
    private RedirectView upgradeChaincode(@RequestParam("ccname") String chaincodeName) {

        try {
            Blockchain blockchain = FireStarter.getChaincode(chaincodeName);
            FireStarter.updateChaincodeVersion(chaincodeName);

            return new RedirectView("/admin/chaincode");
        }
        catch(Exception e){
            return new RedirectView("/error");
        }
    }

    @PostMapping("/lists/chaincode/downgrade")
    private RedirectView downgradeChaincode(@RequestParam("chaincode") String chaincodeName,
                                            @RequestParam("newVersion") String newVersion)
    {
        try {
            if (newVersion.equals("previous")) {
                FireStarter.downgradeChaincodeVersion(chaincodeName);
            } else {
                FireStarter.downgradeChaincodeVersion(chaincodeName, Double.parseDouble(newVersion));
            }

            return new RedirectView("/admin/chaincode");
        }
        catch (Exception e){
            return new RedirectView("/error");
        }
    }



    private String formatList(Iterable list){
        String str="";
        int i = 0;
        for(Object o: list){
            if(i==0){
                str= str + "{" + o;
            }
            else{
                str= str + ", " + o;
            }
            i++;
        }

        str = str + "}";
        return str;
    }

    private void updateDatabase(){
        List<Blockchain> chainList = FireStarter.getChaincodeList();
        chaincodeService.deleteAll();
        for(Blockchain b: chainList){

            Chaincodes c = new Chaincodes(b.getName(), ""+b.getCurrentVersion(), formatList(b.getVersions()), null);
            chaincodeService.save(c);
        }

        for(Chaincodes u: chaincodeService.findAll()){
            u.setEdit(
                    "" +
                            "<center>" +
                                "<a href=\"/admin/chaincode/" + u.getName() +"\">[ upgrade / downgrade ]</a></center>"
            );
        }
    }



    private String parseFileName(String fileName){
        return FilenameUtils.getBaseName(fileName);
    }

    private List<Version> getVersions(String chaincode){
        Blockchain blockchain = FireStarter.getChaincode(chaincode);
        List<Version> newList = new ArrayList<>();
        for(Double d: blockchain.getVersions()){
            if(!d.equals(blockchain.getCurrentVersion())) {
                newList.add(new Version(d, d + ""));
            }
        }
        return newList;
    }

}

class Version{

    private Double id;
    private String name;

    public Version(){}

    public Version(Double id, String name){
        this.id = id;
        this.name = name;
    }

    public Double getId() {
        return id;
    }

    public void setId(Double id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
