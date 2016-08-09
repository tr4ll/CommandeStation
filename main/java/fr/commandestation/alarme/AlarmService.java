package fr.commandestation.alarme;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.commandestation.bdd.DatabaseInit;
import fr.commandestation.bdd.DatabaseQuery;
import fr.commandestation.outils.Outils;
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

    private Alarme alarmBean;
    private PendingIntent pendingintent = null;
	private Intent intentAlarm = null;
    private AlarmManager am = null;

	private AlarmManager getInnerAlarmManager() {
		if (am == null) {
			//Lecture de l'alarme manager
			am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		}
		return am;
	}

	private PendingIntent getInnerPendingIntentAlarm() {
		if (pendingintent == null) {
			//Appel au broadcastReceiver
			Intent intentAlarm = getInnerIntentAlarm();
			//On creer le pending Intent qui identifie l'Intent de reveil avec un ID et un/des flag(s)
			pendingintent = PendingIntent.getBroadcast(this, station.getAlarmeId(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		return pendingintent;
	}

	private Intent getInnerIntentAlarm() {
		if (intentAlarm == null) {
			//Appel au broadcastReceiver
			intentAlarm = new Intent(this, AlarmBroadcastReceiver.class);
			try {
				//On serialize l'objet Station pour planifier l'alarme
				Log.i("ACTION",station.getMsgType());
				intentAlarm.putExtra("STATION", station.serialize());

			} catch (Exception e)
			{
				Log.e("SERIALIZE_ALARME_VIEW", e.toString());
			}
		}
		return intentAlarm;
	}

	/**

     * Permet activer/desactiver le bouton d'alarme
     */
    private void setEnableAlarme(boolean state)
    {
		Switch switchAlarm = (Switch) findViewById(R.id.switch_alarm);
		switchAlarm.setChecked(state);
    }

	public void sauverAlarme(Station s){
		DatabaseInit init = new DatabaseInit(this);
		DatabaseQuery query = init.getBdd();
		String sqlStatement = " SET ALARME_TIME='"+s.getAlarmeBean().getScheduleAlarm().getTimeInMillis()+"'," + "ALARME_ACTION='"+s.getAlarmeBean().getAction().toString()+"' WHERE NUMTEL='" +s.getNumeroTel()+"';";
		Log.i("SAVE_ALARM","QUERY:"+sqlStatement);
		query.updateRow(sqlStatement);

	}

    private void planifierAlarm(AlarmManager am,Station station,PendingIntent pendingintent) {
	    	//On ajoute le reveil au service de l'AlarmManager
    		Log.i("ACTIVE_ALARM", "STATION " + station.getNom() + " Action " + station.getMsgType());
			getInnerAlarmManager().set(AlarmManager.RTC_WAKEUP, station.getAlarmeBean().getScheduleAlarm().getTimeInMillis(), pendingintent);
	    	Log.i("ACTIVE_ALARM","After Alarme");
	    	
    	}
    
    private void initAlarme()
    {
    	//Recuperation de l'instance du service AlarmManager.
    	Log.i("ALARME_SERVICE","Initialisation");
		boolean alarmUp = station.getAlarmeId() != null ? (PendingIntent.getBroadcast(this, station.getAlarmeId(), getInnerIntentAlarm(), PendingIntent.FLAG_NO_CREATE) != null) : false;
		setEnableAlarme(alarmUp);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
		//Charge l'objet Station
		loadStation();

		Log.i("LIFE_CYCLE", "On Create Alarm View");
		setContentView(R.layout.alarme_service);

		//Instancie AlarmManager
		initAlarme();

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

        //Titre de la vue
        setTitle(station.getNom()+" Alarme");


        TextView txtView = (TextView)findViewById(R.id.tDateView);
		//On instancie la date au depart a la date actuel
		txtView.setText( Outils.convertTimeMiliToDateFr(Calendar.getInstance().getTimeInMillis()));
        
        //TimePicker permet de programmer le msg differer
        tpH = (TimePicker)findViewById(R.id.timePicker1);
        tpH.setIs24HourView(true);
		// Pas d'initialisation du timepicker
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

			//On ajoute le temps supplementaire a la date actuelle
			long dateAlarmeMilisec = Calendar.getInstance().getTimeInMillis() + hourOfDay*3600*1000 + minute*60*1000;

			//Texte de restitution de la date d'action
			TextView txtDatePicker = (TextView)findViewById(R.id.tDateView);

			txtDatePicker.setText(Outils.convertTimeMiliToDateFr(dateAlarmeMilisec));
		}
	};

	  private boolean startAlarm() {
			//Planifie l'action du broadcast
			initIntent();
		  	//Calculer la date du scheduler
		  	Long timeDelay = Calendar.getInstance().getTimeInMillis() + tpH.getCurrentHour() * 1000* 3600 + tpH.getCurrentMinute() * 1000 * 60;
		  	Calendar newAlarmeDate = Calendar.getInstance();
		 	newAlarmeDate.setTimeInMillis(timeDelay);
			station.getAlarmeBean().setScheduleAlarm(newAlarmeDate);
		 	station.getAlarmeBean().setAction(Alarme.AlarmAction.getAlarmAction(station.getMsgType()));
		  	//Planification de l'alarme ALarmManager
			planifierAlarm(getInnerAlarmManager(), station, getInnerPendingIntentAlarm());
			//Affichage de la notification
			showNotification();
			//Sauvegarde de l'alarme en base
			sauverAlarme(station);
			Toast.makeText(AlarmService.this, R.string.alarm_active,Toast.LENGTH_LONG).show();
			return true;
	  }

	  
    private boolean stopAlarm()
	{
		//recuperation de l'instance de l'intent de l'alarme
		if(getInnerPendingIntentAlarm() != null)
		{
			Toast.makeText(AlarmService.this, R.string.alarm_cancel, Toast.LENGTH_LONG).show();
			Log.i("CANCEL_ALARM", "CANCEL ALARM " + station.getNom());
			ContextWrapper cw = new ContextWrapper(getApplicationContext());

			mNM.cancel(station.getAlarmeId());
			getInnerAlarmManager().cancel(getInnerPendingIntentAlarm());

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
        	station.populate(stationSerial); //On charge l'objet station
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
	     PendingIntent pendingintentNotification = PendingIntent.getActivity(getApplicationContext(), 0, getIntent(), 0);
		 String dateAlarme = Outils.convertTimeMiliToDateFr(station.getAlarmeBean().getScheduleAlarm().getTimeInMillis());
	     if (station.getMsgType().contains("START"))
	     {
			 //FIXME
	    	 CharSequence text = getText(R.string.alarm_service_text_to_start)+station.getNom()+"->"+dateAlarme;
	    	 notification = new Notification(R.drawable.green, text,System.currentTimeMillis());
		     notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),text, pendingintentNotification);
	     }
	     else if (station.getMsgType().contains("STOP"))
	     {
			 //FIXME
	    	 CharSequence text = getText(R.string.alarm_service_text_to_stop)+station.getNom()+"->"+dateAlarme;
	    	 notification = new Notification(R.drawable.red, text,System.currentTimeMillis());
		     notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),text, pendingintentNotification);
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