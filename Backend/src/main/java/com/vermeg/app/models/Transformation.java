package com.vermeg.app.models;

import java.util.List;

public class Transformation {

    Integer idtransformation;
    String tablesource;
    String tablecible;
    String idLigne;
    String typeidLigne;
    List<RichKey> richkeys;
    List<Document> documents;

    public Integer getIdtransformation() {
        return idtransformation;
    }

    public void setIdtransformation(Integer idtransformation) {
        this.idtransformation = idtransformation;
    }

    public String getTablesource() {
        return tablesource;
    }

    public void setTablesource(String tablesource) {
        this.tablesource = tablesource;
    }

    public String getTablecible() {
        return tablecible;
    }

    public void setTablecible(String tablecible) {
        this.tablecible = tablecible;
    }

    public String getIdLigne() {
        return idLigne;
    }

    public void setIdLigne(String idLigne) {
        this.idLigne = idLigne;
    }

    public String getTypeidLigne() {
        return typeidLigne;
    }

    public void setTypeidLigne(String typeidLigne) {
        this.typeidLigne = typeidLigne;
    }

    public List<RichKey> getRichkeys() {
        return richkeys;
    }

    public void setRichkeys(List<RichKey> richkeys) {
        this.richkeys = richkeys;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
