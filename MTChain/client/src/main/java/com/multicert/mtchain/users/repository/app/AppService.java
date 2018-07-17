package com.multicert.mtchain.users.repository.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service("appService")
public class AppService {

    @Autowired
    private AppRepository appRepository;

    public List<String> getAppNameList(){

        List<String> list = new ArrayList<>();

        for(App a: appRepository.findAll()){
            list.add(a.getName());
        }
        return list;
    }

    public List<String> getAppNameList(String excludedName, List<String> list){

        for(App a: appRepository.findAll()){
            if(!a.getName().equals(excludedName)) {
                list.add(a.getName());
            }
        }
        return list;

    }

    @Transactional
    public void addNewApp(String name, String chaincode){
        App app = new App(name, chaincode);
        appRepository.save(app);
    }


}
