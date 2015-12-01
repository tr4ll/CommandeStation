package fr.commandestation.sms;

import fr.commandestation.station.Station;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import fr.commandestation.R;


/**
 * 
 * Permet d'envoyer un sms
 *
 */
public class SMS extends Service{
	private Station station;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		//Toast.makeText(getApplicationContext(), "onCreateService()", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{   

		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		station = new Station();
		try {
			station.populate(intent.getByteArrayExtra("STATION"));
		} catch (Exception e) {
			Log.e("POPULATE SMS",e.toString());
		} 
			
		sendSms();
		
		return START_STICKY;
	}
	

	public void sendSms() 
	{
		
		//On instancie la classe qui permet d'envoyer le message
		SmsManager message2 =  SmsManager.getDefault();
		try {
			if (station.getMsgType().contains("START"))
			{
				message2.sendTextMessage(station.getNumeroTel(), null,station.getDemarrer(),null, null);
				Log.i("SEND_MSG", "PHONE NUMBER : "+station.getNumeroTel()+" TEXT : "+station.getDemarrer());
				Toast.makeText(SMS.this, R.string.envoie_msg_marche, Toast.LENGTH_SHORT).show();
			}
			else if (station.getMsgType().contains("STOP"))
			{
				message2.sendTextMessage(station.getNumeroTel(), null,station.getArreter(),null, null);
				Log.i("SEND_MSG", "PHONE NUMBER : "+station.getNumeroTel()+" TEXT : "+station.getArreter());
				Toast.makeText(SMS.this, R.string.envoie_msg_arret, Toast.LENGTH_SHORT).show();
			}
			else if (station.getMsgType().contains("CONFIGURATION"))
			{
				String message = readXmlFile();
				message2.sendTextMessage(station.getNumeroTel(), null,message,null, null);
				Log.i("SEND_MSG", "PHONE NUMBER : "+station.getNumeroTel()+" TEXT CONF : "+message);
				Toast.makeText(SMS.this, R.string.envoie_msg_conf, Toast.LENGTH_SHORT).show();
			}
		}catch (Exception e) {
			Toast.makeText(SMS.this, R.string.error_send_sms, Toast.LENGTH_SHORT).show();
		}		
	}

	private String readXmlFile() throws Exception {
		String message = "";
		String delimiter = ",";
		//Recuperation des pr�f�rences
		SharedPreferences shP = PreferenceManager.getDefaultSharedPreferences(this);
		
		String allowPhone = shP.getString("phone_number", "ALLOW_PHONE");
		String actionStart = shP.getString("text_action_start", "START");
		String actionStop = shP.getString("text_action_stop", "STOP");
		String isStart = shP.getString("text_is_start", "IS_START");
		String isStop = shP.getString("text_is_stop", "IS_STOP");
		//TODO : voir comment configurer exactement la station (le format du SMS)
		if (!station.getTelAutorise().isEmpty())
		{
			message += allowPhone+"=";
			for (String tel : station.getTelAutorise())
			{
				message +=tel+delimiter;
			}
		}
		message += actionStart+"="+station.getDemarrer()+delimiter;
		message += actionStop+"="+station.getArreter()+delimiter;
		message += isStart+"="+station.getDemarrerNotif()+delimiter;
		message += isStop+"="+station.getArreterNotif()+delimiter;
		return message;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
