
package co.za.flexdev.BankSmsCollector;

import android.os.Parcel;
import android.os.Parcelable;

public class SmsParcelable implements Parcelable {

    String message;
    long timestamp;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeLong(timestamp);
    }

    public SmsParcelable(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public SmsParcelable(Parcel in) {
        message = in.readString();
        timestamp = in.readLong();
    }

    public static final Parcelable.Creator<SmsParcelable> CREATOR = new Creator<SmsParcelable>() {

        public SmsParcelable[] newArray(int size) {
            return new SmsParcelable[size];
        }

        public SmsParcelable createFromParcel(Parcel source) {
            return new SmsParcelable(source);
        }
    };

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
