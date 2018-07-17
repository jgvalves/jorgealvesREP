package com.multicert.mtchain.users.repository.app;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="Applications")
public class App implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="adminId")
    private Long adminid;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="name")
    private String name;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="chaincode")
    private String chaincode;


    public App(){
        super();
    }

    public App(String name, String chaincode) {
        super();
        this.name = name;
        this.chaincode = chaincode;
    }

    public Long getAdminid() {
        return adminid;
    }

    public void setAdminid(Long adminid) {
        this.adminid = adminid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChaincode() {
        return chaincode;
    }

    public void setChaincode(String chaincode) {
        this.chaincode = chaincode;
    }
}
