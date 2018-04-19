package initial2;

import org.bouncycastle.openssl.PEMWriter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.util.Set;

public class AppUser implements User, Serializable {

    private static final long serializationId = 1L;

    //private HFCAClient clientCA;
    private String name;
    private String mspId;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment;
    private String org;

    public AppUser() {
        // no-arg constructor
    }

    public AppUser(String name, String org, String mspId, Enrollment enrollment, HFCAClient clientCA){
        this.name = name;
        //this.clientCA = clientCA;
        this.affiliation = org;
        this.mspId = mspId;
        this.enrollment = enrollment;
    }

    @Override
    public String getName(){
        return this.name;
    }

    public String getCert(){
        return getEnrollment().getCert();
    }

    public String getPEMStringPrivKey() throws IOException{
        return getPEMStringFromPrivateKey(getEnrollment().getKey());
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public String getMspId(){
        return this.mspId;
    }

    public void getMspId(String mspId){
        this.mspId = mspId;
    }

    @Override
    public Set<String> getRoles(){
        return this.roles;
    }

    public void setRoles(Set<String> set){
        this.roles = set;
    }

    @Override
    public String getAccount(){
        return this.account;
    }

    public void setAccount(String account){
        this.account = account;
    }

    @Override
    public String getAffiliation(){
        return this.affiliation;
    }

    public void setAffiliation(){
        this.affiliation = affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    public void setEnrollment(Enrollment secret){
        this.enrollment = secret;
    }

    /*public HFCAClient getClientCA() {
        return this.clientCA;
    }

    public void setClientCA(HFCAClient clientCA){
        this.clientCA = clientCA;
    }*/

    @Override
    public String toString() {
        return "AppUser{" +
                "name='" + name + '\'' +
                "\n, roles=" + roles +
                "\n, account='" + account + '\'' +
                "\n, affiliation='" + affiliation + '\'' +
                "\n, enrollment=" + enrollment +
                "\n, mspId='" + mspId + '\'' +
                '}';
    }

    static String getPEMStringFromPrivateKey(PrivateKey privateKey) throws IOException {
        StringWriter pemStrWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(pemStrWriter);

        pemWriter.writeObject(privateKey);

        pemWriter.close();

        return pemStrWriter.toString();
    }

}
