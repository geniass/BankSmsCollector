
package co.za.flexdev.BankSmsCollector;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

/*
 * Compares the currencies. If they differ the money is converted to the
 * given currency using an exchange rate from the internet. Date is the date
 * of the transaction, so the correct exchange rate can be found.
 */
class LongRunningGetIO extends AsyncTask<PurchaseDetails, Void, ArrayList<Money>> {

    PurchasesLoadedListener listener;
    Context mContext;

    public LongRunningGetIO(Context context, PurchasesLoadedListener l) {
        this.mContext = context;
        this.listener = l;
    }

    protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException,
            IOException {
        InputStream in = entity.getContent();

        StringBuffer out = new StringBuffer();
        int n = 1;
        while (n > 0) {
            byte[] b = new byte[4096];
            n = in.read(b);

            if (n > 0)
                out.append(new String(b, 0, n));
        }

        return out.toString();
    }

    protected BigDecimal fetchExchangeRate(String fromCurrencyCode, String toCurrencyCode,
            Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpGet httpGet = new HttpGet("http://openexchangerates.org/api/historical/"
                + format.format(date) + ".json?app_id=b851292811f2474e850438848ec4a399");
        Log.d("sdgfsagas", "http://openexchangerates.org/api/historical/" + format.format(date)
                + ".json?app_id=b851292811f2474e850438848ec4a399");

        try {
            HttpResponse response = httpClient.execute(httpGet, localContext);

            HttpEntity entity = response.getEntity();

            JSONObject jSONObject = new JSONObject(getASCIIContentFromEntity(entity));
            JSONObject rates = jSONObject.getJSONObject("rates");

            Log.d("Rates1", Double.toString(rates.getDouble(toCurrencyCode)));
            Log.d("Rates2", Double.toString(rates.getDouble(fromCurrencyCode)));
            BigDecimal conversionRate = (BigDecimal.valueOf(rates.getDouble(toCurrencyCode))
                    .setScale(4, RoundingMode.HALF_UP))
                    .divide((BigDecimal.valueOf(rates.getDouble(fromCurrencyCode)).setScale(4,
                            RoundingMode.HALF_UP)));
            Log.d("ConversionRate", conversionRate.toPlainString());

            return conversionRate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected ArrayList<Money> doInBackground(PurchaseDetails... purchases) {
        SmsSQLiteHelper helper = OpenHelperManager.getHelper(mContext, SmsSQLiteHelper.class);

        Calendar today = Calendar.getInstance();

        // TODO: hard-coded for now, make it a preference
        CurrencyUnit currency = CurrencyUnit.getInstance("ZAR");
        Money monthly_total = Money.zero(currency);
        Money total = Money.zero(currency);

        for (PurchaseDetails purchase : purchases) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(purchase.getDate());

            Log.d("Size", Integer.toString(purchases.length));
            Money money = Money.of(CurrencyUnit.getInstance(purchase.getCurrency()),
                    purchase.getAmount());

            if (!money.getCurrencyUnit().equals(currency)) {
                // get exchange rate
                BigDecimal conversionMultipler = fetchExchangeRate(money.getCurrencyUnit()
                        .getCurrencyCode(), currency.getCurrencyCode(), purchase.getDate());
                money = money.convertedTo(currency, conversionMultipler, RoundingMode.HALF_UP); // arbitrary

                // update the purchase in db so it doesn't need to convert again
                purchase.setAmount(money.getAmount());
                purchase.setCurrency(money.getCurrencyUnit().getCurrencyCode());
                try {
                    helper.getPurchaseDetailsDao().update(purchase);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            total = total.plus(money);

            if (today.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    && today.get(Calendar.MONTH) == cal.get(Calendar.MONTH)) {
                monthly_total = monthly_total.plus(money);
                Log.d("Monthly", purchase.getSeller() + money.toString());
            }
        }

        if (helper != null) {
            OpenHelperManager.releaseHelper();
            helper = null;
        }

        ArrayList<Money> totals = new ArrayList<Money>();
        totals.add(monthly_total);
        totals.add(total);
        return totals;
    }

    protected void onPostExecute(ArrayList<Money> results) {
        if (results != null) {
            Log.d("Results", results.get(0).toString());
            Log.d("Results", results.get(1).toString());

            listener.onPurchasesLoaded(results);
        }
    }

    public interface PurchasesLoadedListener {
        public void onPurchasesLoaded(ArrayList<Money> results);
    }
}
