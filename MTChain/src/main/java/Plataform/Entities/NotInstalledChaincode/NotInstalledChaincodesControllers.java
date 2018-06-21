package Plataform.Entities.NotInstalledChaincode;

import Plataform.Application.Blockchain;
import Plataform.Application.Logs;
import Plataform.Entities.Chaincodes.ChaincodeRepository;
import Plataform.Entities.Chaincodes.ChaincodesControllers;
import Plataform.Entities.Chaincodes.Model.Chaincodes;
import Plataform.Entities.NotInstalledChaincode.Model.NotInstalledChaincodes;
import Plataform.FireStarter;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class NotInstalledChaincodesControllers {

    @Autowired
    private NotChaincodeRepository notChaincodeService;



    @JsonView(DataTablesOutput.View.class)
    @GetMapping("/lists/notchaincode")
    private DataTablesOutput<NotInstalledChaincodes> getNotChaincodeList(@Valid DataTablesInput input){

        updateInstallDatabase();
        return notChaincodeService.findAll(input);
    }



    @GetMapping("/lists/chaincode/install/{chaincode}")
    private RedirectView installChaincode(@PathVariable("chaincode") String chaincodeName)
    {

        FireStarter.updateChaincodeVersion(chaincodeName);
        return new RedirectView("/admin/chaincode");
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
                    NotInstalledChaincodes c = new NotInstalledChaincodes();
                    boolean b = false;
                    c.setName(parseFileName(file.getName()));
                    for(Blockchain u: FireStarter.getChaincodeList()){
                        if(u.getName().equals(c.getName())){
                            b = true;
                        }
                    }
                    if(b){c.setInstalled(NotInstalledChaincodes.Installed.Yes);}
                    else{c.setInstalled(NotInstalledChaincodes.Installed.No);}


                    if(!dbContains(c, notChaincodeService)) {
                        notChaincodeService.save(c);
                    }
                    else{
                        for(NotInstalledChaincodes nic: notChaincodeService.findAll()){
                            if(nic.getName().equals(c)){
                                nic.setInstalled(NotInstalledChaincodes.Installed.Yes);
                                notChaincodeService.save(nic);
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
                            "<center><a href=\"/admin/lists/chaincode/install/" + nic.getName() +"\">[ install ]</a></center>"
            );
        }
    }

    private String parseFileName(String fileName){
        return FilenameUtils.getBaseName(fileName);
    }

    private boolean dbContains(NotInstalledChaincodes chaincode, NotChaincodeRepository list){
        for(NotInstalledChaincodes u: list.findAll()){
            if(u.getName().equals(chaincode.getName() )){
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
