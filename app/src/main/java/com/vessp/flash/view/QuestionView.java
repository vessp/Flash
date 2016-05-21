package com.vessp.flash.view;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vessp.flash.data.IKnowledge;
import com.vessp.flash.data.IQuestion;

import flash.vessp.com.flash.R;

public abstract class QuestionView extends RelativeLayout
{
    public final IKnowledge k;
    public final IQuestion q;
    private QuestionCallbacks callbacks;

    protected boolean markedMaru = false;
    protected boolean showingAnswer = false;

    public interface QuestionCallbacks
    {
        void onComplete(IKnowledge k);
        void onAnswered(IKnowledge k, IQuestion q, boolean maru);
    }

    public QuestionView(Context context, final IKnowledge k, QuestionCallbacks callbacks)
    {
        super(context);
        this.k = k;
        this.q = k.getPriorityQuestion();
        this.callbacks = callbacks;

        setPadding(20, 20, 20, 20);
        setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        drawState();
    }

    protected void onAnswerProvided(boolean maru)
    {
        markedMaru = maru;
        showingAnswer = true;
        drawState();
    }

    protected void onAnswerFinalized()
    {
        callbacks.onAnswered(k, q, markedMaru);
    }

    protected void onComplete()
    {
        callbacks.onComplete(k);
    }

    protected void step()
    {
        if(!showingAnswer)
        {
            onAnswerProvided(false);
            onAnswerFinalized();
        }
        else
        {
            onComplete();
        }
    }

    protected void drawState()
    {
        ((TextView) findViewById(R.id.score)).setText(q.maruCount() + " / " + q.hitCount() + "  ( " + q.maruStreak() + " )");
        this.setBackgroundColor(showingAnswer ? (markedMaru ? getResources().getColor(R.color.maru_green) : getResources().getColor(R.color.batsu_red)) : Color.BLACK);
    }

    public void saveProgress()
    {
        //submit answer if possible
        if(showingAnswer)
            onAnswerFinalized();
    }
}
