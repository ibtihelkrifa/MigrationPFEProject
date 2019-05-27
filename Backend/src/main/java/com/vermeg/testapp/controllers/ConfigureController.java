package com.vermeg.testapp.controllers;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.vermeg.testapp.models.*;
import com.vermeg.testapp.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import scalaclasses.BuildConf;
import scalaclasses.ConfigureService;
import scalaclasses.ConfigureServiceTest;

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


    @PostMapping("/configurer")
    public Configuration Configurer(@RequestBody Configuration conf)
    {  BuildConf buildConf= new BuildConf();

        BaseCible bcible=this.connectionService.getcurrentcible();
        BaseSource bsource= this.connectionService.getcurrentsource();
        conf.setBaseSource(bsource);
        conf.setBaseCible(bcible);

        XStream xstream = new XStream(new DomDriver());

        String xml = xstream.toXML(conf);
        System.out.println(conf.getTransformations().get(0).getDocuments().get(0).colonnecible);
        System.out.println(xml);
         buildConf.buildsourceetcible(conf.getBaseSource(),conf.getBaseCible());

         buildConf.buildTransformations(conf.getTransformations());
        new ConfigureService().Configurer("/home/ibtihel/Desktop/PFE/testapp/testconf2.xml");

        return conf;

    }


}
