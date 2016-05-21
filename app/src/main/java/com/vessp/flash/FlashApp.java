package com.vessp.flash;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.vessp.flash.data.IKnowledge;
import com.vessp.flash.data.Translation;
import com.vessp.flash.support.Tracer;
import com.vessp.flash.support.Tracer.TT;
import com.vessp.flash.support.TracerLogCat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlashApp extends Application
{
    public static final boolean USE_RAW_FILE = true;
//    public static final boolean USE_RAW_FILE = false;

    private static FlashApp instance;
    private boolean didInit = false;

//    public List<Translation> translations;
//    public List<TransitiveLink> transitiveLinks;

    private List<Translation> translations;

    private IKnowledge activeKnowledge;

    public FlashDB fdb;

    public FlashDB fdb()
    {
        return fdb;
    }

    public static FlashApp inst()
    {
        return instance;
    }

    public FlashApp()
    {
        instance = this;
        Tracer.attachLog(new TracerLogCat());
    }

    public void init(List<Translation> translations)
    {
        if(didInit)
            return;
        didInit = true;

        Collections.shuffle(translations);
        this.translations = translations;
    }

    public boolean didInit()
    {
        return didInit;
    }

    public Translation getTranslationFromId(int id)
    {
        for(Translation t : translations)
        {
            if(t.id() == id)
                return t;
        }
        return null;
    }

    public void openTranslationEditor(Activity context, boolean toEdit)
    {
        Intent i = new Intent(context, TranslationEditor.class);
        i.putExtra(TranslationEditor.INTENT_FLAG_EDIT, toEdit);
        context.startActivityForResult(i, toEdit ? ShikenActivity.EDIT_MONDAI_REQUEST_CODE : ShikenActivity.ADD_MONDAI_REQUEST_CODE);
    }

    public void openJisho(Activity context)
    {
        Intent i = new Intent(context, JishoActivity.class);
        context.startActivity(i);
    }

    public void saveKnowledge(IKnowledge k)
    {
        try
        {
            if(k instanceof Translation)
            {
                Translation t = (Translation) k;
                int newId = fdb.push(FlashDB.TABLE_NAME_TRANSLATIONS, t.getAttrMap());
                if (t._id == -1)
                {
                    if (newId == -1)
                        throw new Exception("why are both ids -1");

                    t._id = newId;
                    translations.add(t);
                }
            }
//            else if(translation instanceof TransitiveLink)
//            {
//                //TODO save this one
//            }
        } catch (Exception e)
        {
            Tracer.e(e, TT.FLASH_DB);
        }
    }

    public List<IKnowledge> knowledges()
    {
        List<IKnowledge> list = new ArrayList<>();
        list.addAll(translations);
        return list;
    }

    public IKnowledge activeKnowledge()
    {
        return activeKnowledge;
    }

    public void activeKnowledge(IKnowledge m)
    {
        this.activeKnowledge = m;
    }
}
