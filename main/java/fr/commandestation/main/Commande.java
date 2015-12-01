package fr.commandestation.main;


import java.io.FileNotFoundException;
import java.io.IOException;
import fr.commandestation.sms.SMS;
import fr.commandestation.station.Station;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import fr.commandestation.R;
import fr.commandestation.alarme.AlarmService;

public class Commande extends Activity {
	
	private Station station = new Station();
	private ImageView statusStation;
	private Intent intentAlarm;
	

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("LIFE_CYCLE","On Create Commande View");
		//Toast.makeText(getApplicationContext(), "onCreateCommande()", Toast.LENGTH_SHORT).show();

		byte[] serialStation = getIntent().getExtras().getByteArray("STATION");
		try {
			station.populate(serialStation);
		} catch (Exception e) {
			Log.e("SERIALIZE",e.toString());
		} 
		//On charge l'affichage commande.xml
		setContentView(R.layout.commande);
		//Affichage du titre de l'activit�
		CharSequence titre = station.getNom();	
		setTitle(titre);
		
		//Ajout des evenements sur le bouton demarrer et arreter
		eventsButton();

		//Init du smsReceiver
        registerReceiver(smsreceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        
        //Premier Appel pour connaitre l'etat de la station
		initStatusStation();

		
		//Set Text sur le numero de la station
		TextView txtNumStation = new TextView(this);
		txtNumStation = (TextView)findViewById(R.id.numStationVueCommande);
		CharSequence c = getString(R.string.numStationVueCommande) + " " + station.getNumeroTel();
		txtNumStation.setText(c);
		

	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Log.i("LIFE_CYCLE","On Start Commande View");
		//Toast.makeText(getApplicationContext(), "onStartCommande()", Toast.LENGTH_SHORT).show();
		TextView txtStateAlarm = (TextView)findViewById(R.id.stateAlarm);
		if (alarmIsSet())
			txtStateAlarm.setText("Alarme Active");
		else
			txtStateAlarm.setText("Alarme Inactive");
		
		//On affiche le status de la station
		updateStatusStation();
		alarm();
	}
	
	/**
	 * Permet de modifier l'image de status de la station ainsi que le texte
	 */
	private void updateStatusStation() {
		 
		 TextView txtViewState = (TextView)findViewById(R.id.textStatus);
		 
		 
		 statusStation = new ImageView(this);
		 statusStation = (ImageView)findViewById(R.id.imgStateStationVueCommande);
		 if (station.getStatus() == true)
		 {
		 	statusStation.setImageResource(R.drawable.statusok);
		 	txtViewState.setText(R.string.status_marche);
		 }
		 else if (station.getStatus() == false)
		 {
			 	statusStation.setImageResource(R.drawable.statusnonok);
			 	txtViewState.setText(R.string.status_arret);
		 }
	}

	/**
	 * On recupere le dernier message envoyer par la station et on init l'etat de la station
	 */
	private void initStatusStation() {
	     //SMS URI
        Uri uri = Uri.parse("content://sms");        
        try {

            ContentResolver contentResolver = getContentResolver();

			//Requested colonnes
			String[] reqCols = new String[] { "address", "body" };
			//Query to the contentProvider
			Cursor managedCursor = contentResolver.query(uri,reqCols,null,null,null);
            if (managedCursor.moveToFirst()) {
            	
            	do{
            		Log.i("MSG","Phone:"+managedCursor.getString(managedCursor.getColumnIndex("address"))+";Pattern:(\\+33|0)"+station.getNumeroTel().substring(1));
            		String num = managedCursor.getString(managedCursor.getColumnIndex("address"));
            		if (num.matches("(\\+33|0)"+station.getNumeroTel().substring(1)))
            			break;
            	}while (managedCursor.moveToNext());
            	
            	String state = managedCursor.getString(managedCursor.getColumnIndex("body"));
            	
            	if (station.getDemarrerNotif().equalsIgnoreCase(state))
            		station.setStatus(true);
            	else if (station.getArreterNotif().equalsIgnoreCase(state))
            		station.setStatus(false);
            }
        }
        catch (Exception e) {
            Log.w("GET_LAST_SMS", e.toString());
        }
		
        updateStatusStation();
	}


	private void eventsButton()
	{
		final Vibrator vibreur = (Vibrator)getApplication().getSystemService(Context.VIBRATOR_SERVICE);
    	((Button)findViewById(R.id.imgDemarrer)).setOnClickListener(new OnClickListener(){ 
			@Override
    		public void onClick(View v) {
				vibreur.vibrate(1000);
    			Intent i = new Intent(Commande.this,SMS.class);
    			station.setMsgType("START");
    			try {
					i.putExtra("STATION", station.serialize());
				} catch (IOException e) {
					Log.e("SERIALIZE", e.toString());
				}
    			startService(i);
    			//Toast.makeText(getApplicationContext(), R.string.envoie_msg_marche, Toast.LENGTH_SHORT).show();
    		}
    	});
    	
    	((Button)findViewById(R.id.imgArret)).setOnClickListener(new OnClickListener(){ 
			@Override
    		public void onClick(View v) {
				vibreur.vibrate(1000);
    			Intent i = new Intent(getApplicationContext(),SMS.class);
    			station.setMsgType("STOP");
    			try {
					i.putExtra("STATION", station.serialize());
				} catch (IOException e) {
					Log.e("SERIALIZE", e.toString());
				}
    			startService(i);
    			//Toast.makeText(getApplicationContext(), R.string.envoie_msg_arret, Toast.LENGTH_SHORT).show();
    		}
    	});
	}
	
	
	
	/**
	 * Creation d'une alarme
	 */
	private void alarm()
	  {
		  Button bAlarme = (Button)findViewById(R.id.bAlarm);		 

		  if (intentAlarm == null)
			  intentAlarm = new Intent(this,AlarmService.class);
		  	 	//intentAlarm = new Intent(Intent.ACTION_PICK);
		  
		  bAlarme.setOnClickListener(new OnClickListener() {
			
			//On demarre la nouvelle activit� alarme
			@Override
			public void onClick(View v) {
				try {
					intentAlarm.putExtra("STATION", station.serialize());
					startActivity(intentAlarm);
					
				} catch (IOException e) {
					Log.e("ERROR_SERIALIZE", e.toString());
				}
				
				
			}
		});
	  }
	

	/**
	 * Appel� lorsque l'activit� passe en arri�re plan
	 */
	@Override
	public void onPause() {
		super.onPause();
		Log.i("LIFE_CYCLE","On Pause Alarm View");
	}


	/**
	 * Appel� lors de la reprise de l'activit�
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("LIFE_CYCLE","On Destroy Alarm View");
		//On ferme le broadcast receiver lors de la destruction de l'activit�
		unregisterReceiver(smsreceiver); //Destruction du broadcastReceiver

	}
	  
	/**
	 * Appel de la fontion lors de la reception d'un nouveau message	 
	 * 
	 */
    private BroadcastReceiver smsreceiver = new BroadcastReceiver()
    {
            @Override
            public void onReceive(Context context, Intent intent)
            {
            Bundle bundle = intent.getExtras();        
            SmsMessage[] msgs = null;
            
            if(null != bundle)
            {

                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                
                for (int i=0; i<msgs.length; i++){
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                }
   
                //Toast.makeText(context, info + " Station " + station.getNumeroTel() +" Notif ok " +station.getDemarrerNotif(), Toast.LENGTH_SHORT).show();
                String num = msgs[0].getOriginatingAddress();
                Log.i("MSG_INSTA","PHONE:"+msgs[0].getOriginatingAddress()+";pattern:"+"(\\+33|0)"+station.getNumeroTel().substring(1));
                if (num.matches("(\\+33|0)"+station.getNumeroTel().substring(1)))
                {
                	Log.i("CONFIG","INFO:"+station.getDemarrerNotif()+":"+station.getArreterNotif());
                	Log.i("SMS_RECEIVER","MSG:"+msgs[0].getMessageBody());
	                if(station.getDemarrerNotif().equalsIgnoreCase(msgs[0].getMessageBody()))
	                {
	                	station.setStatus(true);
	                	Toast.makeText(context, R.string.status_marche, Toast.LENGTH_SHORT).show();
	                	updateStatusStation();
	                	Log.i("SMS_RECEIVER", "LISTENER : Station :"+station.getNom()+ " STATE:R.string.status_marche");
	                }
	                else if (station.getArreterNotif().equalsIgnoreCase(msgs[0].getMessageBody()))
	                {
	                	station.setStatus(false);
	                	Toast.makeText(context, R.string.status_arret, Toast.LENGTH_SHORT).show();
	                	updateStatusStation();
	                	Log.i("SMS_RECEIVER", "LISTENER : Station :"+station.getNom()+ " STATE:R.string.status_arret");
	                }
                }
                
            }                         
            }
    };
    
    /**
     * Test alarme is set
     */
    private boolean alarmIsSet()
    {
    	try {
			openFileInput("alarm"+station.getAlarmeId()+".serial");
			return true;
		} catch (FileNotFoundException e) {
			return false;
		}
    	
    }

	  


}