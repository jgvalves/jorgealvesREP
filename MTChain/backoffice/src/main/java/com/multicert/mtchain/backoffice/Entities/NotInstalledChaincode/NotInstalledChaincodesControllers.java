package com.multicert.mtchain.backoffice.Entities.NotInstalledChaincode;

import com.fasterxml.jackson.annotation.JsonView;
import com.multicert.mtchain.backoffice.Entities.Chaincodes.ChaincodeRepository;
import com.multicert.mtchain.backoffice.Entities.Chaincodes.ChaincodesImpl;
import com.multicert.mtchain.backoffice.Entities.Chaincodes.Model.Chaincodes;
import com.multicert.mtchain.backoffice.Entities.NotInstalledChaincode.Model.NotInstalledChaincodes;
import com.multicert.mtchain.backoffice.FireStarter;
import com.multicert.mtchain.core.Blockchain;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/admin")
public class NotInstalledChaincodesControllers {

    @Autowired
    private NotChaincodeRepository notChaincodeService;

    @Autowired
    private ChaincodesImpl chaincodeImplementation;

    @JsonView(DataTablesOutput.View.class)
    @GetMapping("/lists/notchaincode")
    private DataTablesOutput<NotInstalledChaincodes> getNotChaincodeList(@Valid DataTablesInput input){

        updateInstallDatabase();
        return notChaincodeService.findAll(input);
    }



    @GetMapping("/lists/chaincode/install/{chaincode}")
    private RedirectView installChaincode(@PathVariable("chaincode") String chaincodeName)
    {
        Blockchain blockchain = FireStarter.getChaincode(chaincodeName);
        FireStarter.updateChaincodeVersion(blockchain);
        chaincodeImplementation.newChaincode(blockchain);
        return new RedirectView("/mtchain/admin/chaincode");
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


    private void updateInstallDatabase(){

        Path path = Paths.get("src/chaincode/");
        

        try {
            File folder = new File("src/chaincode/");
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if(!dbContains(parseFileName(file.getName()), notChaincodeService)) {
                        NotInstalledChaincodes c = new NotInstalledChaincodes();
                        boolean b = false;
                        c.setName(parseFileName(file.getName()));
                        try {
                            for (Blockchain u : FireStarter.getChaincodeList()) {
                                if (u.getName().equals(c.getName())) {
                                    b = true;
                                }
                            }
                        }
                        catch(Exception e){}
                        if(b){c.setInstalled(NotInstalledChaincodes.Installed.Yes);}
                        else{c.setInstalled(NotInstalledChaincodes.Installed.No);}
                        notChaincodeService.save(c);
                    }
                    else{
                        for(NotInstalledChaincodes nic: notChaincodeService.findAll()){
                            for(Blockchain u: FireStarter.getChaincodeList()){
                                if(u.getName().equals(parseFileName(file.getName()))){
                                    nic.setInstalled(NotInstalledChaincodes.Installed.Yes);
                                    notChaincodeService.save(nic);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        for(NotInstalledChaincodes nic: notChaincodeService.findAll()){
            nic.setInstall(
                    "" +
                            "<center><a href=\"/mtchain/admin/lists/chaincode/install/" + nic.getName() +"\">[ install ]</a></center>"
            );
        }
    }

    private String parseFileName(String fileName){
        return FilenameUtils.getBaseName(fileName);
    }

    private boolean dbContains(String chaincode, NotChaincodeRepository list){
        for(NotInstalledChaincodes u: list.findAll()){
            if(u.getName().equals(chaincode)){
                return true;
            }
        }
        return false;
    }

    private boolean dbContains(Chaincodes chaincode, ChaincodeRepository list){
        for(Chaincodes u: list.findAll()){
            if(u.equals(chaincode)){
                return true;
            }
        }
        return false;
    }
}
