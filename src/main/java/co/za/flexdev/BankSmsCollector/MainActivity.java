
package co.za.flexdev.BankSmsCollector;

import java.sql.SQLException;
import java.util.ArrayList;

import org.joda.money.Money;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import co.za.flexdev.BankSmsCollector.LongRunningGetIO.PurchasesLoadedListener;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class MainActivity extends Activity implements PurchasesLoadedListener {

    TextView monthly_textview, total_textview;

    private SmsSQLiteHelper databaseHelper = null;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // refresh ui
            loadPurchaseDetails();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);

        setContentView(R.layout.activity_main);

        monthly_textview = (TextView) findViewById(R.id.monthly_spent_textview);
        total_textview = (TextView) findViewById(R.id.total_spent_textview);

        loadPurchaseDetails();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("sms-service-finished"));
    }

    private void loadPurchaseDetails() {
        Dao<PurchaseDetails, Long> dao = ((SmsSQLiteHelper) getHelper()).getPurchaseDetailsDao();
        ArrayList<PurchaseDetails> purchases = null;
        try {
            purchases = (ArrayList<PurchaseDetails>) dao.queryForAll();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        setProgressBarIndeterminateVisibility(true);

        new LongRunningGetIO(this, this).execute(purchases.toArray(new PurchaseDetails[purchases
                .size()]));
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_messages_menuitem:
                refreshMessages();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void refreshMessages() {
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null,
                null, null);
        cursor.moveToFirst();

        ArrayList<SmsParcelable> messages = new ArrayList<SmsParcelable>();
        do {
            String message = cursor.getString(cursor.getColumnIndex("body"));
            if (SmsParser.isValidSms(message)) {
                messages.add(new SmsParcelable(message, cursor.getLong(cursor
                        .getColumnIndex("date_sent"))));
                Log.d("Date", cursor.getString(cursor.getColumnIndex("date_sent")));
            }
        } while (cursor.moveToNext());

        if (!messages.isEmpty()) {
            Intent serviceIntent = new Intent(this, SmsParserService.class);
            serviceIntent.putParcelableArrayListExtra("messages", messages);
            startService(serviceIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private SmsSQLiteHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper =
                    OpenHelperManager.getHelper(this, SmsSQLiteHelper.class);
        }
        return databaseHelper;
    }

    public void onPurchasesLoaded(ArrayList<Money> results) {
        monthly_textview.setText(results.get(0).toString());
        total_textview.setText(results.get(1).toString());
        setProgressBarIndeterminateVisibility(false);
    }
}
