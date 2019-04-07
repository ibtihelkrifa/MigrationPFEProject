package com.vermeg.testapp.repositories;


import com.vermeg.testapp.models.ColonneH;
import com.vermeg.testapp.models.FamilleColonne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColonneHRepository extends JpaRepository<ColonneH, Long> {


    public ColonneH findByNomcolonneAndColumnFamily(String nomcolonneh, FamilleColonne fc);


}
