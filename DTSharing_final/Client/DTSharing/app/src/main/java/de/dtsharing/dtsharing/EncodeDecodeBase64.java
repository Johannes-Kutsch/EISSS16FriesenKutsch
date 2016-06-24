package de.dtsharing.dtsharing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class EncodeDecodeBase64 {

    /* http://stackoverflow.com/a/9768973
    * Methoden zum En- und Dekodieren von Bildern/Strings in einen String/Bilder
    * DTSharing wandelt Bilder in Strings um um diese zum Server zu schicken. Dieser kann diese dann als String in der MongoDB sichern.
    * Wird ein Bild benötigt erhält der Client dieses ebenso als String und muss es vor der Benutzung daraus eine Bitmap erstellen */

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}
