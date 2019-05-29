package com.vermeg.app.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

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


    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn

    private User owner;

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Long getIdBase() {
        return idBase;
    }

    public void setIdBase(Long idBase) {
        this.idBase = idBase;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getQuorum() {
        return quorum;
    }

    public void setQuorum(String quorum) {
        this.quorum = quorum;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
