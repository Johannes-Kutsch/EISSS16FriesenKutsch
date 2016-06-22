package de.dtsharing.dtsharing;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

import de.dtsharing.dtsharing.AesCbcWithIntegrity.CipherTextIvMac;

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

}
