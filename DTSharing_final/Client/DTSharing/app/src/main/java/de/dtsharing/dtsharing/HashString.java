package de.dtsharing.dtsharing;

import java.security.MessageDigest;

/*http://stackoverflow.com/a/17490344 (13.06.2016)*/
/* Ein String wird md5 gehashed und das Ergebnis returned.
 * Diese Funktion wird momentan nur beim Login und Registrieren verwendet um das Kennwort nicht im
 * Klartext zu speichern */
public class HashString {

    public static final String md5(final String toEncrypt) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toEncrypt.getBytes("UTF-8"));
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            return sb.toString().toLowerCase();
        } catch (Exception exc) {
            return "";
        }
    }

}
