package de.dtsharing.dtsharing;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;


/**
 * A simple {@link Fragment} subclass.
 */
public class CryptoFragment extends Fragment implements View.OnClickListener {

    private Button bEncrypt, bDecrypt;
    private EditText etInput, etEncrypted, etDecrypted;
    private RadioButton rbKey1, rbKey2;
    private AesCbcWithIntegrity.SecretKeys key1, key2;

    public CryptoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RelativeLayout v = (RelativeLayout) inflater.inflate(R.layout.fragment_crypto, container, false);

        bEncrypt = (Button) v.findViewById(R.id.bEncrypt);
        bDecrypt = (Button) v.findViewById(R.id.bDecrypt);
        etInput = (EditText) v.findViewById(R.id.etInput);
        etEncrypted = (EditText) v.findViewById(R.id.etEncrypted);
        etDecrypted = (EditText) v.findViewById(R.id.etDecrypted);
        rbKey1 = (RadioButton) v.findViewById(R.id.rbKey1);
        rbKey2 = (RadioButton) v.findViewById(R.id.rbKey2);

        /*Generiere Keys. Wird vorläufig hier gesichert. Sollte normalerweise im Keystore landen, da
        * unsicher*/
        try {
            key1 = AesCbcWithIntegrity.generateKey();
            key2 = AesCbcWithIntegrity.generateKey();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        bEncrypt.setOnClickListener(this);
        bDecrypt.setOnClickListener(this);

        return v;
    }

    private String encrypt(String clearText, AesCbcWithIntegrity.SecretKeys key){

        AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = null;

        try {
            cipherTextIvMac = AesCbcWithIntegrity.encrypt(clearText, key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        String ciphertextString = cipherTextIvMac.toString();

        return ciphertextString;
    }

    private String decrypt(String encryptedText, AesCbcWithIntegrity.SecretKeys key){

        String decryptedText = null;
        AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(encryptedText);

        try {
            decryptedText = AesCbcWithIntegrity.decryptString(cipherTextIvMac, key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return decryptedText;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.bEncrypt:

                String  clearText = etInput.getText().toString(),
                        encryptedText = "";

                if(clearText != "") {
                    if (rbKey1.isChecked()) {
                        Toast.makeText(getActivity().getBaseContext(), "KEY 1 SELECTED", Toast.LENGTH_SHORT).show();
                        encryptedText = encrypt(clearText, key1);
                    }else if (rbKey2.isChecked()) {
                        Toast.makeText(getActivity().getBaseContext(), "KEY 2 SELECTED", Toast.LENGTH_SHORT).show();
                        encryptedText = encrypt(clearText, key2);
                    }

                    etEncrypted.setText(encryptedText);
                }

                break;

            case R.id.bDecrypt:

                String decryptedText = "";
                encryptedText = "";
                encryptedText = etEncrypted.getText().toString();

                if(encryptedText != "") {
                    if (rbKey1.isChecked()) {
                        Toast.makeText(getActivity().getBaseContext(), "KEY 1 SELECTED", Toast.LENGTH_SHORT).show();
                        decryptedText = decrypt(encryptedText, key1);
                    }else if (rbKey2.isChecked()) {
                        Toast.makeText(getActivity().getBaseContext(), "KEY 2 SELECTED", Toast.LENGTH_SHORT).show();
                        decryptedText = decrypt(encryptedText, key2);
                    }

                    etDecrypted.setText(decryptedText);
                }

                break;

        }

    }
}
