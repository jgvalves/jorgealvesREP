package com.multicert.mtchain.backoffice.Entities.Admin;

import com.multicert.mtchain.backoffice.Entities.Admin.Model.AdminEntity;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

public interface AdminRepository  extends DataTablesRepository<AdminEntity, Long> {
}
