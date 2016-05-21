package com.vessp.flash.data;

import java.util.HashMap;

public class AttrMap extends HashMap<String, Object>
{
    public void putAll(AttrMap attrs, String prefix)
    {
        AttrMap prefixedAttrs = new AttrMap();
        for(String key : attrs.keySet())
        {
            String camelKey = key.substring(0, 1).toUpperCase() + key.substring(1);
            prefixedAttrs.put(prefix + camelKey, attrs.get(key));
        }
        super.putAll(prefixedAttrs);
    }
}
