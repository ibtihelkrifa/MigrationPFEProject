package com.vermeg.testapp.controllers;


import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.web.bind.annotation.*;
import scalaclasses.*;

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


    @GetMapping("test3/")
    public String functioon2( )
    {
        return new Test5().functioon2();
        //return "hello";
    }

    @GetMapping("test4/")
    public String function2( )
    {
         return new Test4().function();
        //return "hello";
    }

}
