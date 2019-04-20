package com.vermeg.testapp.models;

import java.util.List;

public class RichKey {

    private List<String> colonnessources;
    private String mappingformula;
    private String condition;
    private String colonnecible;
    private String converter;
    private String pattern;

    public List<String> getColonnessources() {
        return colonnessources;
    }

    public void setColonnessources(List<String> colonnessources) {
        this.colonnessources = colonnessources;
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
