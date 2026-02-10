package com.example.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.quiz.Models.QuestionModel;

import java.util.List;
/**
 * MultiSelectFragment
 *
 * Purpose:
 * This fragment displays and manages multi-select quiz questions, allowing users
 * to select multiple answers using checkboxes. It keeps the question state in sync
 * with user interactions and reports changes back to the parent activity.
 *
 * Why this fragment is used:
 * - Represents one type of quiz question (multi-select) in a reusable way.
 * - Allows different question types to be swapped dynamically within the same activity.
 * - Uses checkboxes to support multiple simultaneous selections.
 * - Keeps question logic separate from the activity that controls quiz flow.
 *
 * How it works:
 * 1. The fragment is created using newInstance(), which receives a QuestionModel
 *    and the question index via arguments.
 * 2. When the view is created, the fragment binds the question text and options
 *    from the QuestionModel to the UI elements.
 * 3. CheckBoxes are used for each answer option, enabling multiple selections.
 * 4. Previously selected answers are restored by checking the saved selections
 *    in the QuestionModel, ensuring state consistency when navigating between questions.
 * 5. A shared click listener handles checkbox toggling:
 *    - Selected options are added to the QuestionModel.
 *    - Deselected options are removed from the QuestionModel.
 * 6. The question’s status is updated based on the current selections
 *    (UNANSWERED or ANSWERED).
 * 7. The fragment notifies the parent QuestionsActivity of changes so that
 *    navigation indicators and progress UI remain up to date.
 */



public class MultiSelectFragment extends Fragment {

    private QuestionModel question;
    private int questionIndex;

    public static MultiSelectFragment newInstance(QuestionModel question, int index) {
        MultiSelectFragment fragment = new MultiSelectFragment();
        Bundle args = new Bundle();
        args.putSerializable("question_model", question);
        args.putInt("question_index", index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = (QuestionModel) getArguments().getSerializable("question_model");
            questionIndex = getArguments().getInt("question_index");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_select, container, false);

        TextView questionText = view.findViewById(R.id.question_text);
        CheckBox optionA = view.findViewById(R.id.optionA);
        CheckBox optionB = view.findViewById(R.id.optionB);
        CheckBox optionC = view.findViewById(R.id.optionC);
        CheckBox optionD = view.findViewById(R.id.optionD);

        questionText.setText(question.getQuestion());
        optionA.setText(question.getOptionA());
        optionB.setText(question.getOptionB());
        optionC.setText(question.getOptionC());
        optionD.setText(question.getOptionD());

        List<Integer> savedList = question.getSelectedAnsList();
        if (savedList.contains(1)) optionA.setChecked(true);
        if (savedList.contains(2)) optionB.setChecked(true);
        if (savedList.contains(3)) optionC.setChecked(true);
        if (savedList.contains(4)) optionD.setChecked(true);

        View.OnClickListener toggleCheckbox = v -> {
            CheckBox box = (CheckBox) v;
            int optionNum = -1;
            if (box == optionA) optionNum = 1;
            if (box == optionB) optionNum = 2;
            if (box == optionC) optionNum = 3;
            if (box == optionD) optionNum = 4;

            if (box.isChecked()) {
                question.addSelectedAns(optionNum);
            } else {
                question.removeSelectedAns(optionNum);
            }

            // ✅ Update status based on whether any options are selected
            if (question.getSelectedAnsList().isEmpty()) {
                question.setStatus(QuestionModel.UNANSWERED);
                ((QuestionsActivity) getActivity()).updateUI(questionIndex);
            } else {
                question.setStatus(QuestionModel.ANSWERED);
                ((QuestionsActivity) getActivity()).updateUI(questionIndex);
            }
            ((QuestionsActivity) getActivity()).getAdapter().notifyDataSetChanged();
        };

        optionA.setOnClickListener(toggleCheckbox);
        optionB.setOnClickListener(toggleCheckbox);
        optionC.setOnClickListener(toggleCheckbox);
        optionD.setOnClickListener(toggleCheckbox);

        return view;
    }
}
