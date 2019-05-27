package com.vermeg.testapp.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;

@Entity
@Data
@NoArgsConstructor
public class TableSource  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idTable;
    private String nomTable;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    private BaseSource base;


    @OneToMany(cascade=ALL,fetch = FetchType.EAGER, mappedBy="table")
    @JsonManagedReference

    public List<ColonneR> colonnes;
}
