
package co.za.flexdev.BankSmsCollector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.money.Money;

import android.util.Log;

public class SmsParser {

    static String TAG = "SmsParser";

    // TODO: make these regexs editable
    static String DATE_PATTERN = "\\d+/\\d+/\\d+";
    static String AMOUNT_PATTERN = "(?<=for )(.*)(?= on)";
    static String SELLER_PATTERN = "(?<=at )(.*)(?=\\.)";

    static Pattern datePattern = Pattern.compile(DATE_PATTERN);
    static Pattern amountPattern = Pattern.compile(AMOUNT_PATTERN);
    static Pattern sellerPattern = Pattern.compile(SELLER_PATTERN);
    static Pattern currencyPattern = Pattern.compile("(^[A-Z]+)(\\d+.\\d+)");

    static Matcher matcher;

    public static ArrayList<PurchaseDetails> parseInvestecSmsArrayList(
            ArrayList<SmsParcelable> messages) {
        ArrayList<PurchaseDetails> purchases = new ArrayList<PurchaseDetails>();

        for (SmsParcelable message : messages) {
            purchases.add(parseInvestecSms(message));
        }

        return purchases;
    }

    public static PurchaseDetails parseInvestecSms(SmsParcelable message) {
        Money amount = null;
        Date date = null;
        String seller = null;

        matcher = datePattern.matcher(message.getMessage());
        while (matcher.find()) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            try {
                date = format.parse(matcher.group());
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        matcher = amountPattern.matcher(message.getMessage());
        while (matcher.find()) {
            // TODO: instead of replacing 'R', maybe make a patch for joda
            // to parse the symbol
            //TODO: hacky regex's because joda fails if there's no space 
            // between currency and value
            String group = matcher.group().replaceAll("^R", "ZAR ");
            Matcher q = currencyPattern.matcher(group);
            if(q.matches()){
                group = group.replaceAll("(^[A-Z]+)", "$1 ");
            }
                    
            amount = Money.parse(group);
            Log.d(TAG, amount.toString());
        }

        matcher = sellerPattern.matcher(message.getMessage());
        while (matcher.find()) {
            seller = matcher.group();
            Log.d(TAG, matcher.group());
        }

        if (amount != null && date != null && seller != null) {
            return new PurchaseDetails(date, amount.getAmount(), seller, amount
                    .getCurrencyUnit().getCurrencyCode(), message.getTimestamp());
        }
        else {
            return null;
        }
    }

    public static boolean isValidSms(String message) {
        // TODO: BAD!! make more general with parser etc
        return message.contains("Purchase authorised") && message.contains("Investec");
    }
}
