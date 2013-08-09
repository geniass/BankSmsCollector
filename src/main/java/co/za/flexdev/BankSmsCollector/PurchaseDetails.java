package co.za.flexdev.BankSmsCollector;

import java.math.BigDecimal;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="purchase_details")
public class PurchaseDetails {
    
    @DatabaseField(generatedId=true)
    int id;
    
    @DatabaseField
    Date date;
    
    @DatabaseField
    BigDecimal amount;
    
    @DatabaseField
    String seller;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public PurchaseDetails(int id, Date date, BigDecimal bigDecimal, String seller) {
        this.id = id;
        this.date = date;
        this.amount = bigDecimal;
        this.seller = seller;
    }

    public PurchaseDetails(){
        
    }

}
