package com.vermeg.app.repositories;


import com.vermeg.app.models.ColonneH;
import com.vermeg.app.models.FamilleColonne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColonneHRepository extends JpaRepository<ColonneH, Long> {


     ColonneH findByNomcolonneAndColumnFamily(String nomcolonneh, FamilleColonne fc);


    List<ColonneH> findByColumnFamily(FamilleColonne f);
}
