
package co.za.flexdev.BankSmsCollector;

import java.sql.SQLException;
import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

/*
 * Parses each sms for data. 
 * Assumes all sms's in bundle are valid purchase sms's
 */
public class SmsParserService extends IntentService {

    static String TAG = "SmsParserService";

    private SmsSQLiteHelper databaseHelper = null;

    public SmsParserService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<SmsParcelable> messages = intent.getParcelableArrayListExtra("messages");

        ArrayList<PurchaseDetails> purchases = SmsParser.parseInvestecSmsArrayList(messages);

        for (PurchaseDetails p : purchases) {
            try {
                if (!getHelper().getPurchaseDetailsDao().idExists(p.getTimestamp())) {
                    getHelper().getPurchaseDetailsDao().create(p);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        sendMessage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    private SmsSQLiteHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper =
                    OpenHelperManager.getHelper(this, SmsSQLiteHelper.class);
        }
        return databaseHelper;
    }

    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("sms-service-finished");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
