package com.vermeg.testapp.repositories;

import com.vermeg.testapp.models.BaseSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceRepository extends JpaRepository<BaseSource,Long> {


    BaseSource findBaseSourceByNomBase(String nombase);


}
