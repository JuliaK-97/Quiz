package com.example.quiz;

public class CategoryModel {
    private String docID;
    private String name;
    private int noOfTests;

    public CategoryModel(String name, int noOfTests, String docID) {
        this.name = name;
        this.noOfTests = noOfTests;
        this.docID = docID;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNoOfTests() {
        return noOfTests;
    }

    public void setNoOfTests(int noOfTests) {
        this.noOfTests = noOfTests;
    }
}
