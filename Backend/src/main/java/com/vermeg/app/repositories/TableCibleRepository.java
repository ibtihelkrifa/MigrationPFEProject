package com.vermeg.app.repositories;

import com.vermeg.app.models.BaseCible;
import com.vermeg.app.models.TableCible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TableCibleRepository extends JpaRepository<TableCible,Long> {

    public List<TableCible> findByBase(BaseCible baseCible);

    public TableCible findByBaseAndNomTable(BaseCible basecible,String NomTable);




}
