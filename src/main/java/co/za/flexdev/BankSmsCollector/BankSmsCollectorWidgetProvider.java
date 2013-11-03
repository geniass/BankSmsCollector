package co.za.flexdev.BankSmsCollector;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

public class BankSmsCollectorWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int i : appWidgetIds){
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);

            appWidgetManager.updateAppWidget(i, views);
        }
    }

}
