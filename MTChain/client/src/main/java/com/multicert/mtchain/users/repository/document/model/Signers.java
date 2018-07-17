package com.multicert.mtchain.users.repository.document.model;

import com.multicert.mtchain.users.repository.users.Model.Users;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class Signers implements Serializable{

    private Map<Integer, Users> signers;

    public void setSigners(Map<Integer, Users> signers) {
        this.signers = signers;
    }

    public Map<Integer, Users> getSigners() {
        return signers;
    }

    public Signers (Map<Integer, Users> signers){
        this.signers = signers;
    }

    public void put(Integer key, Users users){
        signers.put(key, users);
    }

    public int size(){
        try {
            return signers.size();
        }
        catch(Exception e){return 0;}
    }

    public Collection<Users> getAllUsers(){
        return signers.values();
    }

    public Users get(int index){
        return signers.get(index);
    }

}
