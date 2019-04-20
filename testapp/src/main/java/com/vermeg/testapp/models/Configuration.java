package com.vermeg.testapp.models;

import java.util.List;

public class Configuration {
    String typeimulation;
    List<Transformation> transformations;

    public String getTypeimulation() {
        return typeimulation;
    }

    public void setTypeimulation(String typeimulation) {
        this.typeimulation = typeimulation;
    }

    public List<Transformation> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<Transformation> transformations) {
        this.transformations = transformations;
    }
}
