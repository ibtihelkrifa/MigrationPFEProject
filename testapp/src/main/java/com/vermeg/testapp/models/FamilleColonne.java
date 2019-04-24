package com.vermeg.testapp.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class FamilleColonne implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long columnId;
    private String familyColumnName;


    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    @JsonBackReference
    private TableCible table;


    @OneToMany(cascade=ALL,fetch = FetchType.EAGER, mappedBy="columnFamily")
    @JsonManagedReference
    public List<ColonneH> columns;
}
