package com.multicert.mtchain.users.repository.document;

import com.multicert.mtchain.users.repository.document.model.Document;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository("documentRepository")
public interface DocumentRepository extends DataTablesRepository<Document, Long> {

    public Document findByid(Long id);
}
