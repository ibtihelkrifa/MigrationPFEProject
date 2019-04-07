package com.vermeg.testapp.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;


@Entity
@Data
@NoArgsConstructor
public class BaseSource implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idBase;
    private String nomBase;
    private String port;
    private String ip;
    private String user;
    private String password;



}
