package fr.commandestation.main;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


import fr.commandestation.sms.SMS;
import fr.commandestation.station.Station;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import fr.commandestation.bdd.DatabaseInit;
import fr.commandestation.bdd.DatabaseQuery;
import fr.commandestation.R;

public class Configuration extends Activity {

	private Station station = new Station(); //Obj Station
	//private ArrayList<String> telAllowTmp = new ArrayList<String>();
	private DatabaseQuery query;
	private Button valid;
	private String numTelTmp;
	private DatabaseInit init;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.ajouterpreference);
		init = new DatabaseInit(this);
		query = init.getBdd();
		Log.i("DATABASE","OPEN DATABASE");
		
		//Test si Update ou insert new station
		Bundle data = getIntent().getExtras();
		if (data != null)
		{
			Log.i("UPDATE_STATION","LAUNCH TO UPDATE A STATION");
			String nomStation = this.getIntent().getExtras().get("nomstation").toString();
			station = loadData(nomStation);
		
			//Init des champ si on edit la page
			EditText t1 = (EditText) findViewById(R.id.name);
			t1.setText(station.getNom());
			EditText t2 = (EditText) findViewById(R.id.numero);
			t2.setText(station.getNumeroTel());
			EditText t3 = (EditText) findViewById(R.id.demarrerConfiguration);
			t3.setText(station.getDemarrer());
			EditText t4 = (EditText) findViewById(R.id.arreterConfiguration);
			t4.setText(station.getArreter());
			EditText t5 = (EditText) findViewById(R.id.demarrerNotification);
			t5.setText(station.getDemarrerNotif());
			EditText t6 = (EditText) findViewById(R.id.arreterNotification);
			t6.setText(station.getArreterNotif());
		}
		else
			Log.i("NEW_STATION", "LAUNCH TO CREATE A NEW STATION");
			
		//Appel � la m�thode pour valider le formulaire
		eventValidForm();

	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		//Affichage de la list des num de phones autoris� a acceder � la station
		affichageList();
		
		//Ajouter un evenement sur le bouton ajout de phone
		Button bAddPhones = (Button)findViewById(R.id.baddAllowPhone);
		bAddPhones.setOnClickListener(onClikListenerAddPhones);
		Button bdelPhones = (Button)findViewById(R.id.bdelAllowPhone);
		bdelPhones.setOnClickListener(onClickListenerDelPhones);
		
	}


	private Station loadData(String nomStation)
	{
		Station s = null;
		//On requete la Base pour recupe les donn�es sur l
		HashMap<String,String> result = new HashMap<String, String>();
		result = query.getDataFields( new String[] {"NOM","NUMTEL","DEMARRER","ARRETER","DEMMARERNOTF","ARRETERNOTF","TELEPHONEAUTO"},"NOM='" + nomStation +"'" ,null , null, null, "NOM", "ASC");

		//Recup des valeurs de la bdd
		String tel = result.get("NUMTEL");
		numTelTmp = tel;
		String dem = result.get("DEMARRER"); 
		String arret = result.get("ARRETER"); 
		String demNotif = result.get("DEMMARERNOTF");
		String arrNotif = result.get("ARRETERNOTF");
		String telAutoStream = result.get("TELEPHONEAUTO");
		s = new Station(nomStation,tel,dem,arret);
		s.setArreterNotif(arrNotif);
		s.setDemarrerNotif(demNotif);
		s.setTelAutorise(getStreamTelephone(telAutoStream));
		return s;
		
	}
	
	private void eventValidForm() {
		final Intent intentAcc = new Intent(this,Acceuil.class);
		final Toast mIncomplet = Toast.makeText(this, R.string.txt_empty_field, Toast.LENGTH_SHORT);
		final Toast mWrongPhone = Toast.makeText(this, R.string.txt_wrong_phone_number , Toast.LENGTH_SHORT);
		
		valid = (Button) findViewById(R.id.bValidStation);
		valid.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v) {
				
				//On recupere les champs des EditText
				String nom = ((EditText)findViewById(R.id.name)).getText().toString();
				String numero = ((EditText)findViewById(R.id.numero)).getText().toString();
				String confDemarrer = ((EditText)findViewById(R.id.demarrerConfiguration)).getText().toString();
				String confArreter = ((EditText)findViewById(R.id.arreterConfiguration)).getText().toString();
				String notifDemarrer = ((EditText)findViewById(R.id.demarrerNotification)).getText().toString();
				String notifArreter = ((EditText)findViewById(R.id.arreterNotification)).getText().toString();
				
				//Check box
				CheckBox cb1 = (CheckBox) findViewById(R.id.checkBox1);
				boolean activeSend = cb1.isChecked();
				
				//On genere un id entre 1 et 10000 pour l'alarme
		    	Random r = new Random();
		    	int id = r.nextInt(10000) + 1;
				
				if ( !nom.equals("") && !numero.equals("") && !confDemarrer.equals("") && !confArreter.equals(""))
				{
					if( numero.matches("^0[0-9]{9}"))
					{
						station.setNom(nom);
						station.setNumeroTel(numero);
						station.setDemarrer(confDemarrer);
						station.setArreter(confArreter);
						station.setAlarmeId( id );
						if (notifArreter != "")
							station.setArreterNotif(notifArreter);
						if (notifDemarrer != "")
							station.setDemarrerNotif(notifDemarrer);	
						
						
						if (stationExists(station))
							updateStation(station);
						else
							insertStation(station);
						//Si on veux pas encoyer de sms pour la config
		    			if (activeSend)
		    			{
			    			Intent i = new Intent(getApplicationContext(),SMS.class);
			    			station.setMsgType("CONFIGURATION");
			    			try {
								i.putExtra("STATION", station.serialize());
							} catch (IOException e) {
								e.printStackTrace();
							}
			    			startService(i);
							startActivity(intentAcc);
		    			}
		    			else 
		    				startActivity(intentAcc);
					}
					else
					{
						mWrongPhone.show();
						Log.w("CHECK_FORM","Wrong Phone Number");
					}
				}
				else
				{
					mIncomplet.show();
					Log.w("CHECK_FORM","Incomplete Form");
				}
				
			}
			
		});
	}
	



	/**
	 * Appel� lors de la reprise de l'activit�
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			query.destroy();
			Log.i("DATABASE","Close Daatabase");
		} catch (Throwable e) {
			Log.e("DATABASE","Close Database "+e.toString());
		}
	}
	

	/**
	 * Appell�e lors du fin de cycle visible (arri�re plan) permet de
	 * sauvegarder la vue utilisateur
	 */
	@Override
	public void onSaveInstanceState(Bundle SavedInstanceState) {
		super.onSaveInstanceState(SavedInstanceState);
	}

	public void insertStation(Station s) {

		query.appendData("NOM", s.getNom());
		query.appendData("NUMTEL", s.getNumeroTel());
		query.appendData("DEMARRER", s.getDemarrer());
		query.appendData("ARRETER", s.getArreter());
		query.appendData("ARRETERNOTF", s.getArreterNotif());
		query.appendData("DEMMARERNOTF", s.getDemarrerNotif());
		query.appendData("ALARMEID", Integer.toString(s.getAlarmeId()));
		query.appendData("TELEPHONEAUTO", setStreamTelephone(s.getTelAutorise())); //Insertion d'une liste de tel on passe par une var tmp car la station n'existe pas lors de l'appel a addPhone
		query.addRow();
		Log.i("DATABASE","Insert Station "+station.getNom());

	}
	public void updateStation(Station s) {
		String sqlStatement = " SET "
				+ "NUMTEL='"+s.getNumeroTel()+"',"
				+ "NOM='"+s.getNom()+"',"
				+ "DEMARRER='"+s.getDemarrer()+ "',"
				+ "ARRETER='"+s.getArreter()+ "',"
				+ "ARRETERNOTF='"+s.getArreterNotif()+ "',"
				+ "DEMMARERNOTF='"+s.getDemarrerNotif()+ "',"
				+ "ALARMEID='"+Integer.toString(s.getAlarmeId())+ "',"
				+ "TELEPHONEAUTO='"+setStreamTelephone(s.getTelAutorise())+"'"
				+ " WHERE NUMTEL='"+numTelTmp+"';";
		query.updateRow(sqlStatement);

		Log.i("DATABASE","Update Station "+s.getNom());
	}
	
	public boolean stationExists(Station s)
	{
		ArrayList<String> retour = new ArrayList<String>();
		retour = query.getData(new String[]{"NUMTEL"}, "NUMTEL='"+numTelTmp+"'",null,null,null,"NUMTEL","ASC");
		if (retour.isEmpty()) return false;
		else return true;
	}
	
	/**
	 * Listener sur le bouton ajout telephone
	 */
	private OnClickListener onClikListenerAddPhones = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Log.i("ADD_PHONES","Click on button add phones");
			EditText tbPhone = (EditText)findViewById(R.id.tbAllowPhone);	
			String txtPhone = tbPhone.getText().toString();
			if( txtPhone.matches("^0[0-9]{9}"))
			{
				Log.i("ADD_PHONE","Set Phone "+txtPhone);
				station.getTelAutorise().add(txtPhone);
				tbPhone.setText(""); // on vide le formulaire
				affichageList(); // on recharge la liste une fois qu'on a ajouter le nouveau num�ro
			}
			else
			{		
				
				Log.e("NUMBERPHONE","WRONG number phone");
			}
		}
	};

	/**
	 * Listener sur le boutton del phone
	 */
	private OnClickListener onClickListenerDelPhones = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.i("DEL_PHONES","Click on button del phones");
			int index = station.getTelAutorise().size() -1 ;
			try {
				station.getTelAutorise().remove(index);
				Log.i("DEL_PHONES", "Delete last index of arrayList");
				affichageList(); // rechargement de la liste des tel autorise
			} catch (Exception e) {
				Log.e("DEL_PHONES",e.toString());
				Toast.makeText(getApplicationContext(), R.string.txt_wrong_phone_number, Toast.LENGTH_SHORT).show();
			}
		}
		
	};
	
	/**
	 * Affichage de la Liste des t�l�phones autoris� a communiquer avec la Station
	 */
	public void affichageList()
	{
		
		ListView list = (ListView)findViewById(R.id.list2);
		
		if (station != null)
			list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, station.getTelAutorise()));
	}
	

	/**
	 * Permet de transformer une arrayList en stream
	 * @param listTelephones
	 * @return stream
	 */
	private String setStreamTelephone(List<String> listTelephones)
	{

		String stream = "";
		for (String phone : listTelephones  )
			stream += phone+";";
		return stream;
	}
	
	/**
	 * @param stream
	 * @return
	 */
	private ArrayList<String> getStreamTelephone(String stream)
	{
		ArrayList<String> listPhone = new ArrayList<String>();
		String[] tabTels = stream.split(";"); //on d�limite chaque num de tel dans la base par un ;
		
		for (int i = 0 ;  i < tabTels.length ; i++)
			listPhone.add(tabTels[i]);

		return listPhone;
	}
}
