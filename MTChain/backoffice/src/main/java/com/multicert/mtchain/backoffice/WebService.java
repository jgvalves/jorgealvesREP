package com.multicert.mtchain.backoffice;

import com.multicert.mtchain.backoffice.Entities.Admin.AdminRepository;
import com.multicert.mtchain.backoffice.Entities.App.AppService;
import com.multicert.mtchain.backoffice.Entities.Chaincodes.ChaincodeRepository;
import com.multicert.mtchain.backoffice.Entities.Chaincodes.ChaincodesImpl;
import com.multicert.mtchain.backoffice.Entities.Chaincodes.Model.Chaincodes;
import com.multicert.mtchain.core.Blockchain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
public class WebService extends SpringBootServletInitializer {



    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebService.class);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext wb =
                SpringApplication.run(WebService.class, args);
        EventHolderBean bean = wb.getBean(EventHolderBean.class);
    }
}


@Component
class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    private EventHolderBean eventHolderBean;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ChaincodeRepository chaincodeRepository;

    @Autowired
    private ChaincodesImpl chaincodeImplementation;

    @Autowired
    private AppService appService;

    @Autowired
    public void setEventHolderBean(EventHolderBean eventHolderBean) {
        this.eventHolderBean = eventHolderBean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        FireStarter.init();

        try {
            /*CertificateFactory fact = CertificateFactory.getInstance("X.509");
            //FileInputStream is = new FileInputStream(new File("/home/jorge/Desktop/jorgecert/jorge_pub.pem"));
            FileInputStream is = new FileInputStream(new File("/home/jorge/Documents/new/client.crt"));
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            AdminEntity admin = new AdminEntity("Jorge Alves" , cer);
            adminRepository.save(admin);

            */
            FireStarter.populateChaincodeInstatiationsList(getChaincodes());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        eventHolderBean.setEventFired(true);
    }

    public List<Blockchain> getChaincodes(){

        List<Blockchain> blockchains = new ArrayList<>();

        try {
            for (Chaincodes c : chaincodeRepository.findAll()) {
                blockchains.add(c.getBlockchain());
            }
        }
        catch(NullPointerException e){

        }

        return blockchains;
    }
}

@Component
class EventHolderBean {
    private Boolean eventFired = false;

    public Boolean getEventFired() {
        return eventFired;
    }

    public void setEventFired(Boolean eventFired) {
        this.eventFired = eventFired;
    }
}



