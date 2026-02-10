package com.example.quiz.Models;

public class RankModel {
    private int score;

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    private int overallScore;
    private int rank;
    private String name;


    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RankModel(String name, int score, int rank, int overallScore) {
        this.score = score;
        this.rank = rank;
        this.name = name;
        this.overallScore = overallScore;
    }
}
