package com.vermeg.testapp.repositories;

import com.vermeg.testapp.models.BaseCible;
import com.vermeg.testapp.models.TableCible;
import org.codehaus.jackson.map.Serializers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TableCibleRepository extends JpaRepository<TableCible,Long> {

    public List<TableCible> findByBase(BaseCible baseCible);

    public TableCible findByBaseAndNomTable(BaseCible basecible,String NomTable);




}
