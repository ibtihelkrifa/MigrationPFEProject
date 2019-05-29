package com.vermeg.app.repositories;

import com.vermeg.app.models.BaseCible;
import com.vermeg.app.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CibleRepository extends JpaRepository<BaseCible,Long> {

    BaseCible findBaseCibleByMaster(String master);
List<BaseCible> findBaseCibleByOwner(User owner);


}
