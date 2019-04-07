package com.vermeg.testapp.controllers;


import org.springframework.web.bind.annotation.*;
import scalaclasses.*;

import java.util.List;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
public class TestController {

    @GetMapping("test/")
    public String configure( )
    {
       return  new GreetingInScala().configure("/home/ibtihel/Desktop/PFE/testapp/testconfig.xml");
        //return "hello";
    }

    @GetMapping("test2/")
    public String configure2( )
    {
        return  new TestScalaClass().configure2("/home/ibtihel/Desktop/PFE/testapp/testconfig.xml");
        //return "hello";
    }

//fonctionnel
    @GetMapping("test3/")
    public String Configurer( )
    {
        return new ConfigureService().Configurer("/home/ibtihel/Desktop/PFE/testapp/testconfig.xml");
        //return "hello";
    }

    @GetMapping("test4/")
    public String function2( )
    {
        return "hello";
    }

    @GetMapping("test6/")
    public String rollbackfunction()
    {
        return new Rollback().rollbackfunction("/home/ibtihel/Desktop/PFE/testapp/rollbackconf.xml");
    }





}
