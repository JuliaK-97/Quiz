package com.example.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.quiz.Models.QuestionModel;
/**
 * SingleChoiceFragment
 *
 * Purpose:
 * This fragment displays and manages single-choice quiz questions, allowing the
 * user to select exactly one answer using radio buttons. It updates the question
 * state and notifies the parent activity when selections change.
 *
 * Why this fragment is used:
 * - Represents single-choice questions in a reusable and isolated UI component.
 * - Allows the QuestionsActivity to support multiple question types without
 *   duplicating logic.
 * - Uses RadioButtons to enforce a single selection, which matches the question type.
 * - Keeps UI logic contained within the fragment while delegating quiz control
 *   to the parent activity.
 *
 * How it works:
 * 1. The fragment is created using newInstance(), which receives a QuestionModel
 *    and the question index through arguments.
 * 2. When the view is created, the fragment binds the question text and options
 *    from the QuestionModel to the UI elements.
 * 3. RadioButtons are used for each option, enforcing only one selected answer.
 * 4. If a selection already exists, the corresponding RadioButton is restored
 *    when the fragment loads.
 * 5. A shared click listener handles selection:
 *    - Selecting a RadioButton sets the chosen answer in the QuestionModel.
 *    - Clicking the same option again clears the selection and marks the question as UNANSWERED.
 * 6. The question’s status is updated based on the current selection.
 * 7. The fragment notifies QuestionsActivity to update the UI and refresh the
 *    question navigation grid.
 */


public class SingleChoiceFragment extends Fragment {
    private static final String ARG_QUESTION = "question_model";
    private QuestionModel question;
    private int questionIndex;
    private RadioButton lastChecked = null;




    public static SingleChoiceFragment newInstance(QuestionModel question, int index) {
        SingleChoiceFragment fragment = new SingleChoiceFragment();
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
       View view = inflater.inflate(R.layout.fragment_single_choice, container, false);

       TextView questionText = view.findViewById(R.id.question_text);
       RadioButton optionA = view.findViewById(R.id.optionA);
       RadioButton optionB = view.findViewById(R.id.optionB);
       RadioButton optionC = view.findViewById(R.id.optionC);
       RadioButton optionD = view.findViewById(R.id.optionD);

       questionText.setText(question.getQuestion());
       optionA.setText(question.getOptionA());
       optionB.setText(question.getOptionB());
       optionC.setText(question.getOptionC());
       optionD.setText(question.getOptionD());

       int savedAns = question.getSelectedAns();
       if (savedAns == 1) {
           optionA.setChecked(true);
           lastChecked = optionA;
       } else if (savedAns == 2) {
           optionB.setChecked(true);
           lastChecked = optionB;
       } else if (savedAns == 3) {
           optionC.setChecked(true);
           lastChecked = optionC;
       } else if (savedAns == 4) {
           optionD.setChecked(true);
           lastChecked = optionD;
       }

       View.OnClickListener toggleListener = v -> {
           RadioButton clicked = (RadioButton) v;
           if (clicked.equals(lastChecked)) {
               clicked.setChecked(false);
               question.setSelectedAns(-1);
               question.setStatus(QuestionModel.UNANSWERED);
               ((QuestionsActivity) getActivity()).updateUI(questionIndex);
               lastChecked = null;
           } else {
               clicked.setChecked(true);
               if (clicked == optionA) question.setSelectedAns(1);
               if (clicked == optionB) question.setSelectedAns(2);
               if (clicked == optionC) question.setSelectedAns(3);
               if (clicked == optionD) question.setSelectedAns(4);
               question.setStatus(QuestionModel.ANSWERED);

               lastChecked = clicked;
               ((QuestionsActivity) getActivity()).updateUI(questionIndex);
           }

           ((QuestionsActivity) getActivity()).getAdapter().notifyDataSetChanged();

       };

       optionA.setOnClickListener(toggleListener);
       optionB.setOnClickListener(toggleListener);
       optionC.setOnClickListener(toggleListener);
       optionD.setOnClickListener(toggleListener);

       return view;
   }


}



