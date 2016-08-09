package fr.commandestation.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import fr.commandestation.alarme.Alarme;
import fr.commandestation.station.Station;
import fr.commandestation.bdd.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import fr.commandestation.R;

/*
 * Une activite est un thread chaque page coresspond a une activite
 */
public class Acceuil extends ListActivity {

	//BDD
	private DatabaseInit init;
	private DatabaseQuery query;
	private Station station = new Station();
	private Button buttonAdd;
	
	//Creation de la list
	private ListView list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{	
		super.onCreate(savedInstanceState);
		Log.i("CYCLE_LIFE", "On Create Acceuil");
		setContentView(R.layout.main);

	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Log.i("CYCLE_LIFE","On Start Acceuil");
		init = new DatabaseInit(this);
		query = init.getBdd();
		listStation();
		
		eventButtonAdd();
		eventList();
	}


	private void eventList() {

		list = getListView();
		final Intent intentCommande = new Intent(this,Commande.class);
		
		//1 Clic long sur un element de la liste
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long id) {

				//On recupere les donn�es du ListView
				final String nomSelectDialogBox = getListView().getItemAtPosition(position).toString();
				Log.i("STATION", "Long click STATION " + nomSelectDialogBox);

				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				builder.setTitle(getString(R.string.box_menu_title));

				final Intent intentConfiguration = new Intent(v.getContext(),Configuration.class);
				final AlertDialog.Builder confirmBox = new AlertDialog.Builder(v.getContext());
				confirmBox.setTitle(getString(R.string.box_confirm_title));
				confirmBox.setMessage(getString(R.string.box_config_message));

				String[] listMenu = {getString(R.string.box_menu_remove),getString(R.string.box_menu_edit)};
				builder.setItems(listMenu, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
							case 1:
								Toast.makeText(getApplicationContext(), getString(R.string.toast_main_edit) + " " + nomSelectDialogBox, Toast.LENGTH_SHORT).show();
								intentConfiguration.putExtra("nomstation", nomSelectDialogBox);
								startActivity(intentConfiguration);
								break;
							default:
								confirmBox.setPositiveButton(getString(R.string.box_config_positive), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										//Suppression de la station de la bdd
										eraseStation(nomSelectDialogBox);
										listStation();
										Toast.makeText(getApplicationContext(), getString(R.string.toast_main_remove) + " " + nomSelectDialogBox, Toast.LENGTH_SHORT).show();

									}
								});
								confirmBox.setNegativeButton(getString(R.string.box_config_negative), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
										Toast.makeText(getApplicationContext(), getString(R.string.box_confirm_cancel), Toast.LENGTH_SHORT).show();
									}
								});
								confirmBox.create().show(); //Creation de la boite de dialogue de confirmation
								break;
						}
					}
				});
				builder.create().show();
				return true;
			}
			
		});
		//AlertDialog alert = builder.create();

	}


	
	private void eventButtonAdd() {
		final Intent intentConfiguration = new Intent(this,Configuration.class);
		
		buttonAdd = (Button) findViewById(R.id.BAddStation);
		buttonAdd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Demmare l'activit� de la classe Configuration
				startActivity(intentConfiguration);
			}
		});
	}

	public void eraseStation( String nom) {

		query.removeRow("NOM", nom);

	}

	public void listStation() {

		ArrayList<String> queryString = query.getData(new String[] { "NOM" },
				null, null, null, null, "NOM", "ASC");

		// Set the ListView
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, queryString));


	}
	
	private Station loadData(String nomStation)
	{
		Station s = null;
		//On requete la Base pour recupe les donnees sur l
		HashMap<String,String> result = new HashMap<String, String>();
		result = query.getDataFields( new String[] {"NOM","NUMTEL","DEMARRER","ARRETER","DEMMARERNOTF","ARRETERNOTF","ALARMEID","ALARME_TIME","ALARME_ACTION"},"NOM='" + nomStation +"'" ,null , null, null, "NOM", "ASC");

		//Recup des valeurs de la bdd
		String tel = result.get("NUMTEL");
		String dem = result.get("DEMARRER"); 
		String arret = result.get("ARRETER"); 
		String demNotif = result.get("DEMMARERNOTF");
		String arrNotif = result.get("ARRETERNOTF");
		String alarmeID = result.get("ALARMEID");
		Alarme alarme = new Alarme();
		alarme.setAction(Alarme.AlarmAction.getAlarmAction(result.get("ALARME_ACTION")));
		if (result.get("ALARME_TIME") != null) {
			Calendar cal=Calendar.getInstance();
			cal.setTimeInMillis(Long.parseLong(result.get("ALARME_TIME")));
			alarme.setScheduleAlarm(cal);
		}

		s = new Station(nomStation,tel,dem,arret);
		s.setArreterNotif(arrNotif);
		s.setDemarrerNotif(demNotif);
		s.setAlarmeId(Integer.parseInt(alarmeID));
		s.setAlarmeBean(alarme);
		return s;
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_conf:
			Intent iConfPref = new Intent(getApplicationContext(), ConfigurationPreferences.class);
			startActivity(iConfPref);
			break;
		case R.id.menu_credit:
			//TODO : creer une page de credit
			break;
		case R.id.menu_refresh:
			
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Appel lorsque l'activite passe en arriere plan
	 */
	@Override
	public void onPause() {
		super.onPause();
		//Toast.makeText(getApplicationContext(), "onPauseAcceuil()", Toast.LENGTH_SHORT).show();
		//Fermeture de la BDD
		Log.i("CYCLE_LIFE", "On Pause Acceuil");
		query.destroy();

	}

	
	/**
	 * Appel� lors de la destruction de l'activit�
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("CYCLE_LIFE","On Destroy Acceuil");
	}


	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Log.i("STATION", "SELECTED ON STATION " + station.getNom());
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Intent intentCommande = new Intent(this,Commande.class);
		//On recupere les donn�es du ListView
		final String nom = getListView().getItemAtPosition(position).toString();

		station = loadData(nom);

		//On serialiez l'objet Station
		try {
			intentCommande.putExtra("STATION", station.serialize());
		} catch (IOException e) {
			Log.e("SERIALIZE",e.toString());
		}
		Log.i("STATION","CLICK ON STATION "+station.getNom());
		startActivity(intentCommande);
	}
}
