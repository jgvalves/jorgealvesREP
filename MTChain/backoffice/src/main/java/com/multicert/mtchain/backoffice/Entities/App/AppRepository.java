package com.multicert.mtchain.backoffice.Entities.App;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository("appRepository")
public interface AppRepository extends DataTablesRepository<App, Long> {
}
