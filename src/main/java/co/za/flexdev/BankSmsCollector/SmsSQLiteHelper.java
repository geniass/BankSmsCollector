
package co.za.flexdev.BankSmsCollector;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class SmsSQLiteHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "bank-sms.db";
    private static final int DATABASE_VERSION = 3;

    private Dao<PurchaseDetails, Long> purchaseDetailsDao;

    public SmsSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils
                    .createTable(connectionSource, PurchaseDetails.class);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource,
            int oldVersion, int newVersion) {
        Log.w(SmsSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        try {
            TableUtils.dropTable(connectionSource, PurchaseDetails.class,
                    false);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        onCreate(database);
    }

    public Dao<PurchaseDetails, Long> getPurchaseDetailsDao() {
        if (purchaseDetailsDao == null) {
            try {
                purchaseDetailsDao = getDao(PurchaseDetails.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return purchaseDetailsDao;
    }

}
