package com.vermeg.app.repositories;


import com.vermeg.app.models.FamilleColonne;
import com.vermeg.app.models.TableCible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColonneFamilyRepository extends JpaRepository<FamilleColonne,Long> {


 public   FamilleColonne findByNomcolonneFamily(String nomcolonnefamille);


 public List<FamilleColonne> findByTable(TableCible table);

}
