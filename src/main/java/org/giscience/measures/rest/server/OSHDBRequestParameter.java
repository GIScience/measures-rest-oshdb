package org.giscience.measures.rest.server;

import org.heigit.bigspatialdata.oshdb.util.tagtranslator.OSMTag;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.OSMTagInterface;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.OSMTagKey;

public class OSHDBRequestParameter extends RequestParameter {
    public OSHDBRequestParameter(RequestParameter requestParameter) {
        super(requestParameter._context);
    }

    public OSMTagInterface getOSMTag() {
        return this.getOSMTag("key", "value");
    }

    public OSMTagInterface getOSMTag(String key) {
        return this.getOSMTag(key, null);
    }

    public OSMTagInterface getOSMTag(String key, String value) {
        if (value == null) return new OSMTagKey(this.get(key).toString());
        String strValue;
        try {
            strValue = this.get(value).toString();
        } catch (RequestParameterException e) {
            return new OSMTagKey(this.get(key).toString());
        }
        return new OSMTag(this.get(key).toString(), strValue);
    }
}
