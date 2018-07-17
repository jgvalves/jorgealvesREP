package com.multicert.mtchain.users.repository.app;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository("appRepository")
public interface AppRepository extends DataTablesRepository<App, Long> {
    App findByname(String name);
}
