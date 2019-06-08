package com.vermeg.app.controllers;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.vermeg.app.models.*;
import com.vermeg.app.repositories.ExecutionRepository;
import com.vermeg.app.repositories.UserRepository;
import com.vermeg.app.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import scala.vermeg.sevices.BuildConf;
import scala.vermeg.sevices.ConfigureService;
import scala.vermeg.sevices.Rollback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class ConfigureController {

    @Autowired
    ConnectionService connectionService;

    @Autowired
    ExecutionRepository executionRepository;

    @Autowired
    UserRepository userRepository;

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


    @GetMapping("/rollback")
    public String rollback()
    {
        return new Rollback().rollbackfunction("/home/ibtihel/Desktop/PFE/testapp/rollbackconf.xml");

    }




    @PostMapping("/configurer/{username}")
    public Execution Configurer(@RequestBody Configuration conf, @PathVariable String username)
    {  BuildConf buildConf= new BuildConf();
    Execution execution= new Execution();
    execution.setTypeExecution(conf.getTypesimulation());
        Date actuelle = new Date();

       User user= this.userRepository.findUserByUsername(username);
        execution.setOwner(user);
      /*  DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dat = dateFormat.format(actuelle);*/
      execution.setDateExecution(actuelle);
      execution.setTitle(conf.getTitle());

      Execution exec= executionRepository.save(execution);

        BaseCible bcible=this.connectionService.getcurrentcible();
        BaseSource bsource= this.connectionService.getcurrentsource();
        conf.setBaseSource(bsource);
        conf.setBaseCible(bcible);

        XStream xstream = new XStream(new DomDriver());

        String xml = xstream.toXML(conf);
       // System.out.println(conf.getTransformations().get(0).getDocuments().get(0).colonnecible);
        System.out.println(xml);
         buildConf.buildsourceetcible(conf.getBaseSource(),conf.getBaseCible());
         buildConf.setTypeSimulation(conf.getTypesimulation());
         buildConf.buildTransformations(conf.getTransformations());

        new ConfigureService().Configurer("/home/ibtihel/Desktop/PFE/testapp/testconf2.xml");

        return exec;

    }



    @GetMapping("/resultexecution/{result}/{username}")
    public String setresultexecution(@PathVariable String result,@PathVariable String username)
    {

        this.connectionService.saveresultexecution(result,username);

        return "done";

    }

    @GetMapping("executions/{username}")
    public List<Execution> getexecutions(@PathVariable String username)
    {

        User user= this.userRepository.findUserByUsername(username);
        return this.executionRepository.findByOwner(user);
    }


}
