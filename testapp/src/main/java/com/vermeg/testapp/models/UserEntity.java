package com.vermeg.testapp.models;

import org.apache.hadoop.hbase.client.Row;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import java.sql.Date;
import java.util.Calendar;
import java.util.Collection;
import java.util.Objects;

@Entity
public class UserEntity {
    private int userId;

    private String firstName;

    private String lastName;
    private Date birthDate;
    private double taille;
    private Collection<AddressEntity> addressesByUserId;

    @Id
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Basic
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Basic
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Basic
    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @Basic
    public double getTaille() {
        return taille;
    }

    public void setTaille(double taille) {
        this.taille = taille;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return userId == that.userId &&
                Double.compare(that.taille, taille) == 0 &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(birthDate, that.birthDate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(userId, firstName, lastName, birthDate, taille);
    }

    @OneToMany(mappedBy = "userByUserId")
    public Collection<AddressEntity> getAddressesByUserId() {
        return addressesByUserId;
    }

    public void setAddressesByUserId(Collection<AddressEntity> addressesByUserId) {
        this.addressesByUserId = addressesByUserId;
    }






}
