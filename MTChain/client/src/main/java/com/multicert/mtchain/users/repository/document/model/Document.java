package com.multicert.mtchain.users.repository.document.model;

import com.multicert.mtchain.users.repository.users.Model.Users;

import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Table(name="Documents")
public class Document implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="description")
    private String description;

    @Column(name="emitter")
    private Users emiiter;

    @Column(name="hash")
    private String hash;

    @Column(name="signedhash", length=1024)
    private String signedhash;


    @Column(name="signers")
    private Signers signers;

    @Column(name="numsigners")
    private int numsigners;

    @Column(name="currentsigner")
    private Users currentsigner;

    @Column(name="nextsigner")
    private int nextsigner;

    @Column(name="state")
    private String state;

    @Column(name="uploadtime")
    private String uploadtime;

    public Document(){
        super();
    }

    public Document(String name, String description, Users emitter, String hash, Signers signers){
        super();
        this.name = name;
        this.description = description;
        this.emiiter = emitter;
        this.hash = hash;
        this.signers = signers;
        this.numsigners = signers.size();
        setFollowingSigners();
        this.state = "PENDING";
        this.uploadtime = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Signers getSigners() {
        return signers;
    }

    public void setSigners(Signers signers) {
        this.signers = signers;
    }

    public int getNumsigners() {
        return numsigners;
    }

    public void setNumsigners(int numsigners) {
        this.numsigners = numsigners;
    }

    public Users getCurrentsigner() {
        return currentsigner;
    }

    public void setCurrentsigner(Users currentsigner) {
        this.currentsigner = currentsigner;
    }

    public int getNextsigner() {
        return nextsigner;
    }

    public void setNextsigner(int nextsigner) {
        this.nextsigner = nextsigner;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUploadtime() {
        return uploadtime;
    }

    public void setUploadtime(String uploadtime) {
        this.uploadtime = uploadtime;
    }

    public Collection<Users> getAllSigners(){
        return signers.getAllUsers();
    }

    public void setFollowingSigners(){
        try{
            setNextsigner(nextsigner + 1);
            setCurrentsigner(signers.get(nextsigner));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void finalizeDocument(){
        //Invoca função de final de assinatura
        setState("FINALIZED AT " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
    }

    public Users getEmiiter() {
        return emiiter;
    }

    public void setEmiiter(Users emiiter) {
        this.emiiter = emiiter;
    }

    public List<Users> getAlreadySigned(){

        List<Users> alreadySigned = new ArrayList<>();
        try{
            Map<Integer, Users> allUsersMap = signers.getSigners();
            int i = 1;
            while(i < nextsigner) {
                alreadySigned.add(allUsersMap.get(i));
                i++;
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }

        return alreadySigned;
    }

    public List<Users> getFollowingSigners(){
        List<Users> nextSigners = new ArrayList<>();

        try{
            Map<Integer, Users> allUsersMap = signers.getSigners();
            int i = nextsigner + 1;
            while(i < (numsigners+1)) {
                nextSigners.add(allUsersMap.get(i));
                i++;
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }

        return nextSigners;
    }

    public Users getNextSigner(){
        return signers.getSigners().get(nextsigner);
    }

    public String getSignedhash() {
        return signedhash;
    }

    public void setSignedhash(String signedhash) {
        this.signedhash = signedhash;
    }
}
