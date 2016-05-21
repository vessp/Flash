package com.vessp.flash.data;


import com.vessp.flash.support.Tracer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class FlashData
{
    public AttrMap getAttrMap() throws IllegalAccessException
    {
        AttrMap attrs = new AttrMap();
        for(Field f : getFields(this.getClass()))
        {
            f.setAccessible(true);
            if(attrs.containsKey(f.getName()))
                Tracer.e("duplicate attribute name detected, this could cause lose of data integrity");
            attrs.put(f.getName(), f.get(this));
        }
        return attrs;
    }

    public void readAttrs(AttrMap attrs) throws IllegalAccessException
    {
        for(Field f : getFields(this.getClass()))
        {
            f.setAccessible(true);
            if(attrs.containsKey(f.getName()))
            {
                Object value = attrs.get(f.getName());
                if(value != null)
                {
                    Class fieldType = f.getType();

                    if (fieldType.isEnum())
                    {
                        String sValue = value.toString();
//                        if (sValue.length() != 0)
                            f.set(this, Enum.valueOf(fieldType, sValue));
                    }
                    else if(fieldType.equals(Integer.TYPE))
                    {
                        f.set(this, Integer.parseInt(value.toString()));
                    }
                    else
                    {
                        f.set(this, value);
                    }
                }
            }
        }
    }

    public void readAttrs(AttrMap attrs, String prefix) throws IllegalAccessException
    {
        AttrMap prefixedAttrs = new AttrMap();
        for(String key : attrs.keySet())
        {
            if(key.startsWith(prefix))
            {
                String strippedKey = key.substring(prefix.length());
                strippedKey = strippedKey.substring(0, 1).toLowerCase() + strippedKey.substring(1);//lower case first letter
                prefixedAttrs.put(strippedKey, attrs.get(key));
            }
        }
        readAttrs(prefixedAttrs);
    }


    public static List<Field> getFields(Class c)
    {
        List<Field> fields = new ArrayList<>();
        while(true)
        {
            if(c == null)
                break;

            Field[] fs = c.getDeclaredFields();
            for(Field f : fs)
            {
                if(Modifier.isTransient(f.getModifiers()))
                    continue;

                fields.add(f);
            }

            c = c.getSuperclass();
        }
        return fields;
    }



    public void readJson(JSONObject json)
    {
        //override in subclass
        throw new UnsupportedOperationException();
    }

    public JSONObject toJson()
    {
        //override in subclass
        throw new UnsupportedOperationException();
    }

    public String toJString()
    {
        return toJson().toString();
    }

    public static JSONArray toJsonArray(List<? extends FlashData> list)
    {
        JSONArray jArr = new JSONArray();
        for (int i = 0; i < list.size(); i++)
        {
            jArr.put(list.get(i).toJson());
        }
        return jArr;
    }
}
