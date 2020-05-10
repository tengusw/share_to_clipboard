package com.tengu.sharetoclipboard;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.widget.Toast;

import com.tengu.sharetoclipboard.Utils.NotificationUtil;
import com.tengu.sharetoclipboard.Utils.PreferenceUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Email;
import ezvcard.property.Telephone;

public class ShareToClipboardActivity extends Activity {

    private static final String PLAIN_TEXT_TYPE = "text/plain";
    private static final String RFC822_MESSAGE_TYPE = "message/rfc822";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String scheme = intent.getScheme();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (PLAIN_TEXT_TYPE.equals(type)) {
                if (!handleSendText(intent)) showToast(getString(R.string.error_no_data));
            } else if (Contacts.CONTENT_VCARD_TYPE.equals(type)) {
                handleSendVCard(intent);
            } else if (RFC822_MESSAGE_TYPE.equals(type)) {
                if (!handleRfc822Message(intent)) showToast(getString(R.string.error_no_data));
            } else if (!handleSendText(intent)) {
                showToast(getString(R.string.error_type_not_supported));
            }
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
        InputStream stream;
        ContentResolver cr = getContentResolver();
        try {
            stream = cr.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showToast(getString(R.string.error_no_data));
            return;
        }

        StringBuilder fileContent = new StringBuilder();
        try {
            int ch;
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

        StringBuilder output = new StringBuilder();

        String fullName = vcard.getFormattedName().getValue();
        output.append(fullName);
        output.append("\n");

        for (Telephone telephone : vcard.getTelephoneNumbers()) {
            output.append(arrayToString(telephone.getTypes().toArray()));
            output.append(": ");
            output.append(telephone.getText());
            output.append("\n");
        }

        for (Email email : vcard.getEmails()) {
            output.append(arrayToString(email.getTypes().toArray()));
            output.append(": ");
            output.append(email.getValue());
            output.append("\n");
        }

        copyToClipboard(output.toString());
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private String arrayToString(Object[] objectArray) {
        StringBuilder result = new StringBuilder();
        for (Object value : objectArray) {
            String s = value.toString();
            if (!s.equals("pref")) {
                result.append(capitalize(s));
            }
        }
        if (result.length() == 0) {
            result.append(getString(R.string.other));
        }
        return result.toString();
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private boolean handleSendText(Intent intent) {
        String text = getSendTextString(intent);
        if (text != null) copyToClipboard(text);
        return text != null;
    }

    private boolean handleRfc822Message(Intent intent) {
        if (!intent.getExtras().containsKey(Intent.EXTRA_EMAIL)) return handleSendText(intent);

        String[] emails = intent.getStringArrayExtra(Intent.EXTRA_EMAIL);
        if (emails.length == 0) return handleSendText(intent);

        String email = emails[0];
        for (int i = 1; i < emails.length; i++) {
            email = ", " + emails[i];
        }
        String text = getSendTextString(intent);
        String sharedText = email + (text != null ? "\n" + text : "");
        copyToClipboard(sharedText);
        return true;
    }

    private String getSendTextString(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        String sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (sharedText == null && sharedTitle == null) return null;
        if (sharedText != null) {
            if (sharedTitle != null &&
                    !sharedText.contains(sharedTitle) &&
                    PreferenceUtil.shouldShowTitle(this)) {
                sharedText = String.format("%s - %s", sharedTitle, sharedText);
            }
            return sharedText;
        } else {
            return sharedTitle;
        }
    }

    private void copyToClipboard(String clipboardText) {
        ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", clipboardText);
        clipboard.setPrimaryClip(clip);

        if (PreferenceUtil.shouldDisplayNotification(this)) {
            NotificationUtil.createNotification(this);
        }
    }
}
