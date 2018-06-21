package Plataform.Entities.Admin;

import Plataform.Entities.Admin.Model.AdminEntity;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

public interface AdminRepository  extends DataTablesRepository<AdminEntity, Long> {
}
