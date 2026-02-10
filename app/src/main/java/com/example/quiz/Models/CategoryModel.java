package com.example.quiz.Models;

public class CategoryModel {
    private String docID;
    private String name;
    private int noOfTests;


    public CategoryModel(String docID, int noOfTests, String name) {
        this.docID = docID;
        this.noOfTests = noOfTests;
        this.name = name;
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
