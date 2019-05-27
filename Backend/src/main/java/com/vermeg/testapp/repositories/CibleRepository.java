package com.vermeg.testapp.repositories;

import com.vermeg.testapp.models.BaseCible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CibleRepository extends JpaRepository<BaseCible,Long> {

    BaseCible findBaseCibleByMaster(String master);

}
