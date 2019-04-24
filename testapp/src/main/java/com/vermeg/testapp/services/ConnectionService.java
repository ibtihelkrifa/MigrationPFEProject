package com.vermeg.testapp.services;


import com.vermeg.testapp.models.*;
import com.vermeg.testapp.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ConnectionService {

@Autowired
    SourceRepository sourceRepository;
@Autowired
    TableSourceRepository tableSourceRepository;
  @Autowired
  CibleRepository cibleRepository;
  @Autowired
  TableCibleRepository tableCibleRepository;
  @Autowired
    ColonneRepository colonneRepository;
  @Autowired
  ColonneFamilyRepository colonneFamilyRepository;

  @Autowired
  ColonneHRepository colonneHRepository;

    public BaseSource saveBase(BaseSource baseSource)
    {
       return this.sourceRepository.save(baseSource);
    }

    public BaseCible saveCible(BaseCible baseCible){ return this.cibleRepository.save(baseCible);}


    public Boolean existcible(BaseCible baseCible)
    {
        if(this.cibleRepository.findBaseCibleByMaster(baseCible.getMaster())!= null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public Boolean existSource(BaseSource baseSource)
    {
        if(this.sourceRepository.findBaseSourceByNomBase(baseSource.getNomBase())!= null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public TableSource saveTable(TableSource tableSource)
    {
         return this.tableSourceRepository.save(tableSource);

    }

     public TableCible saveTableCible(TableCible tablecible)
     {
         return this.tableCibleRepository.save(tablecible);
     }



    public List<TableSource> getTableSource(BaseSource baseSource)
    {
        return this.tableSourceRepository.findByBase(baseSource);
    }

    public List<TableCible> getTablesCibles(BaseCible baseCible)
    {
        return this.tableCibleRepository.findByBase(baseCible);
    }


    public BaseSource getbaseByName(String nombase)
    {
        return  this.sourceRepository.findBaseSourceByNomBase(nombase);
    }


    public BaseCible getCibleByMaster(String master)
    {
        return  this.cibleRepository.findBaseCibleByMaster(master);
    }


    public Boolean existTableSource(String NomTable,BaseSource Base)
    {
        if(this.tableSourceRepository.findByBaseAndNomTable(Base,NomTable)!= null)
        {
            return true;
        }
        return false;
    }

    public Boolean existtablecible(String NomTable, BaseCible basecible)
    {
        if(this.tableCibleRepository.findByBaseAndNomTable(basecible,NomTable)!= null)
        {
            return true;
        }
        return false;
    }


    public ColonneR savecolonneR( ColonneR colonneR)
    {
     return this.colonneRepository.save(colonneR);
    }

    public FamilleColonne savecolonneFamily(FamilleColonne familleColonne)
    {
        return  this.colonneFamilyRepository.save(familleColonne);
    }


    public List<TableSource> getallTablesource(BaseSource basesource)
    {
        return this.tableSourceRepository.findByBase(basesource);
    }

    public Boolean existColumnR(String nomcolonneR)
    {
        if(colonneRepository.findByNomcolonne(nomcolonneR) !=null)
        {
            return true;
        }
        return false;
    }


    public Boolean existColumnFamily(String nomcolonneF)
    {
        if(colonneFamilyRepository.findByFamilyColumnName(nomcolonneF)!= null)
        {
            return true;
        }

        return false;
    }


    public BaseSource getcurrentsource() {
        if (this.sourceRepository.count() != 0) {
            return this.sourceRepository.findAll().get(0);
        }
        return  null;
    }

    public BaseCible getcurrentcible()
    {
        if(this.cibleRepository.count() !=0)
        {
         return this.cibleRepository.findAll().get(0);
        }
        return null;
    }

    public void deleteCible()
    {
        if(this.colonneHRepository.count() !=0)
        {
            this.colonneHRepository.deleteAll();
        }

        if(this.colonneFamilyRepository.count() != 0)
        {
            this.colonneFamilyRepository.deleteAll();
        }
        if(this.tableCibleRepository.count() != 0)
        {
            this.tableCibleRepository.deleteAll();

        }
        if(this.cibleRepository.count() != 0)
        {
            this.cibleRepository.deleteAll();
        }



    }

    public void deleSource()
    {

        if(this.tableSourceRepository.count() !=0 )
        {
            this.tableSourceRepository.deleteAll();
        }

        if(this.colonneRepository.count() !=0)
        {
            this.colonneRepository.deleteAll();
        }

        if(this.sourceRepository.count() != 0)
        {

            this.sourceRepository.deleteAll();
        }
    }



    public List<FamilleColonne> getListFamille(TableCible tableCible)
    {
        return this.colonneFamilyRepository.findByTable(tableCible);
    }

    public ColonneH saveColonneH(ColonneH colonneH)
    {
        return this.colonneHRepository.save(colonneH);
    }

    public Boolean existColonneH(String nomcolonneH, FamilleColonne FC)
    {
        if(this.colonneHRepository.findByNomcolonneAndColumnFamily(nomcolonneH,FC) == null)
        {
            return false;
        }

        return true;
    }

    public List<TableSource> getAllTableSources()
    {
        return this.tableSourceRepository.findAll();
    }


    public List<TableCible> getAllTablesCibles()
    {
        return this.tableCibleRepository.findAll();
    }

}
