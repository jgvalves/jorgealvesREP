package Plataform;

import Plataform.Application.Logs;
import Plataform.Entities.Admin.AdminRepository;
import Plataform.Entities.Admin.Model.AdminEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


@SpringBootApplication
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
public class WebService extends SpringBootServletInitializer {



    /*@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebService.class);
    }*/

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
    public void setEventHolderBean(EventHolderBean eventHolderBean) {
        this.eventHolderBean = eventHolderBean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        FireStarter.init();

        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            FileInputStream is = new FileInputStream(new File("/home/jorge/Desktop/jorgecert/jorge_pub.pem"));
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            AdminEntity admin = new AdminEntity("Jorge Alves" , cer);
            adminRepository.save(admin);

            Logs.write("Admin added: " + admin.getName());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        eventHolderBean.setEventFired(true);
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



