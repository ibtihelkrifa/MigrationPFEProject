package com.vermeg.testapp.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
@Data
@NoArgsConstructor
public class BaseCible implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idBase;
    @Column(unique = true)
    private String master;
    private String quorum;
    private String port;

}
