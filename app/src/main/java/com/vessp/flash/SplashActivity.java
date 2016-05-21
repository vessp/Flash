package com.vessp.flash;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.vessp.flash.data.AttrMap;
import com.vessp.flash.data.Translation;
import com.vessp.flash.support.Alerts;
import com.vessp.flash.support.Tracer;
import com.vessp.flash.support.Tracer.TT;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new InitAppTask().execute();
    }

    private class InitAppTask extends AsyncTask<Void, Void, AsyncTaskResult<Integer>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected AsyncTaskResult<Integer> doInBackground(Void... arg0)
        {
            try
            {
                FlashApp app = FlashApp.inst();
                FlashDB fdb = new FlashDB(getApplicationContext());
                app.fdb = fdb;

//                fdb.dropTable(FlashDB.TABLE_NAME_TRANSLATIONS);
                if(!fdb.hasTable(FlashDB.TABLE_NAME_TRANSLATIONS))
                {
                    fdb.createTable(FlashDB.TABLE_NAME_TRANSLATIONS, new Translation().getAttrMap());
//                    Translation t = new Translation();
//                    t.kanji = "??";
//                    t.kana = "??";
//                    t.eigo = "to go";
//                    fdb.push(FlashDB.TABLE_NAME_TRANSLATIONS, t.getAttrMap());
                }
                List<Translation> translations = new ArrayList<>();
                List<AttrMap> attrsList = fdb.pullTableRows(FlashDB.TABLE_NAME_TRANSLATIONS);
                for(AttrMap attrs: attrsList)
                {
                    Translation t = new Translation();
                    t.readAttrs(attrs);
                    translations.add(t);
                }
                app.init(translations);

//                fdb.dropTable("Translations");
//                fdb.createTable("Translations", new Translation().getAttrMap());
//                for(Translation t :  translations)
//                {
//                    int id = t._id;
//                    t._id = -1;
//                    fdb.push("Translations", t.getAttrMap());
//                    t._id = id;
//                }

//                fdb.dropTable("TranslationsBackup");
//                fdb.createTable("TranslationsBackup", new NewTranslation().getAttrMap());
//                for(NewTranslation t :  newTranslations)
//                {
//                    t._id = -1;
//                    fdb.push("TranslationsBackup", t.getAttrMap());
//                }

            }
            catch (Exception e)
            {
                return new AsyncTaskResult<>(e);
            }
            return new AsyncTaskResult<>(1);
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Integer> result)
        {
            super.onPostExecute(result);
            if(result.hasException())
            {
                Tracer.e(result.getException(), TT.APP_INIT);
                Alerts.showInfoAlert(SplashActivity.this, "Error during startup", result.getException().getMessage(), new OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        finish();
                    }
                }, null);
            }
            else
            {
                Intent intent = new Intent(getApplicationContext(), ShikenActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onCancelled(AsyncTaskResult<Integer> result)
        {
            super.onCancelled(result);
        }
    }

    public class AsyncTaskResult<T>
    {
        private T result;
        private Exception e;

        public T getResult() {
            return result;
        }

        public Exception getException() {
            return e;
        }

        public boolean hasException()
        {
            return e != null;
        }

        public AsyncTaskResult(T result) {
            super();
            this.result = result;
        }

        public AsyncTaskResult(Exception e) {
            super();
            this.e = e;
        }
    }
}
