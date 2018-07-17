
package com.multicert.mtchain.users.service;


import com.multicert.mtchain.core.Blockchain;
import com.multicert.mtchain.users.cryptography.Certification;
import com.multicert.mtchain.users.repository.blockchain.ChaincodeService;
import com.multicert.mtchain.users.repository.document.DocumentRepository;
import com.multicert.mtchain.users.repository.document.DocumentService;
import com.multicert.mtchain.users.repository.document.model.Document;
import com.multicert.mtchain.users.repository.document.model.Signers;
import com.multicert.mtchain.users.repository.users.Model.Users;
import com.multicert.mtchain.users.repository.users.UsersRepository;
import com.multicert.mtchain.users.utils.Logs;
import org.hyperledger.fabric.sdk.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;


@RestController
public class ServiceController {

    @Autowired
    private ChaincodeService chaincodeService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;




    @GetMapping("/")
    public RedirectView login(){
        return new RedirectView("/users/login");
    }

    @GetMapping("/login")
    public RedirectView loginPage(){
        return new RedirectView("/users/index");
    }

    @GetMapping("/index")
    public ModelAndView index() {
            try {

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Users user = (Users) auth.getPrincipal();

                Iterable<Document> documents = documentRepository.findAll();

                List<DocumentMimic> pendentDocs = getAllPendentDocuments(documents, user);
                List<DocumentMimic> historyDocs = getAllHistoryDocuments(documents, user);
                List<UsersMimic> appUsers = getAllAppUsers(user);

                ModelAndView mav = new ModelAndView("index");
                mav.addObject("user", user.getName());
                mav.addObject("login", user.getLastlogin());
                mav.addObject("app", user.getApp());
                mav.addObject("certdate", user.getCertdate());
                mav.addObject("signingdocs", pendentDocs.size());
                mav.addObject("doclist", pendentDocs);
                mav.addObject("userslist", appUsers);
                mav.addObject("userslistsize", appUsers.size());
                mav.addObject("historylist", historyDocs);


                return mav;
            } catch (Exception e) {
                return new ModelAndView("error");
            }
    }

    @PostMapping("/newsignature")
    public RedirectView newSignature(@RequestParam("name") String name,
                                     @RequestParam("desc") String description,
                                     @RequestParam("document") MultipartFile document,
                                     @RequestParam(name = "present", required = false) List<Long> signers){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users user = (Users) auth.getPrincipal();
        Signers signersMap = new Signers(new HashMap<>());
        String hash = null;
        try {
            hash = Certification.computeHash(document);
            signersMap.put(1, user);
            int i = 2;
            for (Long id : signers) {
                Users u = usersRepository.findByuserid(id);
                signersMap.put(i, u);
                i++;
            }
        }
        catch(Exception e){
            signersMap.put(1, user);
        }


        Document doc = documentService.addNewDocument(name, description, user, hash, signersMap);
        documentStartOP(doc);

        return new RedirectView("/users/index");
    }

    @PostMapping("/signdocument")
    public RedirectView signDocument(@RequestParam("docid") Long id,
                                     @RequestParam("clientSignature") String clientSignature){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users user = (Users) auth.getPrincipal();

        Document document = documentRepository.findByid(id);

        try{
            byte[] signatureBytes = Certification.hexStringToByteArray(clientSignature);
            if(Certification.verifySignature(document.getSignedhash().getBytes(), signatureBytes, user.getCertificate().getPublicKey())){
                try{
                    Blockchain blockchain = chaincodeService.getChaincodeObject(user.getApp());
                    blockchain.sign_op(Initializator.client, Initializator.channelObj, user.getName(), clientSignature, document.getHash());
                    document.setSignedhash(clientSignature);
                    document.setFollowingSigners();
                    documentService.saveDocChanges(document);
                }
                catch(Exception e){
                    e.printStackTrace();
                    return new RedirectView("/error");
                }
            }
            else{
                throw new Exception("Signature not matching with user certificate!");
            }

        }
        catch(Exception e){
            e.printStackTrace();
            return new RedirectView("/error");
        }

        try{
            if(document.getCurrentsigner().equals(null)){
                throw new Exception();
            }}
        catch(Exception e){
            documentEndOP(document);
        }

        return new RedirectView("/users/index");
    }

    @GetMapping("/query/{id}")
    public String query(@PathVariable("id") Long id){
        Document d = documentRepository.findByid(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users user = (Users) auth.getPrincipal();

        try{
            Blockchain blockchain = chaincodeService.getChaincodeObject(user.getApp());
            return blockchain.queryDocument(Initializator.client, Initializator.channelObj, d.getHash());
        }
        catch(Exception e){
            e.printStackTrace();
            return "erro";
        }
    }


    private List<DocumentMimic> getAllPendentDocuments(Iterable<Document> documents, Users user){

        List<DocumentMimic> list = new ArrayList<>();

        for(Document d: documents){
            try {
                if (d.getCurrentsigner().getUserid().equals(user.getUserid())) {
                    list.add(mimicDocument(d));
                }
            }
            catch(NullPointerException e){}
        }
        return list;
    }

    private List<DocumentMimic> getAllHistoryDocuments(Iterable<Document> documents, Users user){

        List<DocumentMimic> list = new ArrayList<>();

        for(Document d: documents){
            try {
                for(Users u: d.getAllSigners()) {
                    if(u.getUserid().equals(user.getUserid())) {
                        DocumentMimic dm = mimicDocument(d);
                        list.add(dm);
                    }
                }
                generateSignatureHtml(list);
            }
            catch(NullPointerException npe){
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return list;
    }

    private void documentStartOP(Document document){
        try{
            //Blockchain blockchain = blockchainInfoService.getChaincodeObject(document.getEmiiter().getApp());
            Blockchain blockchain = chaincodeService.getChaincodeObject(document.getEmiiter().getApp());
            byte[] signedHash = Certification.performSignature(document.getHash(), Certification.getPrivateKey());
            blockchain.start_op(Initializator.client,
                    Initializator.channelObj,
                    "Platform",
                    Certification.bytesToHex(signedHash),
                    document.getHash());
            document.setSignedhash(Certification.bytesToHex(signedHash));
            documentRepository.save(document);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void documentEndOP(Document document){
        try{
            Blockchain blockchain = chaincodeService.getChaincodeObject(document.getEmiiter().getApp());
            byte[] signedHash = Certification.performSignature(document.getHash(), Certification.getPrivateKey());
            blockchain.end_op(Initializator.client,
                    Initializator.channelObj,
                    "Platform",
                    Certification.bytesToHex(signedHash),
                    document.getHash());
            document.setSignedhash(Certification.bytesToHex(signedHash));
            document.finalizeDocument();
            documentRepository.save(document);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private DocumentMimic mimicDocument(Document document){
        DocumentMimic dm = new DocumentMimic(document.getId(), document.getName(), document.getCurrentsigner(), document.getEmiiter(), document.getDescription(), document.getHash(), document.getUploadtime(), document.getState());
        dm.setAlreadySigned(document.getAlreadySigned());
        dm.setNextSigners(document.getFollowingSigners());
        dm.setSignedHash(document.getSignedhash());

        try {
            document.getFollowingSigners().get(0).getName();
            dm.setNextSigner(document.getFollowingSigners().get(0));
        }
        catch (Exception e){
            dm.setNextSigner(new Users("No next signer!", null, null, null, null));
        }

        try{
            dm.getCurrentSigner().getName();
        }
        catch(Exception e){
            dm.setCurrentSigner(new Users("No next signer!", null, null, null, null));
        }

        return dm;

    }

    private void generateSignatureHtml(List<DocumentMimic> docs) throws Exception{

        for(DocumentMimic doc: docs){
            int i = 0;
            String finalImage = "Not <br> Finished...";
            String htmlCode = "<ul class=\"timeline\">";
            int platSignatures = 0;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Users user = (Users) auth.getPrincipal();

            String jsonString = queryDocument(Initializator.client, Initializator.channelObj, doc.getHash(), chaincodeService.getChaincodeObject(user.getApp()));

            JSONObject jsonbject = new JSONObject(jsonString);

            JSONArray jsonArray = jsonbject.getJSONArray("data");

            for(int j = 0; j < jsonArray.length(); j++){



                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String signer = jsonObject.getString("signer");
                String sig_id = jsonObject.getString("ID");
                String hash = jsonObject.getString("hash");
                String time = jsonObject.getString("time");

                String img = "img/user.png";

                if(signer.equals("Platform")){
                    img = "img/platform.png";
                    platSignatures++;
                }


                String str = "";
                if(i%2==0) {
                    str =
                            "<li class=\"timeline\">" +
                                    "<div class=\"timeline-image\">" +
                                    "<img class=\"rounded-circle img-fluid\" src=" + img + " alt=\"\">" +
                                    " </div>" +
                                    "<div class=\"timeline-panel\">" +
                                    "<div class=\"timeline-heading\">" +
                                    "<h4>" + signer + "</h4>" +
                                    "<h4 class=\"subheading\">" + sig_id + "</h4>" +
                                    "</div>" +
                                    "<div class=\"timeline-body\">" +
                                    "<p  class=\"text-muted hashbox\">" + hash + "</p>" +
                                    "</div>" +
                                    "<p class=\"text-muted\">" + time + "</p>" +
                                    "</div>" +
                                    "</li>";
                }
                else{
                    str = "<li class=\"timeline-inverted\">" +
                            "<div class=\"timeline-image\">" +
                            "<img class=\"rounded-circle img-fluid\" src=" + img + " alt=\"\">" +
                            " </div>" +
                            "<div class=\"timeline-panel\">" +
                            "<div class=\"timeline-heading\">" +
                            "<h4>" + signer + "</h4>" +
                            "<h4 class=\"subheading\">" + sig_id + "</h4>" +
                            "</div>" +
                            "<div class=\"timeline-body\">" +
                            "<p class=\"text-muted hashbox\">" + hash + "</p>" +
                            "</div>" +
                            "<p class=\"text-muted\">" + time +"</p>" +
                            "</div>" +
                            "</li>";
                }
                htmlCode += str;
                i++;
            }

            if(platSignatures==2){
                finalImage = "<br>Finalized!";
            }

            htmlCode += "<li class=\"timeline\">" +
                            "<div class=\"timeline-image\">" +
                            "<h4> " + finalImage + "</h4>" +
                            "</div>" +
                        "</li>";

            htmlCode += "</ul>";
            doc.setHtmlCode(htmlCode);

        }
    }

    public String queryDocument(HFClient client, Channel channel, String documentName, Blockchain blockchain){

        try {
            String str = "{\"data\":[";
            if (blockchain.getIDHashtable().containsKey(documentName)) {
                List<UUID> idList = blockchain.getIDHashtable().get(documentName);

                for (UUID id : idList) {
                    str = str + queryDocumentSignatures(client, channel, documentName, id, blockchain) + ",";
                }
                return str + "]}";
            } else {
                return "NO SUCH DOCUMENT";
            }
        }
        catch(Exception e){
        }
        return "Nothing to present";
    }

    private String queryDocumentSignatures(HFClient client, Channel channel, String documentName, UUID id, Blockchain blockchain) throws Exception{

        Logs.write("Query blockchain for document " + documentName);

        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        ChaincodeID ccId = ChaincodeID.newBuilder().setName(blockchain.getName()).setVersion(""+blockchain.getCurrentVersion()).build();
        qpr.setChaincodeID(ccId);
        qpr.setFcn("op_query");
        qpr.setArgs(id.toString());


        Logs.write("Getting chaincode " + ccId.getName() + ":" + ccId.getVersion());
        Logs.write("Querying the peers...");

        Collection<ProposalResponse> res = channel.queryByChaincode(qpr);

        String payload = "nothing";
        for (ProposalResponse pres : res) {

            try {
                if (!pres.getProposalResponse().getResponse().getPayload().toString().equals("")) {
                    payload = pres.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    Logs.write("Query worked.");
                } else {
                    Logs.write("Query didn't return anything.");
                }
            }
            catch(Exception e){
                Logs.write("Response from query returned null.");
                return "-1";
            }

        }

        return payload;

    }

    private List<UsersMimic> getAllAppUsers(Users user){
        List<UsersMimic> list = new ArrayList<>();

        for(Users u: usersRepository.findAll()){
            if(u.getApp().equals(user.getApp()) && !u.getUserid().equals(user.getUserid())){
                list.add(new UsersMimic(u.getUserid(), u.getName(), u.getApp()));
            }
        }

        return list;
    }
}

class DocumentMimic{

    private Long id;
    private String name;
    private Users currentSigner;
    private Users emitter;
    private String emitterName;
    private String description;
    private String emittionDate;
    private String state;
    private List<Users> alreadySigned;
    private List<Users> nextSigners;
    private Users signer;
    private String hash;
    private String signedHash;
    private String htmlCode;
    private Users nextSigner;


    public DocumentMimic(Long id, String name, Users currentSigner, Users emitter, String description, String hash, String emittionDate, String state) {
        this.id = id;
        this.name = name;
        this.currentSigner = currentSigner;
        this.emitterName = emitter.getName();
        this.description = description;
        this.emittionDate = emittionDate;
        this.state = state;
        this.hash = hash;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Users getCurrentSigner() {
        return currentSigner;
    }

    public void setCurrentSigner(Users currentSigner) {
        this.currentSigner = currentSigner;
    }

    public Users getEmitter() {
        return emitter;
    }

    public void setEmitter(Users emitter) {
        this.emitter = emitter;
    }

    public String getEmitterName() {
        return emitterName;
    }

    public void setEmitterName(String emitterName) {
        this.emitterName = emitterName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmittionDate() {
        return emittionDate;
    }

    public void setEmittionDate(String emittionDate) {
        this.emittionDate = emittionDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Users> getAlreadySigned() {
        return alreadySigned;
    }

    public void setAlreadySigned(List<Users> alreadySigned) {
        this.alreadySigned = alreadySigned;
    }

    public List<Users> getNextSigners() {
        return nextSigners;
    }

    public void setNextSigners(List<Users> nextSigners) {
        this.nextSigners = nextSigners;
    }

    public Users getSigner() {
        return signer;
    }

    public void setSigner(Users signer) {
        this.signer = signer;
    }

    public String getSignedHash() {
        return signedHash;
    }

    public void setSignedHash(String signedHash) {
        this.signedHash = signedHash;
    }

    public String getHtmlCode() {
        return htmlCode;
    }

    public void setHtmlCode(String htmlCode) {
        this.htmlCode = htmlCode;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Users getNextSigner() {
        return nextSigner;
    }

    public void setNextSigner(Users nextSigner) {
        this.nextSigner = nextSigner;
    }
}

class UsersMimic{

    private Long id;
    private String name;
    private String app;

    public UsersMimic(Long id, String name, String app) {
        this.id = id;
        this.name = name;
        this.app = app;
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


}
