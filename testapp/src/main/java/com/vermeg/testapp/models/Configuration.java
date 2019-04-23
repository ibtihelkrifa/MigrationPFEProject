package com.vermeg.testapp.models;

import java.util.List;

public class Configuration {
    BaseSource baseSource;
    BaseCible baseCible;
    String typesimulation;
    List<Transformation> transformations;

    public BaseSource getBaseSource() {
        return baseSource;
    }

    public void setBaseSource(BaseSource baseSource) {
        this.baseSource = baseSource;
    }

    public BaseCible getBaseCible() {
        return baseCible;
    }

    public void setBaseCible(BaseCible baseCible) {
        this.baseCible = baseCible;
    }

    public String getTypesimulation() {
        return typesimulation;
    }

    public void setTypesimulation(String typesimulation) {
        this.typesimulation = typesimulation;
    }

    public List<Transformation> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<Transformation> transformations) {
        this.transformations = transformations;
    }
}
