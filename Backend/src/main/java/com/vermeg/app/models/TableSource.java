package com.vermeg.app.models;


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
public class TableSource  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idTable;
    private String nomTable;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    @OnDelete(action = OnDeleteAction.CASCADE)

    private BaseSource base;


    @OneToMany(cascade=ALL,fetch = FetchType.EAGER, mappedBy="table")
    @JsonManagedReference

    public List<ColonneR> colonnes;
}
