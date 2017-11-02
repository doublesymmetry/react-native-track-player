package guichaguri.trackplayer.logic;

import android.os.Bundle;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * !!! TEMPORARY !!!
 * Added this methods here while my PR isn't accepted
 * https://github.com/facebook/react-native/pull/15056
 * TODO REMOVE
 */
public class Temp {

    public static WritableArray fromList(List list) {
        WritableArray catalystArray = Arguments.createArray();
        for (Object obj : list) {
            if (obj == null) {
                catalystArray.pushNull();
            } else if (obj.getClass().isArray()) {
                catalystArray.pushArray(Arguments.fromArray(obj));
            } else if (obj instanceof Bundle) {
                catalystArray.pushMap(fromBundle((Bundle) obj));
            } else if (obj instanceof List) {
                catalystArray.pushArray(fromList((List) obj));
            } else if (obj instanceof String) {
                catalystArray.pushString((String) obj);
            } else if (obj instanceof Integer) {
                catalystArray.pushInt((Integer) obj);
            } else if (obj instanceof Number) {
                catalystArray.pushDouble(((Number) obj).doubleValue());
            } else if (obj instanceof Boolean) {
                catalystArray.pushBoolean((Boolean) obj);
            } else {
                throw new IllegalArgumentException("Unknown value type " + obj.getClass());
            }
        }
        return catalystArray;
    }

    public static WritableMap fromBundle(Bundle bundle) {
        WritableMap map = Arguments.createMap();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value == null) {
                map.putNull(key);
            } else if (value.getClass().isArray()) {
                map.putArray(key, Arguments.fromArray(value));
            } else if (value instanceof String) {
                map.putString(key, (String) value);
            } else if (value instanceof Number) {
                if (value instanceof Integer) {
                    map.putInt(key, (Integer) value);
                } else {
                    map.putDouble(key, ((Number) value).doubleValue());
                }
            } else if (value instanceof Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof Bundle) {
                map.putMap(key, fromBundle((Bundle) value));
            } else if (value instanceof List) {
                map.putArray(key, fromList((List) value));
            } else {
                throw new IllegalArgumentException("Could not convert " + value.getClass());
            }
        }
        return map;
    }

    @Nullable
    public static ArrayList toList(@Nullable ReadableArray readableArray) {
        if (readableArray == null) {
            return null;
        }

        ArrayList list = new ArrayList();

        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    list.add(null);
                    break;
                case Boolean:
                    list.add(readableArray.getBoolean(i));
                    break;
                case Number:
                    double number = readableArray.getDouble(i);
                    if (number == Math.rint(number)) {
                        // Add as an integer
                        list.add((int) number);
                    } else {
                        // Add as a double
                        list.add(number);
                    }
                    break;
                case String:
                    list.add(readableArray.getString(i));
                    break;
                case Map:
                    list.add(toBundle(readableArray.getMap(i)));
                    break;
                case Array:
                    list.add(toList(readableArray.getArray(i)));
                    break;
                default:
                    throw new IllegalArgumentException("Could not convert object in array.");
            }
        }

        return list;
    }

    @Nullable
    public static Bundle toBundle(@Nullable ReadableMap readableMap) {
        if (readableMap == null) {
            return null;
        }

        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

        Bundle bundle = new Bundle();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType readableType = readableMap.getType(key);
            switch (readableType) {
                case Null:
                    bundle.putString(key, null);
                    break;
                case Boolean:
                    bundle.putBoolean(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    // Can be int or double.
                    bundle.putDouble(key, readableMap.getDouble(key));
                    break;
                case String:
                    bundle.putString(key, readableMap.getString(key));
                    break;
                case Map:
                    bundle.putBundle(key, toBundle(readableMap.getMap(key)));
                    break;
                case Array:
                    bundle.putSerializable(key, toList(readableMap.getArray(key)));
                    break;
                default:
                    throw new IllegalArgumentException("Could not convert object with key: " + key + ".");
            }
        }

        return bundle;
    }

}