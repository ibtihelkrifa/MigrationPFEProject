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
public class BaseSource implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long idBase;
    private String nomBase;
    private String port;
    private String ip;
    private String user;
    private String password;


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

    public String getNomBase() {
        return nomBase;
    }

    public void setNomBase(String nomBase) {
        this.nomBase = nomBase;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
