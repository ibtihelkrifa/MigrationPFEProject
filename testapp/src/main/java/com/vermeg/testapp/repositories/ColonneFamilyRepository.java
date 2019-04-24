package com.vermeg.testapp.repositories;


import com.vermeg.testapp.models.FamilleColonne;
import com.vermeg.testapp.models.TableCible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColonneFamilyRepository extends JpaRepository<FamilleColonne,Long> {


 public   FamilleColonne findByFamilyColumnName(String nomcolonnefamille);


 public List<FamilleColonne> findByTable(TableCible table);

}
