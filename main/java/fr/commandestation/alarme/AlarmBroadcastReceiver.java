package fr.commandestation.alarme;

import java.io.File;
import java.io.IOException;
import fr.commandestation.sms.SMS;
import fr.commandestation.station.Station;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;

public class AlarmBroadcastReceiver extends BroadcastReceiver{


	private Station station;
	private File f;

	@Override
	public void onReceive(Context context, Intent intent) {
		station = new Station();
		ContextWrapper x = new ContextWrapper(context);
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		byte[] serialStation = intent.getExtras().getByteArray("STATION");
		try {
			station.populate(serialStation);

			//Log.i("TEST","station "+station.getNom()+" ACTION " +station.getMsgType());
			f = x.getFileStreamPath("alarm"+station.getAlarmeId()+".serial");	
			
			notificationManager.cancel(station.getAlarmeId());
			

			if (f.delete())
				Log.i("REMOVE_FILE_BROADCAST","Remove File "+f.getAbsolutePath().toString());
			else
			{
				Log.e("REMOVE_FILE_BROADCAST","Remove File "+f.getAbsolutePath().toString());
				return;
			}

			Log.i("ACTION_BROADCAST",station.getMsgType());
			if (station.getMsgType().contains("START"))
			{
				Intent i = new Intent(context,SMS.class);
				try {
					i.putExtra("STATION", station.serialize());
					context.startService(i);
					Log.i("ALARM_ACTIVE", "Listener Alarm : STATION :"+station.getNom()+" MESSAGE :"+station.getDemarrer());
				} catch (IOException e) {
					Log.e("SERIAL_ALARM_BROADCAST",e.toString());
				}

			}
			else if (station.getMsgType().contains("STOP"))
			{
				Intent i = new Intent(context,SMS.class);
				try {
					i.putExtra("STATION", station.serialize());
					context.startService(i);
					Log.i("ALARM_ACTIVE", "Listener Alarm : STATION :"+station.getNom()+" MESSAGE :"+station.getArreter());
				} catch (IOException e) {
					Log.e("SERIALIZE",e.toString());
				}

			}
			else
			{
				Log.e("ALARM_ACTIVE","Error Action "+station.getMsgType());
			}
		    
		} catch (Exception e) {
			Log.e("POPUL_ALARM_BROADCAST",e.toString() +" station "+station.getNom()+" ACTION " +station.getMsgType());
		} 
		
	}
	
	
}
