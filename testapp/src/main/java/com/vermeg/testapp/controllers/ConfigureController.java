package com.vermeg.testapp.controllers;


import com.vermeg.testapp.models.BaseSource;
import com.vermeg.testapp.models.TableCible;
import com.vermeg.testapp.models.TableSource;
import com.vermeg.testapp.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class ConfigureController {

    @Autowired
    ConnectionService connectionService;



    @GetMapping("/getTablesSources")
    public List<TableSource> getAllTablesSource()
    {
        return this.connectionService.getAllTableSources();
    }

    @GetMapping("/getTablesCibles")
    public List<TableCible> getAllTablesCibles()
    {
        return this.connectionService.getAllTablesCibles();
    }

}
