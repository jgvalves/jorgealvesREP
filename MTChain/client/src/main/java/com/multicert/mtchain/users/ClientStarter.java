package com.multicert.mtchain.users;

import com.multicert.mtchain.users.repository.blockchain.ChaincodeRepository;
import com.multicert.mtchain.users.service.Initializator;
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


@SpringBootApplication
@EnableJpaRepositories(repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class)
public class ClientStarter extends SpringBootServletInitializer {



    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ClientStarter.class);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext wb =
                SpringApplication.run(ClientStarter.class, args);
        EventHolderBean bean = wb.getBean(EventHolderBean.class);
    }
}

    @Component
    class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

        @Autowired
        private ChaincodeRepository chaincodeRepository;

        private EventHolderBean eventHolderBean;

        @Autowired
        public void setEventHolderBean(EventHolderBean eventHolderBean) {
            this.eventHolderBean = eventHolderBean;
        }

        @Override
        public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
            Initializator.init();
            //Initializator.populateChaincodeInstatiationsList(getChaincodes());
            eventHolderBean.setEventFired(true);
        }

        /*public List<Blockchain> getChaincodes(){

            List<Blockchain> blockchains = new ArrayList<>();

            try {
                for (BlockchainInfo c : blockchainInfoRepository.findAll()) {
                    blockchains.add(c.getBlockchain());
                }
            }
            catch(NullPointerException e){

            }

            return blockchains;
        }*/
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

