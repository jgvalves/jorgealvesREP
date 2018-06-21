package Plataform.Entities.NotInstalledChaincode.Model;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import javax.persistence.*;

@Entity
@Table(name="NotInstalledChaincodes")
public class NotInstalledChaincodes {

    public enum Installed {
        Yes,
        No
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userid")
    private Integer userid;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="name")
    private String name;

    @JsonView(DataTablesOutput.View.class)
    @Enumerated(EnumType.STRING)
    @Column(name="installed")
    private Installed installed;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="install")
    private String install;

    public NotInstalledChaincodes() {
        super();
    }

    public NotInstalledChaincodes(String name, Installed installed, String install){
        super();
        this.name = name;
        this.installed = installed;
        this.install = install;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Installed getInstalled() {
        return installed;
    }

    public void setInstalled(Installed installed) {
        this.installed = installed;
    }

    public String getInstall() {
        return install;
    }

    public void setInstall(String install) {
        this.install = install;
    }
}
