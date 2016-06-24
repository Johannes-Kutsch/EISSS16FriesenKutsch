package de.dtsharing.dtsharing;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import de.dtsharing.dtsharing.AesCbcWithIntegrity.CipherTextIvMac;


/* Eine Helper Klasse zur Nutzung der AesCbcWithIntegrity Klasse.
*  Diese wird genutzt um die Verschlüsselten Nachrichten des Chats zu entschlüsseln bzw neue Nachrichten zu verschlüsseln.
*  Ebenso wird bei der Erstellung der StringKey mitgegeben. Somit muss der StringKey nur 1x umgewandelt werden. Ebenso werden
*  die Try/Catches hierher ausgelagert, was in den Chat Parts für saubereren Code sorgt. */
public class ToznyHelper {

    AesCbcWithIntegrity.SecretKeys keys;
    String key, encryptedString, decryptedString;

    public ToznyHelper(String key){
        this.key = key;
        StringToKey();
    }

    public void encryptString (String message){

        CipherTextIvMac cipherTextIvMac = null;

        try {
            cipherTextIvMac = AesCbcWithIntegrity.encrypt(message, keys);
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        encryptedString = cipherTextIvMac.toString();

    }

    public void decryptString (String message){

        CipherTextIvMac cipherTextIvMac = new CipherTextIvMac(message);

        try {
            decryptedString = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

    }

    public void StringToKey (){

        try {
            keys = AesCbcWithIntegrity.keys(key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        Log.d("ToznyHelper", "ToznyHelper KEY: "+keys.toString());

    }

    public String getEncryptedString(){
        return encryptedString;
    }

    public String getDecryptedString(){
        return decryptedString;
    }

    public AesCbcWithIntegrity.SecretKeys getKeys(){
        return keys;
    }

}
