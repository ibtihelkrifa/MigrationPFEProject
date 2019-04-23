package com.vermeg.testapp.controllers;


import com.sun.javafx.collections.MappingChange;
import com.vermeg.testapp.models.*;
import com.vermeg.testapp.services.ConnectionService;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import scalaclasses.GestionConnection;

import javax.ws.rs.DELETE;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class ConnectionController {

    @Autowired
    ConnectionService connectionService;

    GestionConnection gestionConnection = new GestionConnection();


    @PostMapping("/connectsource")
    public BaseSource connectsource(@RequestBody BaseSource baseSource)
    {


        try {
            Class.forName("com.mysql.jdbc.Driver");
            String jdbcURL= "jdbc:mysql://localhost:" + baseSource.getPort() +"/" + baseSource.getNomBase();
            String user= baseSource.getUser();
            String password = baseSource.getPassword();
            Connection jdbcConnection =  DriverManager.getConnection(jdbcURL, user, password);
           if(! this.connectionService.existSource(baseSource)) {
               BaseSource base= this.connectionService.saveBase(baseSource);
           }
               ArrayList<String> tables= new ArrayList<String>();
               tables= gestionConnection.getMysqlTables(jdbcConnection);

               for(int i=0; i< tables.size(); i++ )
               {
                   TableSource tableSource= new TableSource();
                   tableSource.setNomTable(tables.get(i));
                   tableSource.setBase(connectionService.getbaseByName(baseSource.getNomBase()));
                   if(!connectionService.existTableSource(tableSource.getNomTable(),tableSource.getBase())) {
                   TableSource tableSource1= this.connectionService.saveTable(tableSource);}
               }



               BaseSource baseSource1= connectionService.getbaseByName(baseSource.getNomBase());

            List<TableSource> listtables= connectionService.getallTablesource(baseSource1);

             for(int k=0; k< listtables.size();k++) {



                List<GestionConnection.Column> colonnes=gestionConnection.getColumnNamesMysqlTable(jdbcConnection, listtables.get(k).getNomTable());

                 for (int j = 0; j < colonnes.size(); j++) {
                   if(! connectionService.existColumnR(colonnes.get(j).getcolumnname()))
                   {
                     ColonneR colonneR = new ColonneR();
                     colonneR.setNomcolonne(colonnes.get(j).getcolumnname());
                     colonneR.setTypecolonne(colonnes.get(j).gettypecolumn());
                     colonneR.setTable(listtables.get(k));
                     connectionService.savecolonneR(colonneR);
                 }}

             }



            return baseSource;
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return null;
    }



    @PostMapping("/connectcible")
    public BaseCible connectcible(@RequestBody BaseCible basecible)
    {


        try {

         org.apache.hadoop.hbase.client.Connection connection= gestionConnection.checkHbaseConnection(basecible.getMaster(),basecible.getQuorum(),basecible.getPort());


             if(!connectionService.existcible(basecible))
             {
                 connectionService.saveCible(basecible);
             }

            BaseCible baseCible= connectionService.getCibleByMaster(basecible.getMaster());
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

           }

            return  baseCible;

        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return null;
    }





    @GetMapping("/tablessources/{nombase}")
    public List<TableSource> getTableSource(@PathVariable  String nombase)
    {
        BaseSource baseSource= connectionService.getbaseByName(nombase);
         return this.connectionService.getTableSource(baseSource);
    }



    @GetMapping("/tablescibles/{master}")
    public List<TableCible> getTableCibles(@PathVariable  String master)
    {

        BaseCible baseCible= connectionService.getCibleByMaster(master);
        return this.connectionService.getTablesCibles(baseCible);

    }

    @GetMapping("/getCurrentSource")
    public BaseSource getcurrentSource()
    {
        return connectionService.getcurrentsource();
    }

    @GetMapping("/getcurrentcible")
    public BaseCible getcurrentcible()
    {
        return connectionService.getcurrentcible();
    }

    @DeleteMapping("/deletecible")
    public void deleteCible()
    {
        connectionService.deleteCible();
    }

    @DeleteMapping("/deleteSource")
    public void deleteSource()
    {
        connectionService.deleSource();
    }




}
