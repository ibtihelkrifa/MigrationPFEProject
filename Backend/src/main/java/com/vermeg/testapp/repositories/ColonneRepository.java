package com.vermeg.testapp.repositories;

import com.vermeg.testapp.models.ColonneR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColonneRepository extends JpaRepository<ColonneR,Long> {


    public ColonneR findByNomcolonne(String nomcolonne);

}
