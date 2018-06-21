package Plataform.Entities.Chaincodes.Model;


import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import javax.persistence.*;
import javax.validation.constraints.Null;

@Entity
@Table(name="Chaincodes")
public class Chaincodes {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userid")
    private Integer userid;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="name")
    private String name;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="currentversion")
    private String currentversion;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="avalversions")
    private String avalversions;

    @JsonView(DataTablesOutput.View.class)
    @Column(name="edit")
    @Null
    private String edit;

    public Chaincodes() {
        super();
    }

    public Chaincodes(String name, String currentversion, String avalversions, String edit){
        super();
        this.name = name;
        this.currentversion = currentversion;
        this.avalversions = avalversions;
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

    public String getCurrentversion() {
        return currentversion;
    }

    public void setCurrentversion(String currentversion) {
        this.currentversion = currentversion;
    }

    public String getAvalversions() {
        return avalversions;
    }

    public void setAvalversions(String avalversions) {
        this.avalversions = avalversions;
    }

    public String getEdit() {
        return edit;
    }

    public void setEdit(String edit) {
        this.edit = edit;
    }
}
