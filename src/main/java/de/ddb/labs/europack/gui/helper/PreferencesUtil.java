package de.ddb.labs.europack.gui.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Michael Büchner <m.buechner@dnb.de>
 */
public class PreferencesUtil {

    private final static Logger LOG = LoggerFactory.getLogger(PreferencesUtil.class);

    public static boolean putMap(Preferences pref, String key, HashMap<String, String> value) {

        try {
            String s = serialize(value);
            pref.put(key, s);
        } catch (IOException ex) {
            LOG.error("{}", ex.getMessage());
            return false;
        }
        return true;
    }

    public static HashMap<String, String> getMap(Preferences pref, String key) {
        try {
            final Object o = deserialize(pref.get(key, ""));
            return (HashMap<String, String>) o;
        } catch (IOException | ClassNotFoundException ex) {
            return new HashMap<>();
        }
    }

    private static String serialize(Serializable o) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(o);
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private static Object deserialize(String s) throws IOException, ClassNotFoundException {
        final byte[] data = Base64.getDecoder().decode(s);
        Object o;
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            o = ois.readObject();
        }
        return o;
    }

}

