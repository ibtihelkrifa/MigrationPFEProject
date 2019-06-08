package com.vermeg.app.services;


import com.vermeg.app.models.*;
import com.vermeg.app.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  @Autowired
  UserRepository userRepository;

  @Autowired
  ExecutionRepository executionRepository;

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
        if(colonneFamilyRepository.findByNomcolonneFamily(nomcolonneF)!= null)
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

    public void deleteCible(String username)
    {
      User owner =this.userRepository.findUserByUsername(username);
      List<BaseCible> listbasescibles=this.cibleRepository.findBaseCibleByOwner(owner);
      for(int i=0; i < listbasescibles.size(); i++)
      {
          List<TableCible> listtables=this.tableCibleRepository.findByBase(listbasescibles.get(i));

          for(int j=0; j< listtables.size(); j++)
          {
              List<FamilleColonne> listF= this.colonneFamilyRepository.findByTable(listtables.get(j));

              for(int f=0 ; f< listF.size(); f++)
              {
                  List<ColonneH> listCH= this.colonneHRepository.findByColumnFamily(listF.get(f));
                  for(int h=0; h < listCH.size(); h++)
                  {
                      this.colonneHRepository.delete(listCH.get(h));
                  }

                  this.colonneFamilyRepository.delete(listF.get(f));
              }

              this.tableCibleRepository.delete(listtables.get(j));

          }

          this.cibleRepository.delete(listbasescibles.get(i));
      }


    }

    public void deleSource(String username)
    {

            User owner= this.userRepository.findUserByUsername(username);
            List<BaseSource> listBasesSources=this.sourceRepository.findBaseSourceByOwner(owner);

            for(int i=0; i < listBasesSources.size();i++)
            {
                List<TableSource> listtables= this.tableSourceRepository.findByBase(listBasesSources.get(i));
                for(int j=0; j < listtables.size();j++)
                {
                    List<ColonneR> listcolonnes= this.colonneRepository.findColonneRByTable(listtables.get(j));
                    for(int k=0; k < listcolonnes.size(); k++)
                    {
                        this.colonneRepository.delete(listcolonnes.get(k));
                    }

                    this.tableSourceRepository.delete(listtables.get(j));

                }

                this.sourceRepository.delete(listBasesSources.get(i));
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

    public void saveresultexecution(String result, String username)
    {

        User user= this.userRepository.findUserByUsername(username);

        List<Execution> listexecution= this.executionRepository.findAll();
        listexecution.get(listexecution.size()-1).setResultat(result);
        Execution execution=listexecution.get(listexecution.size()-1);
        execution.setOwner(user);
        this.executionRepository.save(execution);
    }

}
