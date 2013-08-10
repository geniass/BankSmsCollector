
package co.za.flexdev.BankSmsCollector;

import java.math.BigDecimal;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "purchase_details")
public class PurchaseDetails {

    @DatabaseField
    Date date;

    @DatabaseField
    BigDecimal amount;

    @DatabaseField
    String seller;

    @DatabaseField
    String currency;

    @DatabaseField(id = true)
    long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public PurchaseDetails(Date date, BigDecimal bigDecimal, String seller, String currency,
            long timestamp) {
        this.date = date;
        this.amount = bigDecimal;
        this.seller = seller;
        this.currency = currency;
        this.timestamp = timestamp;
    }

    public PurchaseDetails() {

    }

}
