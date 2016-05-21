package com.vessp.flash.view;


import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vessp.flash.FlashApp;
import com.vessp.flash.WanaKana;
import com.vessp.flash.data.IKnowledge;
import com.vessp.flash.data.Translation;
import com.vessp.flash.data.Translation.GSubType;
import com.vessp.flash.support.TextAlertButton;
import com.vessp.flash.support.TextAlertButton.onEditedListener;
import com.vessp.flash.support.Tracer;

import junit.framework.Assert;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import flash.vessp.com.flash.R;

public class SingleTextQuestionView extends QuestionView
{
    public final Translation translation;

    private TextView kanji;
    private TextView shitsumon;
    private TextView kotae;
    private EditText gimon;
    private Button correct;

    private TextAlertButton notes;
    private Button voice;
    private Button known;
    private Button unknown;
    private Button fix;
    private Button go;


    final WanaKana wk;

    private boolean inSafeZone = true;

    public SingleTextQuestionView(Context context, final Translation translation, QuestionView.QuestionCallbacks callbacks)
    {
        super(context, translation, callbacks);
        this.translation = translation;

        LayoutInflater.from(getContext()).inflate(R.layout.single_text_question, this, true);

        kanji = (TextView) findViewById(R.id.kanji);
        shitsumon = (TextView) findViewById(R.id.shitsumon);
        kotae = (TextView) findViewById(R.id.kotae);

        gimon = (EditText) findViewById(R.id.gimon);
        gimon.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        wk = new WanaKana(gimon, false);
//        wk.bind();
        notes = (TextAlertButton)findViewById(R.id.notes);
        try
        {
            notes.bindToField(translation, translation.getClass().getField("notes"), new onEditedListener()
            {
                @Override
                public void onEdit(Object o, Field f)
                {
                    FlashApp.inst().saveKnowledge((IKnowledge) o);
                }
            });
        } catch (Exception e)
        {
            Tracer.e(e);
        }

        //forcing correct answer input
//        gimon.addTextChangedListener(new TextWatcher()
//        {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after)
//            {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count)
//            {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s)
//            {
//                if (showingAnswer && !markedMaru && currentTextMatchesAnswer())
//                    onComplete();
//            }
//        });

        findViewById(R.id.goButton).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                proceed();
            }
        });

        correct = (Button) findViewById(R.id.correct);
        correct.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onAnswerProvided(!markedMaru);
            }
        });

        voice = (Button) findViewById(R.id.voice);
        voice.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//                startActivityForResult(intent, ShikenActivity.VOICE_REQUEST_CODE);

            }
        });

        known = (Button) findViewById(R.id.known);
        known.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                proceed(true);
            }
        });

        fix = (Button) findViewById(R.id.fix);
        fix.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onAnswerProvided(!markedMaru);
            }
        });

        go = (Button) findViewById(R.id.go);
        go.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Assert.assertEquals(true, showingAnswer);
                proceed();
            }
        });

        unknown = (Button) findViewById(R.id.unknown);
        unknown.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onAnswerProvided(false);
            }
        });
    }

    private boolean currentTextMatchesAnswer()
    {
        String text = gimon.getText().toString();
//        return text.equals(k.getCorrectAnswer(q));// || wk.toKana(text).equals(translation.kana);
        return text.length() > 1 && k.isCorrectAnswer(q, text);
    }

    private void proceed()
    {
        if(inSafeZone)
            return;

        if(!showingAnswer)
        {
            onAnswerProvided(currentTextMatchesAnswer());
        }
        else// if(markedMaru)
        {
            onAnswerFinalized();
            onComplete();
        }
    }

    private void proceed(boolean maru)
    {
        if(inSafeZone)
            return;

        if(!showingAnswer)
        {
            onAnswerProvided(maru);
        }
        else
        {
            onAnswerProvided(maru);
            onAnswerFinalized();
            onComplete();
        }
    }

    @Override
    protected void onAnswerProvided(boolean maru)
    {
        super.onAnswerProvided(maru);
        if(!maru)
            ((ScrollView)findViewById(R.id.scrollView)).fullScroll(FOCUS_DOWN);

        cancelTimer();
    }

    @Override
    protected void onAnswerFinalized()
    {
        super.onAnswerFinalized();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
//        gimon.requestFocus();

        postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                inSafeZone = false;
            }
        }, 1000);

        proceedTimer = new Timer();
        proceedTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                known.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        known.setText("" + (10 - timerCount));
                        if(timerCount == 10)
                        {
                            onAnswerProvided(false);
                            cancelTimer();
                        }
                        timerCount++;
                    }
                });

            }

        }, 0, 1000);
    }

    private void cancelTimer()
    {
        if(proceedTimer != null)
        {
            proceedTimer.purge();
            proceedTimer.cancel();
            proceedTimer = null;
        }
        timerCount = 0;
    }

    int timerCount = 0;
    Timer proceedTimer = null;


    @Override
    public void drawState()
    {
        super.drawState();
        String qText = translation.getQuestionText(q);
        String ansText = translation.getCorrectAnswer(q);

        ((TextView)findViewById(R.id.gType)).setText(translation.gType + " " + ((translation.gSubType!= GSubType.NONE)? translation.gSubType:""));

        kanji.setText(translation.kanji);
        shitsumon.setText(qText);
        shitsumon.setTextColor(getResources().getColor(translation.gType.colorRes));
        kotae.setText(ansText);

        kanji.setTextSize(30);//? activeQuestions.a:"");
        shitsumon.setTextSize(40);//(float) (200.0f / Math.pow(qtv.getText().length(), 0.75)));
        kotae.setTextSize(40);//(float) (200.0f / Math.pow(atv.getText().length(), 0.75)));

        kanji.setVisibility(showingAnswer ? View.VISIBLE : View.INVISIBLE);
        kotae.setVisibility(showingAnswer ? View.VISIBLE : View.INVISIBLE);
//        correct.setVisibility(showingAnswer ? View.VISIBLE : View.INVISIBLE);
        correct.setVisibility(View.INVISIBLE);

        unknown.setVisibility(showingAnswer ? View.GONE : View.VISIBLE);
        known.setVisibility(showingAnswer ? View.GONE : View.VISIBLE);
        fix.setVisibility(showingAnswer ? View.VISIBLE : View.GONE);
        go.setVisibility(showingAnswer ? View.VISIBLE : View.GONE);
    }
}
