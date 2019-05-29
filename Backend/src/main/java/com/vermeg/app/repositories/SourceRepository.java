package com.vermeg.app.repositories;

import com.vermeg.app.models.BaseSource;
import com.vermeg.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<BaseSource,Long> {


    BaseSource findBaseSourceByNomBase(String nombase);
    List<BaseSource> findBaseSourceByOwner(User owner);

}
