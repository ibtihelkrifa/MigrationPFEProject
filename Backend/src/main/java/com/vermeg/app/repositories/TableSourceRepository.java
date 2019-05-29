package com.vermeg.app.repositories;

import com.vermeg.app.models.BaseSource;
import com.vermeg.app.models.TableSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TableSourceRepository extends JpaRepository<TableSource,Long> {

    List<TableSource> findByBase(BaseSource nombase);


    TableSource findByBaseAndNomTable(BaseSource base,String nomTable);

}
