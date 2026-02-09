package com.example.quiz;

public class CategoryModel {
    private String docID;     // Firestore document ID
    private String name;      // Category name
    private int noOfTests;    // Number of tests in this category

    // Constructor matches how you call it in DbQuery
    public CategoryModel(String docID, int noOfTests, String name) {
        this.docID = docID;
        this.noOfTests = noOfTests;
        this.name = name;
    }

    // Getters and setters
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
