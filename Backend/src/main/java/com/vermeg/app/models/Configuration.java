package com.vermeg.app.models;

import java.util.List;

public class Configuration {

    String title;
    BaseSource baseSource;

    BaseCible baseCible;
    String typesimulation;
    List<Transformation> transformations;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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
