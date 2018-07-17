package com.multicert.mtchain.backoffice.Entities.Chaincodes;

import com.fasterxml.jackson.annotation.JsonView;
import com.multicert.mtchain.backoffice.Entities.Chaincodes.Model.Chaincodes;
import com.multicert.mtchain.backoffice.FireStarter;
import com.multicert.mtchain.core.Blockchain;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class ChaincodesControllers {

    @Autowired
    private ChaincodeRepository chaincodeRepository;

    @Autowired
    private ChaincodesImpl chaincodeImplementation;

    @JsonView(DataTablesOutput.View.class)
    @GetMapping("/lists/chaincode")
    private DataTablesOutput<Chaincodes> getChaincodeList(@Valid DataTablesInput input){

        updateDatabase();
        return chaincodeRepository.findAll(input);
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
            FireStarter.updateChaincodeVersion(blockchain);
            chaincodeImplementation.saveChaincodeChanges(blockchain);
            return new RedirectView("/mtchain/admin/chaincode");
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
            Blockchain blockchain = FireStarter.getChaincode(chaincodeName);
            if (newVersion.equals("previous")) {
                FireStarter.downgradeChaincodeVersion(blockchain);
            } else {
                FireStarter.downgradeChaincodeVersion(blockchain, Double.parseDouble(newVersion));

            }
            chaincodeImplementation.saveChaincodeChanges(blockchain);

            return new RedirectView("/mtchain/admin/chaincode");
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
        /*List<Blockchain> chainList = FireStarter.getChaincodeList();
        for(Blockchain b: chainList){

            Chaincodes c = new Chaincodes();
            c.setName(b.getName());
            c.setCurrentVersion(b.getCurrentVersion());
            c.setChaincodeVersions(formatList(getVersions(b.getName())));
            chaincodeRepository.save(c);
        }*/

        for(Chaincodes u: chaincodeRepository.findAll()){
            u.setEdit(
                    "" +
                            "<center>" +
                                "<a href=\"/mtchain/admin/chaincode/" + u.getName() +"\">[ upgrade / downgrade ]</a></center>"
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
