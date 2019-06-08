package com.vermeg.app.controllers;


import com.sun.javaws.exceptions.InvalidArgumentException;
import com.vermeg.app.models.*;
import com.vermeg.app.services.ConnectionService;
import com.vermeg.app.services.MetadataService;
import com.vermeg.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import scala.vermeg.sevices.GestionConnection;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class ConnectionController {

    @Autowired
    ConnectionService connectionService;
    @Autowired
    MetadataService metadataService;

    @Autowired
    UserService userService;
    GestionConnection gestionConnection = new GestionConnection();


    @PostMapping("/connectsource/{username}")
    public BaseSource connectsource(@RequestBody BaseSource baseSource, @PathVariable String username) {


        try {

            User owner= this.userService.getUserByUserName(username);
                baseSource.setOwner(owner);
            this.deleteSource(username);

            Class.forName("com.mysql.jdbc.Driver");
            String user = baseSource.getUser();
            String password = baseSource.getPassword();


            String jdbcURL = "jdbc:mysql://localhost:" + baseSource.getPort() + "/" + baseSource.getNomBase();

            Connection jdbcConnection = DriverManager.getConnection(jdbcURL, user, password);
            BaseSource base = this.connectionService.saveBase(baseSource);
            this.metadataService.saveSourceMetadat(baseSource, jdbcConnection);


            return baseSource;
        }


        catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }


    @PostMapping("/connectcible/{username}")
    public BaseCible connectcible(@RequestBody BaseCible basecible,@PathVariable String username) {


        try {

            User owner= this.userService.getUserByUserName(username);
            basecible.setOwner(owner);

            this.deleteCible(username);

            org.apache.hadoop.hbase.client.Connection connection = gestionConnection.checkHbaseConnection(basecible.getMaster(), basecible.getQuorum(), basecible.getPort());

            BaseCible baseCible = connectionService.saveCible(basecible);
            //  BaseCible baseCible= connectionService.getCibleByMaster(basecible.getMaster());

            this.metadataService.saveCibleMetadat(baseCible, connection);


            return baseCible;

        }



        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }


    @GetMapping("/tablessources/{nombase}")
    public List<TableSource> getTableSource(@PathVariable String nombase) {
       BaseSource baseSource = connectionService.getbaseByName(nombase);
        return this.connectionService.getTableSource(baseSource);
    }


    @GetMapping("/tablescibles/{master}")
    public List<TableCible> getTableCibles(@PathVariable String master) {

        BaseCible baseCible = connectionService.getCibleByMaster(master);
        return this.connectionService.getTablesCibles(baseCible);

    }

    @GetMapping("/getCurrentSource")
    public BaseSource getcurrentSource() {
        return connectionService.getcurrentsource();
    }

    @GetMapping("/getcurrentcible")
    public BaseCible getcurrentcible() {
        return connectionService.getcurrentcible();
    }

    @DeleteMapping("/deletecible/{username}")
    public void deleteCible(@PathVariable String username) {
        connectionService.deleteCible(username);
    }

    @DeleteMapping("/deleteSource/{username}")
    public void deleteSource(@PathVariable String username) {
        connectionService.deleSource(username);
    }



}
