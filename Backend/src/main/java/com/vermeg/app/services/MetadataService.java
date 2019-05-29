package com.vermeg.app.services;


import com.mysql.cj.jdbc.JdbcConnection;
import com.vermeg.app.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.vermeg.sevices.GestionConnection;

import java.util.ArrayList;
import java.util.List;

@Service
public class MetadataService {

    @Autowired
    ConnectionService connectionService;


    GestionConnection gestionConnection = new GestionConnection();




    public void saveCibleMetadat(BaseCible baseCible,org.apache.hadoop.hbase.client.Connection connection)
    {
      ArrayList<String> tables= gestionConnection.getHbasetablesname(connection);

            for(int i=0; i< tables.size(); i++ )
    {
        TableCible tablecible= new TableCible();
        tablecible.setNomTable(tables.get(i));

        tablecible.setBase(baseCible);

        if(! connectionService.existtablecible(tablecible.getNomTable(),baseCible)) {
            this.connectionService.saveTableCible(tablecible);
        }
    }


    List<TableCible> listtables= connectionService.getTablesCibles(baseCible);
           for(int k=0; k< listtables.size();k++) {

        ArrayList<String> colonnesfamilies = gestionConnection.getColumnFamilies(connection, listtables.get(k).getNomTable());

        for (int j = 0; j < colonnesfamilies.size(); j++) {
            if (!connectionService.existColumnFamily(colonnesfamilies.get(j))) {
                FamilleColonne colonneF = new FamilleColonne();
                colonneF.setNomcolonneFamily(colonnesfamilies.get(j));
                colonneF.setTable(listtables.get(k));
                connectionService.savecolonneFamily(colonneF);
            }
        }


        List<FamilleColonne> listfamille = connectionService.getListFamille(listtables.get(k));

        for(int c=0; c < listfamille.size(); c++) {

            List<String> colonnes = gestionConnection.getColumnsHabseOfColumnFamily(connection, listtables.get(k).getNomTable(), colonnesfamilies.get(c));

            for(int h=0; h< colonnes.size(); h++)
            {
                ColonneH colonneH= new ColonneH();
                colonneH.setColumnFamily(listfamille.get(c));
                colonneH.setNomcolonne(colonnes.get(h));
                if(! connectionService.existColonneH(colonnes.get(h), listfamille.get(c)))
                {
                    connectionService.saveColonneH(colonneH);}
            }

        }

    }}






public void saveSourceMetadat(BaseSource baseSource, java.sql.Connection jdbcConnection )
{



    ArrayList<String> tables = new ArrayList<String>();
    tables = gestionConnection.getMysqlTables(jdbcConnection);

    for (int i = 0; i < tables.size(); i++) {
        TableSource tableSource = new TableSource();
        tableSource.setNomTable(tables.get(i));
        tableSource.setBase(connectionService.getbaseByName(baseSource.getNomBase()));
        if (!connectionService.existTableSource(tableSource.getNomTable(), tableSource.getBase())) {
            TableSource tableSource1 = this.connectionService.saveTable(tableSource);
        }
    }

    BaseSource baseSource1 = connectionService.getbaseByName(baseSource.getNomBase());

    List<TableSource> listtables = connectionService.getallTablesource(baseSource1);

            for (int k = 0; k < listtables.size(); k++) {


        List<GestionConnection.Column> colonnes = gestionConnection.getColumnNamesMysqlTable(jdbcConnection, listtables.get(k).getNomTable());

        for (int j = 0; j < colonnes.size(); j++) {
            if (!connectionService.existColumnR(colonnes.get(j).getcolumnname())) {
                ColonneR colonneR = new ColonneR();
                colonneR.setNomcolonne(colonnes.get(j).getcolumnname());
                colonneR.setTypecolonne(colonnes.get(j).gettypecolumn());
                colonneR.setTable(listtables.get(k));
                connectionService.savecolonneR(colonneR);
            }
        }

    }

}




}
