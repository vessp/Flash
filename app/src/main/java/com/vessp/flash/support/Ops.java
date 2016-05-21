package com.vessp.flash.support;


import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

public class Ops
{

    public static String readableEstimate(Duration dur)
    {
        long days = dur.getStandardDays();
        if(days > 0)
            return days + "d";

        long hours = dur.getStandardHours();
        if(hours > 0)
            return hours + "h";

        return dur.getStandardMinutes() + "m";
    }

    public static <T> List<T> asList(T[] arr)
    {
        List<T> list = new ArrayList<T>();
        for(T t : arr)
            list.add(t);
        return list;
    }

    public static void focusTextView(final EditText input)
    {
        input.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                input.requestFocus();
                input.setSelection(input.getText().length());
                InputMethodManager inputMethodManager = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                //toggleSoftInputFromWindow(input.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
            }
        }, 1);
    }

    public static void hideKeyboard(View v)
    {
        InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


}
