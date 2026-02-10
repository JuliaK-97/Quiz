package com.example.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quiz.Models.QuestionModel;
/**
 * TrueFalseFragment
 *
 * Purpose:
 * This fragment displays and manages true/false quiz questions. It allows users
 * to select either True or False, updates the question state accordingly, and
 * notifies the parent activity of changes.
 *
 * Why this fragment is used:
 * - Represents the true/false question type as a reusable UI component.
 * - Keeps question UI and selection logic isolated from QuestionsActivity.
 * - Allows QuestionsActivity to support multiple question types without duplicating code.
 * - Provides a simple and clear interaction model using two toggle buttons.
 *
 * How it works:
 * 1. The fragment is created using newInstance(), which receives a QuestionModel
 *    and question index via arguments.
 * 2. The question text is displayed in a TextView.
 * 3. The fragment restores any previously saved selection:
 *    - If selected answer is True, the True button is activated.
 *    - If selected answer is False, the False button is activated.
 * 4. A shared click listener handles toggling:
 *    - Clicking the same button again clears the selection and sets the question status to UNANSWERED.
 *    - Clicking a different button sets the selected answer and updates the status to ANSWERED.
 * 5. The fragment notifies QuestionsActivity to update the UI and refresh the question navigation grid.
 */

public class TrueFalseFragment extends Fragment {

    private static final String ARG_QUESTION = "question_model";
    private QuestionModel question;

    // Track current selection
    private Button lastSelected = null;
    private int questionIndex;


    public TrueFalseFragment() {
        // Required empty public constructor
    }

    public static TrueFalseFragment newInstance(QuestionModel question, int index) {
        TrueFalseFragment fragment = new TrueFalseFragment();
        Bundle args = new Bundle();
        args.putSerializable("question_model", question);
        args.putInt("question_index", index);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = (QuestionModel) getArguments().getSerializable("question_model");
            questionIndex = getArguments().getInt("question_index");

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_true_false, container, false);

        TextView questionTextView = view.findViewById(R.id.question_text);
        Button trueButton = view.findViewById(R.id.true_button);
        Button falseButton = view.findViewById(R.id.false_button);

        if (question != null) {
            questionTextView.setText(question.getQuestion());
        }

        int savedAns = question.getSelectedAns();
        if (savedAns == 1) {
            trueButton.setActivated(true);
            lastSelected = trueButton;
        } else if (savedAns == 0) {
            falseButton.setActivated(true);
            lastSelected = falseButton;
        }

        View.OnClickListener toggleListener = v -> {
            Button clicked = (Button) v;

            if (clicked.equals(lastSelected)) {
                // Unselect if clicked again
                clicked.setActivated(false);
                question.setSelectedAns(-1);
                question.setStatus(QuestionModel.UNANSWERED);
                ((QuestionsActivity) getActivity()).updateUI(questionIndex);


                lastSelected = null;
            } else {
                // Save new selection
                if (clicked == trueButton) {
                    question.setSelectedAns(1); // 1 = True
                } else if (clicked == falseButton) {
                    question.setSelectedAns(0); // 0 = False
                }
                question.setStatus(QuestionModel.ANSWERED);
                if (lastSelected != null) lastSelected.setActivated(false);
                clicked.setActivated(true);
                lastSelected = clicked;
                ((QuestionsActivity) getActivity()).updateUI(questionIndex);

            }
            ((QuestionsActivity) getActivity()).getAdapter().notifyDataSetChanged();

        };


        trueButton.setOnClickListener(toggleListener);
        falseButton.setOnClickListener(toggleListener);

        return view;
    }
}
