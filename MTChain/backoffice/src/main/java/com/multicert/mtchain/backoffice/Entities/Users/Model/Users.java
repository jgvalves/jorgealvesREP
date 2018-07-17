package com.multicert.mtchain.backoffice.Entities.Users.Model;

import com.fasterxml.jackson.annotation.JsonView;
import com.multicert.mtchain.backoffice.Cryptography.Certification;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import sun.misc.BASE64Decoder;

import javax.persistence.*;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;


@Entity
@Table(name="Users")
public class Users implements Serializable {

    public enum State {
        UP,
        DOWN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userid")
    private Long userid;


    @JsonView(DataTablesOutput.View.class)
    @Column(name="name")
    private String name;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="app")
    private String app;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="certdate")
    private String certdate;

    @JsonView(DataTablesOutput.View.class)
    @Enumerated(EnumType.STRING)
    @Column(name="state")
    private State state;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="lastlogin")
    private String lastlogin;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="edit")
    @Null
    private String edit;

    @Column(name="certificate", length = 4096)
    private String certificate;

    @Column(name="chaincode")
    private String chaincode;

    public Users() {
        super();
    }

    public Users(String name, String app, State state, String edit, String certificate) {
        super();
        this.name = name;
        this.app = app;
        this.lastlogin = "-";
        this.state = state;
        this.edit = edit;
        this.certificate = certificate;
        //this.databaseCertificate = Certification.convertX509toBase64(certificate);
    }


    public void setLastlogin(String lastlogin) {
        this.lastlogin = lastlogin;
    }

    public String getEdit() {
        return edit;
    }

    public void setEdit(String edit) {
        this.edit = edit;
    }

    public Long getUserid() {
        return userid;
    }

    public String getName() {
        return name;
    }

    public String getApp() {
        return app;
    }

    public String getCertdate() {
        return certdate;
    }

    public String getLastlogin() {
        return lastlogin;
    }

    public void setUserid(Long id) {
        this.userid = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setLastLogin(String lastLogin) {
        this.lastlogin = lastLogin;
    }

    public State getState() {
        return state;
    }

    public X509Certificate getCertificate() {
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            return Certification.certificateFromByteStream(decoder.decodeBuffer(certificate));
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void setCertificate(X509Certificate certificate) {
            this.certificate = Certification.convertX509toBase64(certificate);
            //this.databaseCertificate = Certification.convertX509toBase64(certificate);
            this.certdate = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(certificate.getNotAfter());
    }

    public String getChaincode() {
        return chaincode;
    }

    public void setChaincode(String chaincode) {
        this.chaincode = chaincode;
    }


}
