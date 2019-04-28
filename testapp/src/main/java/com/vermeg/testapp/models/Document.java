package com.vermeg.testapp.models;

import java.util.List;

public class Document {
    public List<String> colonnessources;
    public String colonnecible;
    public String typejointure;
    public String clejointure;
    public String tablesource;

    public List<String> getColonnessources() {
        return colonnessources;
    }

    public void setColonnessources(List<String> colonnessources) {
        this.colonnessources = colonnessources;
    }

    public String getColonnecible() {
        return colonnecible;
    }

    public void setColonnecible(String colonnecible) {
        this.colonnecible = colonnecible;
    }

    public String getTypejointure() {
        return typejointure;
    }

    public void setTypejointure(String typejointure) {
        this.typejointure = typejointure;
    }

    public String getClejointure() {
        return clejointure;
    }

    public void setClejointure(String clejointure) {
        this.clejointure = clejointure;
    }

    public String getTablesource() {
        return tablesource;
    }

    public void setTablesource(String tablesource) {
        this.tablesource = tablesource;
    }
}
