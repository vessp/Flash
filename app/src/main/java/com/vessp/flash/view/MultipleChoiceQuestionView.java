package com.vessp.flash.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.vessp.flash.data.MultipleChoiceQuestion;
import com.vessp.flash.support.Ops;

import flash.vessp.com.flash.R;

public class MultipleChoiceQuestionView extends QuestionView implements View.OnClickListener
{
    public final MultipleChoiceQuestion mq;

    private Button input1;
    private Button input2;
    private Button correctInput;
    private Button incorrectInput;

    private double r = Math.random();

    public MultipleChoiceQuestionView(Context context, final MultipleChoiceQuestion mq, QuestionView.QuestionCallbacks callbacks)
    {
        super(context, mq, callbacks);
        this.mq = mq;

        LayoutInflater.from(getContext()).inflate(R.layout.transitive_question, this, true);

        input1 = (Button)findViewById(R.id.input1);
        input2 = (Button)findViewById(R.id.input2);

        input1.setOnClickListener(this);
        input2.setOnClickListener(this);

        setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (showingAnswer)
                    onComplete();
            }
        });

        findViewById(R.id.goButton).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                step();
            }
        });

        Ops.hideKeyboard(this);
    }

    @Override
    protected void drawState()
    {
        super.drawState();

        input1.setText(r < 0.5 ? k.getCorrectAnswer(q) : mq.getOtherAnswer(q));
        input2.setText(r < 0.5 ? mq.getOtherAnswer(q) : k.getCorrectAnswer(q));
        correctInput = r < 0.5 ? input1 : input2;
        incorrectInput = r < 0.5 ? input2 : input1;

        input1.setTextSize(40);
        input2.setTextSize(40);

        correctInput.setTextColor(showingAnswer ? getResources().getColor(R.color.normal_green) : getResources().getColor(R.color.normal_white));
        incorrectInput.setTextColor(showingAnswer ? getResources().getColor(R.color.normal_red) : getResources().getColor(R.color.normal_white));
    }

    @Override
    protected void onAnswerProvided(boolean maru)
    {
        super.onAnswerProvided(maru);
        input1.setClickable(false);
        input2.setClickable(false);
    }

    @Override
    protected void onAnswerFinalized()
    {
        super.onAnswerFinalized();

    }

    @Override
    public void onClick(View v)
    {
        onAnswerProvided(v == correctInput);
    }
}
