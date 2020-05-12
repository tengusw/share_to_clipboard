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

import com.tengu.sharetoclipboard.utils.NotificationUtil;
import com.tengu.sharetoclipboard.utils.PreferenceUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import ezvcard.parameter.VCardParameter;
import ezvcard.property.Email;
import ezvcard.property.Telephone;

public class ShareToClipboardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("text/")) {
                if (type.equals("text/plain")){
                    handleSendText(intent, R.string.error_no_data);
                } else if (type.equals("text/x-vcard")) {
                    handleSendVCard(intent);
                } else {
                    handleSendText(intent, R.string.error_type_not_supported);
                }
            }
        }
        finish();
    }

    private void handleSendVCard(Intent intent) {
        Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);

        VCard vcard;
        try {
            ContentResolver cr = getContentResolver();
            InputStream stream = cr.openInputStream(uri);
            VCardReader reader = new VCardReader(stream);
            vcard = reader.readNext();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showToast(getString(R.string.error_no_data));
            return;
        } catch (IOException e) {
            e.printStackTrace();
            showToast(getString(R.string.error_no_data));
            return;
        }

        StringBuilder output = new StringBuilder();

        String fullName = vcard.getFormattedName().getValue();
        output.append(fullName);
        output.append("\n");

        for (Telephone telephone : vcard.getTelephoneNumbers()) {
            appendVCardTypes(output, telephone.getTypes());
            output.append(": ");
            output.append(telephone.getText());
            output.append("\n");
        }

        for (Email email : vcard.getEmails()) {
            appendVCardTypes(output, email.getTypes());
            output.append(": ");
            output.append(email.getValue());
            output.append("\n");
        }

        copyToClipboard(output.toString());
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void appendVCardTypes(StringBuilder output, List <? extends VCardParameter> types) {
        if (types.isEmpty()) {
            output.append(getString(R.string.other));
        } else {
            for (int i = 0; i < types.size(); i++) {
                if (i > 0) output.append(",");
                output.append(capitalize(types.get(i).toString()));
            }
        }
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void handleSendText(Intent intent, int err_msg) {
        String text = getSendTextString(intent);
        if (text != null) {
            copyToClipboard(text);
        } else {
            showToast(getString(err_msg));
        }
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
