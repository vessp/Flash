package com.vessp.flash;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vessp.flash.data.IKnowledge;
import com.vessp.flash.data.IQuestion;
import com.vessp.flash.data.Translation;
import com.vessp.flash.support.Tracer;
import com.vessp.flash.view.QuestionView;
import com.vessp.flash.view.SingleTextQuestionView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import flash.vessp.com.flash.R;
import me.leolin.shortcutbadger.ShortcutBadger;


public class ShikenActivity extends ActionBarActivity implements QuestionView.QuestionCallbacks
{
    public static final int ADD_MONDAI_REQUEST_CODE = 100;
    public static final int EDIT_MONDAI_REQUEST_CODE = 101;
    public static final int VOICE_REQUEST_CODE = 102;

    private ViewGroup root;

    private Button nextTestButton;

    private List<IKnowledge> selectedItems;
    private QuestionView questionView;

    public FlashApp app()
    {
        return FlashApp.inst();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(!app().didInit())
        {
            startActivity(new Intent(getApplicationContext(), SplashActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.shiken_activity);

        root = (ViewGroup) findViewById(R.id.rootView);

        nextTestButton = (Button) findViewById(R.id.nextTest);
        nextTestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                app().loadDb();
                selectQuestions();
            }
        });

        selectQuestions();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //cycleActiveTestable();
        drawState();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
//        FlashApp.instance.persistQuestions(this);

        int count = 0;
        for(IKnowledge t : app().knowledges())
        {
            if(t.needsTesting())
                count++;
        }
        ShortcutBadger.with(this).count(count);

        if(questionView != null)
            questionView.saveProgress();
    }

    private void selectQuestions()
    {
        selectedItems = new ArrayList<>();
        for(IKnowledge t : app().knowledges())
        {
            if(t.needsTesting())
                selectedItems.add(t);
        }
        cycleActiveTestable();
        drawState();
    }

    public void cycleActiveTestable()
    {
        cleanQuestionView();

        if(selectedItems.size() == 0)
        {
            app().activeKnowledge(null);
            return;
        }

        Collections.sort(selectedItems);
        app().activeKnowledge(selectedItems.get(0));
    }

    private void redrawState()
    {
        cleanQuestionView();
        drawState();
    }

    private void drawState()
    {
        final IKnowledge activeKnowledge = app().activeKnowledge();
        if(activeKnowledge == null)
        {
            nextTestButton.setVisibility(View.VISIBLE);
        }
        else
        {
            nextTestButton.setVisibility(View.GONE);

            if(questionView == null || questionView.q != activeKnowledge.getPriorityQuestion())
            {
                cleanQuestionView();

                if(activeKnowledge instanceof Translation)
                {
                    questionView = new SingleTextQuestionView(getBaseContext(), (Translation) activeKnowledge, this);
                }
//                else if(activeKnowledge instanceof TransitiveLink)
//                {
//                    TransitiveLink transitiveLink = (TransitiveLink) activeKnowledge;
////                    if(transitiveLink.hasBoth())
//                        questionView = new MultipleChoiceQuestionView(getBaseContext(), transitiveLink, this);
////                    else
////                        questionView = new SingleTextQuestionView(getBaseContext(), transitiveLink.getOnlyWord(), this);
//                }
                root.addView(questionView);
            }
        }

        int doneCount = 0;
        int totalCount = 0;
        for(IKnowledge t : app().knowledges())
        {
            for(IQuestion q : t.getQuestions())
            {
                if(!q.needsTesting())
                    doneCount++;
                totalCount++;
            }
        }
        setTitle(doneCount + "/" + totalCount);
    }

    private void cleanQuestionView()
    {
        if(questionView != null)
        {
            root.removeView(questionView);
            questionView = null;
        }
    }

    //===Question callbacks=================================================================
    @Override
    public void onComplete(IKnowledge k)
    {
        cycleActiveTestable();
        drawState();
    }

    @Override
    public void onAnswered(IKnowledge k, IQuestion q, boolean maru)
    {
        q.markHit(maru);
        if(!k.needsTesting())
            selectedItems.remove(app().activeKnowledge());
        app().saveKnowledge(k);
    }
    //----------------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        switch (item.getItemId())
        {
            case R.id.action_jisho:
                app().openJisho(this);
                return true;
            case R.id.action_add:
                app().openTranslationEditor(this, false);
                return true;
            case R.id.action_edit:
                app().openTranslationEditor(this, true);
                return true;
            case R.id.action_share:
                try
                {
                    File backupFile = app().fdb().backup(this);

                    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"dwr169@gmail.com"});
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, backupFile.getName());
                    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(backupFile));
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                    this.startActivity(Intent.createChooser(emailIntent, "Share database file..."));
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_db:
                Intent dbmanager = new Intent(this, AndroidDatabaseManager.class);
                startActivity(dbmanager);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.v("MainActivity", data.getStringExtra(MondaiEditor.INTENT_FLAG_MONDAI_JSON));

        if(requestCode == ADD_MONDAI_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
//            Mondai m = Mondai.fromJString(data.getStringExtra(MondaiEditor.INTENT_FLAG_MONDAI_JSON));
//            FlashApp.instance.addTranslation(this, m);
        }
        if(requestCode == EDIT_MONDAI_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
//            Mondai m = Mondai.fromJString(data.getStringExtra(MondaiEditor.INTENT_FLAG_MONDAI_JSON));
//            activeQuestions.copyFrom(m);
//            FlashApp.instance.persistQuestions(this);
        }

        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Tracer.i(spokenText);
        }

        redrawState();
    }
}
