package com.vermeg.testapp.repositories;

import com.vermeg.testapp.models.BaseSource;
import com.vermeg.testapp.models.TableSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;


@Repository
public interface TableSourceRepository extends JpaRepository<TableSource,Long> {

    List<TableSource> findByBase(BaseSource nombase);


    TableSource findByBaseAndNomTable(BaseSource base,String nomTable);

}
