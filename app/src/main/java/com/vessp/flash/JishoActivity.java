package com.vessp.flash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.vessp.flash.data.IKnowledge;
import com.vessp.flash.data.IQuestion;
import com.vessp.flash.support.Ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import flash.vessp.com.flash.R;

public class JishoActivity extends Activity
{
    public FlashApp app()
    {
        return FlashApp.inst();
    }

    private ListView listView;
    private EditText searchView;

    List<IKnowledge> knowledges;

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

        knowledges = app().knowledges();
        Collections.sort(knowledges);

        setContentView(R.layout.jisho_activity);

        listView = (ListView)findViewById(R.id.mondai_list);

        populateList(knowledges);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                app().activeKnowledge((IKnowledge) parent.getAdapter().getItem(position));
                app().openTranslationEditor(JishoActivity.this, true);
            }
        });

        searchView = (EditText) findViewById(R.id.search);
        searchView.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                List<IKnowledge> qs = knowledges;
                List<IKnowledge> passed = new ArrayList<>();
                for(IKnowledge q : qs)
                {
                    if(q.filterBy(s.toString()))
                        passed.add(q);
                }

                populateList(passed);
            }
        });
    }

    private void populateList(List<IKnowledge> qs)
    {
        listView.setAdapter(new QuestionAdapter(listView.getContext(), qs));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ShikenActivity.EDIT_MONDAI_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
//            Log.v("JishoActivity", data.getStringExtra(MondaiEditor.INTENT_FLAG_MONDAI_JSON));
//            Mondai m = Mondai.fromJString(data.getStringExtra(MondaiEditor.INTENT_FLAG_MONDAI_JSON));
//            FlashApp.instance.editingMondai.copyFrom(m);
//            FlashApp.instance.persistQuestions(this);
//            FlashApp.instance.editingMondai = null;
        }
    }

    public class QuestionAdapter extends ArrayAdapter<IKnowledge>
    {
        public QuestionAdapter(Context context, List<IKnowledge> list)
        {
            super(context, 0, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final IKnowledge k = getItem(position);
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.jisho_item, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.kana)).setText(k.getKnowledgeSnippet());
//            ((TextView) convertView.findViewById(R.id.eigo)).setText(m.eigo);

            String scoreSnippet = "";
            for(IQuestion q : k.getQuestions())
                scoreSnippet += q.getScoreSnippet() + "\n";
            ((TextView) convertView.findViewById(R.id.progress)).setText(scoreSnippet);

            convertView.setBackgroundColor(k.needsTesting() ? Color.parseColor("#bbff4444") : Color.parseColor("#bb44ff44"));
            ((TextView) convertView.findViewById(R.id.status)).setText(k.needsTesting() ? "      " : Ops.readableEstimate(k.getPriorityQuestion().timeTillTest()));

            return convertView;
        }
    }
}
