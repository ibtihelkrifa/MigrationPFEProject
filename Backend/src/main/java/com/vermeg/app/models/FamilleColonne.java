package com.vermeg.app.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    private Long idcolonne;
    private String nomcolonneFamily;


    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)

    private TableCible table;


    @OneToMany(cascade=ALL,fetch = FetchType.EAGER, mappedBy="columnFamily")
    @JsonManagedReference
    public List<ColonneH> colonnes;
}