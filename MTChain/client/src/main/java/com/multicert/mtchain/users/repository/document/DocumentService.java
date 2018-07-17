package com.multicert.mtchain.users.repository.document;

import com.multicert.mtchain.users.repository.document.model.Document;
import com.multicert.mtchain.users.repository.document.model.Signers;
import com.multicert.mtchain.users.repository.users.Model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("documentService")
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Transactional
    public Document addNewDocument(String name, String description, Users emitter, String hash, Signers signers){
        Document d = new Document(name, description, emitter, hash, signers);
        documentRepository.save(d);
        return d;
    }

    @Transactional
    public void saveDocChanges(Document document){
        documentRepository.save(document);
    }

}
