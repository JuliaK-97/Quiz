package com.example.quiz.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuestionModel implements Serializable {
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    // For multiple choice / true-false
    private List<Integer> correctAnswers = new ArrayList<>();
    private String questionType;
    private String qID;

    public String getqID() {
        return qID;
    }

    public void setqID(String qID) {
        this.qID = qID;
    }

    private int selectedAns = -1; // for single choice / true-false
    private List<Integer> selectedAnsList = new ArrayList<>(); // for multi-select

    public static final int NOT_VISITED = 0;
    public static final int UNANSWERED = 1;
    public static final int ANSWERED = 2;
    public static final int REVIEW = 3;
    private int status = NOT_VISITED;
    private boolean isBookmarked;


    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    // Constructor
    public QuestionModel(String question,
                         String optionA,
                         String optionB,
                         String optionC,
                         String optionD,
                         List<Integer> correctAnswers,
                         String questionType,
                         int status, boolean isBookmarked, String qID) {
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswers = correctAnswers;
        this.questionType = questionType;
        this.status = status;
        this.isBookmarked = isBookmarked;
        this.qID = qID;
    }

    // Getters
    public String getQuestion() { return question; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public List<Integer> getCorrectAnswers() { return correctAnswers; }
    public String getQuestionType() { return questionType; }
    public int getSelectedAns() { return selectedAns; }
    public List<Integer> getSelectedAnsList() { return selectedAnsList; }
    public int getStatus() { return status; }

    // Setters
    public void setSelectedAns(int selectedAns) { this.selectedAns = selectedAns; }
    public void setSelectedAnsList(List<Integer> selectedAnsList) { this.selectedAnsList = selectedAnsList; }
    public void setStatus(int status) { this.status = status; }

    // Helpers for multi-select
    public void addSelectedAns(int ans) {
        if (!selectedAnsList.contains(ans)) {
            selectedAnsList.add(ans);
        }
    }

    public void removeSelectedAns(int ans) {
        selectedAnsList.remove(Integer.valueOf(ans));
    }

    // ✅ Evaluation Logic
    public boolean isAttempted() {
        return selectedAns != -1 ||
                (selectedAnsList != null && !selectedAnsList.isEmpty());
    }

    public boolean isCorrect() {
        switch (questionType) {
            case "single_choice":
            case "true_false":
                return selectedAns != -1 &&
                        correctAnswers != null &&
                        !correctAnswers.isEmpty() &&
                        correctAnswers.get(0) == selectedAns;

            case "multi_select":
                return selectedAnsList != null &&
                        !selectedAnsList.isEmpty() &&
                        correctAnswers != null &&
                        !correctAnswers.isEmpty() &&
                        selectedAnsList.containsAll(correctAnswers) &&
                        correctAnswers.containsAll(selectedAnsList);

            default:
                return false;
        }
    }

    public String getAnswerStatus() {
        if (!isAttempted()) return "unattempted";
        return isCorrect() ? "correct" : "wrong";
    }
}
