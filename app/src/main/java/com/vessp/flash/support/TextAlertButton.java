package com.vessp.flash.support;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.lang.reflect.Field;

public class TextAlertButton extends Button
{
    public TextAlertButton(Context context)
    {
        super(context);
        init();
    }

    public TextAlertButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public TextAlertButton(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

//    public TextAlertButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
//    {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    public interface onEditedListener
    {
        public void onEdit(Object o, Field f);
    }

    private void init()
    {

    }

    public void bindToField(final Object o, final Field f, final onEditedListener listener)
    {
        setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                    final EditText input = new EditText(v.getContext());
                    input.setHint(f.getName() + " field");
                    input.setText((String)f.get(o));
                    alert.setView(input);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            try
                            {
                                String value = input.getText().toString();
                                f.set(o, value);
                                if (listener != null)
                                    listener.onEdit(o, f);
                            } catch (Exception e)
                            {
                                Tracer.e(e);
                            }
                        }
                    });

                    alert.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int whichButton)
                                {
                                    dialog.cancel();
                                }
                            });
                    alert.show();

                    Ops.focusTextView(input);
                }
                catch (Exception e)
                {
                    Tracer.e(e);
                }
            }
        });
    }

}
