package com.vermeg.app.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
public class ColonneH implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idcolonne;
    private String nomcolonne;
    private String typecolonne;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)

    private FamilleColonne columnFamily;
}
