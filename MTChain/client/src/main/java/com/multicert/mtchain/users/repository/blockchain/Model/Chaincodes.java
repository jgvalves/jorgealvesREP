package com.multicert.mtchain.users.repository.blockchain.Model;


import com.multicert.mtchain.core.Blockchain;

import javax.persistence.*;
import javax.validation.constraints.Null;
import java.io.Serializable;

@Entity
@Table(name="BlockchainInfo")
public class Chaincodes implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="name")
    private String name;

    @Column(name="currentVersion")
    private double currentVersion;

    @Column(name="upgradingPattern")
    private double upgradingPattern;


    @Column(name="chaincodeVersions")
    private String chaincodeVersions;

    @Column(name="blockchain")
    private Blockchain blockchain;

    @Column(name="edit")
    @Null
    private String edit;


    public Chaincodes (){super();}

    public Chaincodes(String name, double currentVersion, double upgradingPattern, String chaincodeVersions, Blockchain blockchain) {
        super();
        this.name = name;
        this.currentVersion = currentVersion;
        this.upgradingPattern = upgradingPattern;
        this.chaincodeVersions = chaincodeVersions;
        this.blockchain = blockchain;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(double currentVersion) {
        this.currentVersion = currentVersion;
    }

    public double getUpgradingPattern() {
        return upgradingPattern;
    }

    public void setUpgradingPattern(double upgradingPattern) {
        this.upgradingPattern = upgradingPattern;
    }


    public String getChaincodeVersions() {
        return chaincodeVersions;
    }

    public void setChaincodeVersions(String chaincodeVersions) {
        this.chaincodeVersions = chaincodeVersions;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }
}
