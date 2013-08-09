
package co.za.flexdev.BankSmsCollector;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        ArrayList<String> messages = new ArrayList<String>();
        for (Object pdu : pdus) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            String messageBody = message.getDisplayMessageBody();
            Toast.makeText(context, messageBody, Toast.LENGTH_LONG).show();

            // TODO: BAD!! make more general with parser etc
            if (messageBody.contains("Purchase authorised") && messageBody.contains("Investec")) {
                messages.add(messageBody);
            }
        }

        if (!messages.isEmpty()) {
            Intent serviceIntent = new Intent(context, SmsParserService.class);
            serviceIntent.putStringArrayListExtra("messages", messages);
            context.startService(serviceIntent);
        }
    }

}
