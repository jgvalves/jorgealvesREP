package com.multicert.mtchain.backoffice.Entities.Admin.Model;


import javax.persistence.*;
import java.io.Serializable;
import java.security.cert.X509Certificate;

@Entity
@Table(name="AdminEntity")
public class AdminEntity implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="adminId")
    private Long adminid;

    @Column(name="name")
    private String name;

    @Column(name="certificate", length = 4096)
    private X509Certificate certificate;

    public AdminEntity(){
        super();
    }

    public AdminEntity(String name, X509Certificate certificate) {
        super();
        this.name = name;
        this.certificate = certificate;
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

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }
}
