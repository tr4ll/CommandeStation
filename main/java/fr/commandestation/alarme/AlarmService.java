package fr.commandestation.alarme;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import fr.commandestation.station.Station;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import fr.commandestation.R;


public class AlarmService extends Activity {

	private Station station = null;

    private NotificationManager mNM;
    private long timeDelay=0;
    private Button bStart;
    private Button bStop;
    private TimePicker tpH;

    private String dateAlarme = "";

    private Alarme alarm;
    private PendingIntent pendingintent;
    private AlarmManager am;



    /**
     * Permet activer/desactiver le bouton d'alarme
     */
    private void setEnableAlarme(boolean state)
    {
    	alarm.setEnable(state);
		Switch switchAlarm = (Switch) findViewById(R.id.switch_alarm);
		switchAlarm.setChecked(alarm.isEnable());
    }
    
    private void chargerAlarme() {

    		alarm = null;
    		try {
    			Log.i("ALARM","Read Alarm file");
    			//On charge le fichier d'alarme
	    		ObjectInputStream alarmOIS= new ObjectInputStream(openFileInput("alarm" + station.getAlarmeId() + ".serial"));
	    		
	    		alarm = (Alarme) alarmOIS.readObject();
	    		//Fermeture 
	    		alarmOIS.close();
	    		setEnableAlarme(alarm.isEnable());
	    		
    		}
    		catch(FileNotFoundException fnfe){//Pas de fichier on cree une nouvelle alarme 			
	    		alarm = new Alarme();
	    		setEnableAlarme(false);
	    		alarm.setHour(0);
	    		alarm.setMinute(0);

    		}
    		catch(Exception e) 
    		{
	    		Log.e("LOAD_FILE","Error to Load or create Alarme "+e.toString());
    		}

	}
    
    public void sauverAlarme(){
	    	try {
	    		//On sauvegarde le fichier d'alarme
		    	ObjectOutputStream alarmOOS= new ObjectOutputStream(openFileOutput("alarm"+station.getAlarmeId()+".serial",MODE_APPEND));
		    	alarmOOS.writeObject(alarm);
		    	alarmOOS.flush();
		    	alarmOOS.close();
		    	Log.i("SAVE_ALARM","SAVE ALARM "+station.getNom()+" ACTION "+station.getMsgType() +" DATE "+dateAlarme);
	    	}
	    	catch(Exception e) {
	    		Log.e("SAVE_FILE","Save File "+e.toString());
	    	}
    	}
    
    private void planifierAlarm() {
	 
    		timeDelay = tpH.getCurrentHour()*3600*1000 + tpH.getCurrentMinute()*60*1000;
	    	//On ajoute le reveil au service de l'AlarmManager
    		Log.i("ACTIVE_ALARM","STATION "+station.getNom()+" DATE "+dateAlarme+" Action "+station.getMsgType());
	    	am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeDelay, pendingintent);
	    	setEnableAlarme(true);
	    	Log.i("ACTIVE_ALARM","After Alarme");
	    	
    	}
    
    private void initAlarme()
    {
    	//R�cup�ration de l'instance du service AlarmManager.
    	Log.i("ALARM","Initialisation");
    	am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i("LIFE_CYCLE", "On Create Alarm View");
        setContentView(R.layout.alarme_service);

		//Instancie AlarmManager
		initAlarme();

		//Charge l'objet Station
		loadStation();

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        //Boutton active/desactive l'alarme
		Switch switchAlarm = (Switch) findViewById(R.id.switch_alarm);
		switchAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					startAlarm();
				} else {
					stopAlarm();
				}
			}
		});
		/* DEPRECATED use switch button instead
        bStart = (Button)findViewById(R.id.start_alarm);
        bStart.setOnClickListener(mStartAlarmListener);
        
        //Bouton desactiv� alarme
        bStop = (Button)findViewById(R.id.stop_alarm);
        bStop.setOnClickListener(mStopAlarmListener);
        */

        //Titre de la vue
        setTitle(station.getNom()+" Alarme");
        
        //On charge la vue precedente de l'alarme si elle existe
        chargerAlarme();    

        //On instancie la date au d�part a la date actuel
        if (dateAlarme == "")
        {
	        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	        Date dateView = new Date();
	        dateView.setTime(Calendar.getInstance().getTimeInMillis());
	        
	        dateAlarme = format.format(dateView);
        }
        
        TextView txtView = (TextView)findViewById(R.id.tDateView);
        txtView.setText(dateAlarme);
        
        //TimePicker permet de programmer le msg differer
        tpH = (TimePicker)findViewById(R.id.timePicker1);
        tpH.setIs24HourView(true);
        tpH.setCurrentHour(alarm.getHour());
        tpH.setCurrentMinute(alarm.getMinute());
        // Add listener on change on datepicker
        tpH.setOnTimeChangedListener(tpListener);

        
        //Radio bouton pour start/stop station
        RadioButton rbStart = (RadioButton)findViewById(R.id.rbStart);
        rbStart.setOnClickListener(rbCheckChangeStart);
        RadioButton rbStop = (RadioButton)findViewById(R.id.rbStop);
        rbStop.setOnClickListener(rbCheckChangeStop);

    }
    
    /**
     * Permet de planifier l'action a effectuer sur le broadcast alarm
     */
    private void initIntent()
    {
    	//Appel au broadcastReceiver
    	Intent intentAlarm = new Intent(this, AlarmBroadcastReceiver.class);
		try {
			//On serialize l'objet Station pour planifier l'alarme
			Log.i("ACTION",station.getMsgType());
			intentAlarm.putExtra("STATION", station.serialize());

		} catch (Exception e) 
		{
			Log.e("SERIALIZE_ALARME_VIEW", e.toString());
		}
    	//On creer le pending Intent qui identifie l'Intent de reveil avec un ID et un/des flag(s)
        //TODO
    	pendingintent = PendingIntent.getBroadcast(this, station.getAlarmeId(), intentAlarm, 0);
    }

    @Override
    public void onStart()
    {
    	super.onStart();	
    	Log.i("LIFE_CYCLE","On Start Alarm View");
    }

    /**
     * Evenement gerant le bouton radio pour demarrer la station
     */
    private OnClickListener rbCheckChangeStart = new OnClickListener() {

		@Override
		public void onClick(View v) {
			station.setMsgType("START");	
		}
	};

    /**
     * Evenement gerant le bouton radio pour arreter la station
     */  
    private OnClickListener rbCheckChangeStop = new OnClickListener() {

		@Override
		public void onClick(View v) {
			station.setMsgType("STOP");	
		}
	};


	private OnTimeChangedListener tpListener = new OnTimeChangedListener() {
		
		@Override
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			
			//On enregistre l'action effectuee par l'utilisateur
			alarm.setHour(hourOfDay);
			alarm.setMinute(minute);
			
			//On ajoute le temps supplementaire a la date actuelle
			Date date = new Date();
			long dateAlarmeMilisec = Calendar.getInstance().getTimeInMillis() + hourOfDay*3600*1000 + minute*60*1000;
			date.setTime(dateAlarmeMilisec);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

			TextView txtDatePicker = (TextView)findViewById(R.id.tDateView);
			
			dateAlarme = format.format(date);
			
			txtDatePicker.setText(dateAlarme);
		}
	};

	  private boolean startAlarm() {
			//Planifie l'action du broadcast
			initIntent();
		  //Planification de l'alarme ALarmManager
			planifierAlarm();
			//Affichage de la notification
			showNotification();
			//Sauvegarde de l'alarme dans un fichier
			sauverAlarme();
			Toast.makeText(AlarmService.this, R.string.alarm_active,Toast.LENGTH_LONG).show();
			return true;
	  }

	  
    private boolean stopAlarm()
	{

        	//recuperation de l'instance de l'intent de l'alarme
            //TODO
        	pendingintent = PendingIntent.getBroadcast(getApplicationContext(), station.getAlarmeId(), new Intent(), 0);
        	if(pendingintent != null)
        	{   		    	
	            Toast.makeText(AlarmService.this, R.string.alarm_cancel,Toast.LENGTH_LONG).show();
	            Log.i("CANCEL_ALARM", "CANCEL ALARM " + station.getNom());
	            ContextWrapper cw = new ContextWrapper(getApplicationContext());
	            cw.getFileStreamPath("alarm"+station.getAlarmeId()+".serial").delete();
	            Log.i("REMOVE_FILE", "FILE " + "alarm" + station.getAlarmeId() + ".serial");
        		setEnableAlarme(false);
        		mNM.cancel(station.getAlarmeId());
        		initAlarme();
		    	am.cancel(pendingintent);
		    	return true;
        	}
        	else {
				Log.e("INTENT","NO INSTENCE OF PendingIntent");
				return false;

			}
	}

	/**
	 * Permet de remplir l'objet Station et de specifier l'intent pour le broadcastReceiver
	 */
	public void loadStation()
	{
		byte[] stationSerial = getIntent().getExtras().getByteArray("STATION");
		station = new Station(); 
        try {   	
        	station.populate(stationSerial ); //On charge l'objet station
        	station.setMsgType("START"); //Par default on veux que la station demarre
        	Log.i("POPULATE","Populate station set Start by default");
			
		} catch (Exception e) {
			Log.e("POPULATE", e.toString());
		} 
        
		  
	}
	
	 /**
	  * Affiche une notification avec l'id de l'alarme lancee
	 * @throws IOException 
	  */
	 private void showNotification()  {
	     
	     Notification notification;
         //TODO
	     PendingIntent pendingintentNotification = PendingIntent.getActivity(getApplicationContext(), 0, getIntent(), 0);
	     if (station.getMsgType().contains("START"))
	     {
	    	 CharSequence text = getText(R.string.alarm_service_text_to_start)+station.getNom()+"->"+dateAlarme;
	    	 notification = new Notification(R.drawable.green, text,System.currentTimeMillis());
		     //notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),text, pendingintentNotification);
	     }
	     else if (station.getMsgType().contains("STOP"))
	     {
	    	 CharSequence text = getText(R.string.alarm_service_text_to_stop)+station.getNom()+"->"+dateAlarme;
	    	 notification = new Notification(R.drawable.red, text,System.currentTimeMillis());
		     //notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),text, pendingintentNotification);
	     }
	     else 
	    	 notification = null;

	     try
	     {
	    	 mNM.notify(station.getAlarmeId(), notification);
	     } catch (Exception e) {
			Log.e("NOTIFICATION",e.toString());
		}
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
	 * Appel� lors de la destruction l'activit�
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("LIFE_CYCLE","On Destroy Alarm View");
	}

	

}