package com.example.quiz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QuestionModel implements Serializable {
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private List<Object> correctAnswers; // changed from List<?> to List<Object>
    private String questionType;
    private int selectedAns = -1;
    private List<Integer> selectedAnsList = new ArrayList<>();
    private String userAnswerText = null;

    public static final int NOT_VISITED = 0;
    public static final int UNANSWERED = 1;
    public static final int ANSWERED = 2;
    public static final int REVIEW = 3;
    private int status = NOT_VISITED;

    // Constructor
    public QuestionModel(String question,
                         String optionA,
                         String optionB,
                         String optionC,
                         String optionD,
                         List<Object> correctAnswers,
                         String questionType,
                         int status) {
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswers = correctAnswers;
        this.questionType = questionType;
        this.status = status;
    }

    // Getters
    public String getQuestion() { return question; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public List<Object> getCorrectAnswers() { return correctAnswers; }
    public String getQuestionType() { return questionType; }
    public int getSelectedAns() { return selectedAns; }
    public List<Integer> getSelectedAnsList() { return selectedAnsList; }
    public String getUserAnswerText() { return userAnswerText; }
    public int getStatus() { return status; }

    // Setters
    public void setSelectedAns(int selectedAns) { this.selectedAns = selectedAns; }
    public void setSelectedAnsList(List<Integer> selectedAnsList) { this.selectedAnsList = selectedAnsList; }
    public void setUserAnswerText(String userAnswerText) { this.userAnswerText = userAnswerText; }
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
                (selectedAnsList != null && !selectedAnsList.isEmpty()) ||
                (userAnswerText != null && !userAnswerText.trim().isEmpty());
    }

    public boolean isCorrect() {
        if (correctAnswers == null || correctAnswers.isEmpty()) return false;

        switch (questionType) {
            case "single_choice":
            case "true_false":
                if (selectedAns == -1) return false;
                Object correct = correctAnswers.get(0);
                if (correct instanceof Number) {
                    return ((Number) correct).intValue() == selectedAns;
                } else {
                    return correct.toString().equals(String.valueOf(selectedAns));
                }

            case "multi_select":
                if (selectedAnsList == null || selectedAnsList.isEmpty()) return false;
                List<Object> correctList = correctAnswers;
                return selectedAnsList.containsAll(correctList) && correctList.containsAll(selectedAnsList);

            case "short_answer":
                if (userAnswerText == null || userAnswerText.trim().isEmpty()) return false;
                String userAns = userAnswerText.trim().toLowerCase();
                String correctAns = correctAnswers.get(0).toString().trim().toLowerCase();
                return userAns.equals(correctAns);

            default:
                return false;
        }
    }

    public String getAnswerStatus() {
        if (!isAttempted()) return "unattempted";
        return isCorrect() ? "correct" : "wrong";
    }
}

