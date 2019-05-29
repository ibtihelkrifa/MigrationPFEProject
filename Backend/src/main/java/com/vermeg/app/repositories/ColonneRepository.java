package com.vermeg.app.repositories;

import com.vermeg.app.models.ColonneR;
import com.vermeg.app.models.TableSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColonneRepository extends JpaRepository<ColonneR,Long> {


     ColonneR findByNomcolonne(String nomcolonne);


    List<ColonneR> findColonneRByTable(TableSource table);
}
