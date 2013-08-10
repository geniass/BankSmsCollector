
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
        ArrayList<SmsParcelable> messages = new ArrayList<SmsParcelable>();
        for (Object pdu : pdus) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
            String messageBody = message.getDisplayMessageBody();

            if (SmsParser.isValidSms(messageBody)) {
                messages.add(new SmsParcelable(messageBody, message.getTimestampMillis()));
                Toast.makeText(context, messageBody, Toast.LENGTH_LONG).show();
            }
        }

        if (!messages.isEmpty()) {
            Intent serviceIntent = new Intent(context, SmsParserService.class);
            serviceIntent.putParcelableArrayListExtra("messages", messages);
            context.startService(serviceIntent);
        }
    }

}
