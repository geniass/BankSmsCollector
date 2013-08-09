
package co.za.flexdev.BankSmsCollector;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.money.CurrencyUnit;
import org.joda.money.CurrencyUnitDataProvider;
import org.joda.money.Money;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.IntentService;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

/*
 * Parses each sms for data. 
 * Assumes all sms's in bundle are valid purchase sms's
 */
public class SmsParserService extends IntentService {

    String TAG = "SmsParserService";

    // TODO: make these regexs editable
    String DATE_PATTERN = "\\d+/\\d+/\\d+";
    String AMOUNT_PATTERN = "(?<=for )(.*)(?= on)";
    String SELLER_PATTERN = "(?<=at )(.*)(?=\\.)";

    private SmsSQLiteHelper databaseHelper = null;

    public SmsParserService() {
        super("SmsParserService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<String> messages = intent.getStringArrayListExtra("messages");
        ArrayList<PurchaseDetails> purchases = new ArrayList<PurchaseDetails>();

        Pattern datePattern = Pattern.compile(DATE_PATTERN);
        Pattern amountPattern = Pattern.compile(AMOUNT_PATTERN);
        Pattern sellerPattern = Pattern.compile(SELLER_PATTERN);
        Pattern randReplacePattern = Pattern.compile("^R");

        Matcher matcher;

        for (String message : messages) {
            Money amount = null;
            Date date = null;
            String seller = null;

            matcher = datePattern.matcher(message);
            while (matcher.find()) {
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    date = format.parse(matcher.group());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            matcher = amountPattern.matcher(message);
            while (matcher.find()) {
                // TODO: instead of replacing 'R', maybe make a patch for joda
                // to parse the symbol
                amount = Money.parse(matcher.group().replaceAll("^R", "ZAR "));
                Log.d(TAG, amount.toString());
            }

            matcher = sellerPattern.matcher(message);
            while (matcher.find()) {
                seller = matcher.group();
                Log.d(TAG, matcher.group());
            }

            if (amount != null && date != null && seller != null) {
                purchases.add(new PurchaseDetails(-1, date, amount.getAmount(), seller));
            }
        }

        for (PurchaseDetails p : purchases) {
            try {
                getHelper().getPurchaseDetailsDao().create(p);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

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

}
