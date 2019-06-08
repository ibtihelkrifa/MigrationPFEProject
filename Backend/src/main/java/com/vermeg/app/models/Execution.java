package com.vermeg.app.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
public class Execution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date dateExecution ;

    private String title;

    private String typeExecution;

    private String resultat;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn

    private User owner;

}
