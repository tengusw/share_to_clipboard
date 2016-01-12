package com.tengu.sharetoclipboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Email;
import ezvcard.property.Telephone;

public class shareToClipboardActivity extends Activity {

    private static final String PLAIN_TEXT_TYPE = "text/plain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String scheme = intent.getScheme();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (PLAIN_TEXT_TYPE.equals(type))
                handleSendText(intent);
            else if (Contacts.CONTENT_VCARD_TYPE.equals(type))
                handleSendVCard(intent);
            else
                showToast(getString(R.string.error_type_not_supported));
        } else if ((Intent.ACTION_VIEW.equals(action) || Intent.ACTION_DIAL.equals(action))
                && (scheme.equals("tel") || scheme.equals("mailto"))) {
            handleSchemeSpecificPart(intent);
        }
        finish();
    }

    private void handleSchemeSpecificPart(Intent intent) {
        String dataString = "";
        Uri uri = intent.getData();

        if (uri != null) {
            dataString = uri.getSchemeSpecificPart();
        }

        if (dataString.length() > 0) {
            copyToClipboard(dataString);
        } else {
            //If no scheme retrieved, try get it as send intent
            handleSendText(intent);
        }

    }

    private void handleSendVCard(Intent intent) {
        Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
        InputStream stream = null;
        ContentResolver cr = getContentResolver();
        try {
            stream = cr.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showToast(getString(R.string.error_no_data));
            return;
        }

        StringBuffer fileContent = new StringBuffer("");
        int ch;
        try {
            while ((ch = stream.read()) != -1) {
                fileContent.append((char) ch);
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            showToast(getString(R.string.error_no_data));
            return;
        }
        VCard vcard = Ezvcard.parse(new String(fileContent)).first();
        String fullName = vcard.getFormattedName().getValue();
        String phone = "";
        for (Telephone telephone : vcard.getTelephoneNumbers()) {
            phone += arrayToString(telephone.getTypes().toArray()) + ": " + telephone.getText() + "\n";
        }
        String emailString = "";
        for (Email email : vcard.getEmails()) {
            emailString += arrayToString(email.getTypes().toArray()) + ": " + email.getValue() + "\n";
        }
        copyToClipboard(fullName + "\n" + phone + emailString);

    }

    private String arrayToString(Object[] objectArray) {
        String return_value = "";
        for (Object value : objectArray) {
            if (!value.toString().equals("pref"))
                return_value += value.toString().substring(0, 1).toUpperCase() + value.toString().substring(1).toLowerCase();
        }
        if (return_value.equals(""))
            return_value = getString(R.string.other);
        return return_value;
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT);

        if (sharedText != null) {
            copyToClipboard(sharedText);
        } else if (sharedTitle != null) {
            copyToClipboard(sharedTitle);
        } else {
            showToast(getString(R.string.error_no_data));
        }
    }

    @SuppressLint("NewApi")
    private void copyToClipboard(String clipboardText) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(clipboardText);
        } else {
            ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(this.CLIPBOARD_SERVICE);
            ClipData clip = ClipData
                    .newPlainText(
                            "text", clipboardText);
            clipboard.setPrimaryClip(clip);
        }
        showToast(getString(R.string.copied));
    }

}
