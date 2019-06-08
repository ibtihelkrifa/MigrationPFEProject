package com.vermeg.app.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.List;



public class RichKey implements Serializable {


    private List<String> colonnes;
    private String mappingformula;
    private String condition;
    private String colonnecible;
    private String converter;
    private String pattern;
    private String typecolonnecible ;

    public String getTypecolonnecible() {
        return typecolonnecible;
    }

    public void setTypecolonnecible(String typecolonnecible) {
        this.typecolonnecible = typecolonnecible;
    }

    public List<String> getColonnes() {
        return colonnes;
    }

    public void setColonnes(List<String> colonnes) {
        this.colonnes = colonnes;
    }

    public String getMappingformula() {
        return mappingformula;
    }

    public void setMappingformula(String mappingformula) {
        this.mappingformula = mappingformula;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getColonnecible() {
        return colonnecible;
    }

    public void setColonnecible(String colonnecible) {
        this.colonnecible = colonnecible;
    }

    public String getConverter() {
        return converter;
    }

    public void setConverter(String converter) {
        this.converter = converter;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
