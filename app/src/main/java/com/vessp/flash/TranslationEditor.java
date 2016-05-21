package com.vessp.flash;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vessp.flash.data.IKnowledge;
import com.vessp.flash.data.Translation;
import com.vessp.flash.data.Translation.GSubType;
import com.vessp.flash.data.Translation.GType;
import com.vessp.flash.support.TextAlertButton;
import com.vessp.flash.support.TextAlertButton.onEditedListener;
import com.vessp.flash.support.Tracer;

import java.lang.reflect.Field;

import flash.vessp.com.flash.R;

public class TranslationEditor extends Activity
{
    public static final String INTENT_FLAG_EDIT = "INTENT_FLAG_EDIT";
//    public static final String INTENT_FLAG_MONDAI_JSON = "INTENT_FLAG_MONDAI_JSON";

//    Mondai m;

    public FlashApp app()
    {
        return FlashApp.inst();
    }

    public Translation activeTranslation()
    {
        return (Translation) app().activeKnowledge();
    }

    private boolean editActiveMondai;

    private ListView gTypeList;
    private ListView gSubTypeList;

    private boolean guessGType = true;
    private boolean guessGSubType = true;

    @TargetApi(VERSION_CODES.JELLY_BEAN)
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

        setContentView(R.layout.translation_editor);

        gTypeList = (ListView)findViewById(R.id.gTypeListView);
        gSubTypeList = (ListView)findViewById(R.id.gSubTypeListView);

        fillListWithEnum(gTypeList, GType.class);
        fillListWithEnum(gSubTypeList, GSubType.class);

        gTypeList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                guessGType = false;
            }
        });

        gSubTypeList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                guessGSubType = false;
            }
        });

        editActiveMondai = getIntent().getBooleanExtra(INTENT_FLAG_EDIT, false);
        if(editActiveMondai)
        {
            //safety check for active question type
            if(!(app().activeKnowledge() instanceof Translation))
            {
                finish();
                return;
            }

            ((TextView)findViewById(R.id.kanji)).setText(activeTranslation().kanji);
            ((TextView)findViewById(R.id.kana)).setText(activeTranslation().kana);
            ((TextView)findViewById(R.id.eigo)).setText(activeTranslation().eigo);
            gTypeList.setItemChecked(activeTranslation().gType.ordinal(), true);
            gSubTypeList.setItemChecked(activeTranslation().gSubType.ordinal(), true);
        }
        else
        {
            gTypeList.setItemChecked(0, true);
            gSubTypeList.setItemChecked(0, true);
        }

        ((TextView)findViewById(R.id.kanji)).addTextChangedListener(new TextWatcher()
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
                if (pause)
                    return;

                boolean allKana = true;
                for (int i = 0; i < s.length(); i++)
                {
                    Character.UnicodeBlock block = Character.UnicodeBlock.of(s.charAt(i));

                    if (block != Character.UnicodeBlock.HIRAGANA && block != Character.UnicodeBlock.KATAKANA)
                        allKana = false;
                }

                if (allKana)
                    ((TextView) findViewById(R.id.kana)).setText(((TextView) findViewById(R.id.kanji)).getText());
            }
        });

        ((TextView)findViewById(R.id.eigo)).addTextChangedListener(new TextWatcher()
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
                String text = ((TextView)findViewById(R.id.eigo)).getText().toString();

                GType gType = null;
                GSubType gSubType = null;

                if(text.length() == 0)
                {
                    gType = GType.NONE;
                    gSubType = GSubType.NONE;
                }
                else if(text.startsWith("to "))
                {
                    gType = GType.VERB;
                    if (text.startsWith("to be "))
                        gSubType = GSubType.INTRANS;
                    else
                        gSubType = GSubType.TRANS;
                }
//                else if(text.contains(" "))
//                {
//                    gType = GType.PHRASE;
//                    gSubType = GSubType.NONE;
//                }
                else
                {
                    gType = GType.NOUN;
                    gSubType = GSubType.NONE;
                }

                if(guessGType && gType != null)
                    gTypeList.setItemChecked(gType.ordinal(), true);

                if(guessGSubType && gSubType != null)
                    gSubTypeList.setItemChecked(gSubType.ordinal(), true);
            }
        });

        findViewById(R.id.done).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Translation t = editActiveMondai ? activeTranslation() : new Translation();
                t.kanji = ((TextView) findViewById(R.id.kanji)).getText().toString();
                t.kana = ((TextView) findViewById(R.id.kana)).getText().toString();
                t.eigo = ((TextView) findViewById(R.id.eigo)).getText().toString();
                t.gType = GType.values()[((ListView) findViewById(R.id.gTypeListView)).getCheckedItemPosition()];
                t.gSubType = GSubType.values()[((ListView) findViewById(R.id.gSubTypeListView)).getCheckedItemPosition()];

//                if (!editActiveMondai)
//                {
//                    app().addTranslation(t);
//                } else
//                {
//                    app().updateWord(t);
//                }
//                app().persistQuestions(TranslationEditor.this);
                app().saveKnowledge(t);

                Intent output = new Intent();
//                output.putExtra(INTENT_FLAG_MONDAI_JSON, m.toJString());
                setResult(RESULT_OK, output);
                finish();
            }
        });

        findViewById(R.id.erase).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                String s = ((TextView)findViewById(R.id.kanji))
                pause = true;
                ((TextView) findViewById(R.id.kanji)).setText("");
                pause = false;
            }
        });

        try
        {
            //TODO fix activeTranslation() is null case
            ((TextAlertButton)findViewById(R.id.notes)).bindToField(activeTranslation(), activeTranslation().getClass().getField("notes"), new onEditedListener()
            {
                @Override
                public void onEdit(Object o, Field f)
                {
                    app().saveKnowledge((IKnowledge) o);
                }
            });
        } catch (Exception e)
        {
            Tracer.e(e);
        }
    }

    boolean pause = false;

    @Override
    protected void onResume()
    {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(((TextView)findViewById(R.id.kanji)), InputMethodManager.SHOW_IMPLICIT);
    }

    private <E extends Enum> void fillListWithEnum(final ListView lv, final Class<E> e)
    {
        lv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item)
        {
            @Override
            public String getItem(int position)
            {

                return e.getEnumConstants()[position].toString();
            }

            @Override
            public int getCount()
            {
                return e.getEnumConstants().length;
            }
        });
    }
}
