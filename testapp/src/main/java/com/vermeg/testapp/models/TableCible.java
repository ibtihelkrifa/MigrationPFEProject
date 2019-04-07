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
public class TableCible implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idTable;
    private String nomTable;


    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    private BaseCible base;


    @OneToMany(cascade=ALL,fetch = FetchType.EAGER, mappedBy="table")
    @JsonManagedReference
    public List<FamilleColonne> colonnesfamille;


}
