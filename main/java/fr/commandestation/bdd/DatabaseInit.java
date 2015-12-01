package fr.commandestation.bdd;

import java.util.ArrayList;

import android.content.Context;

public class DatabaseInit {

	private DatabaseQuery query;
	private ArrayList<String> champ;
	private String nameBdd;

	public DatabaseInit(Context c) {
		
		champ = new ArrayList<String>();
		champ.add("NOM");
		champ.add("NUMTEL");
		champ.add("DEMARRER");
		champ.add("ARRETER");
		champ.add("DEMMARERNOTF");
		champ.add("ARRETERNOTF");
		champ.add("TELEPHONEAUTO");
		champ.add("ALARMEID");
		nameBdd = "Station";
		query = new DatabaseQuery(c, nameBdd, champ);

	}

	public DatabaseQuery getBdd() {
		return query;
	}

	@SuppressWarnings("null")
	public String[] getChamp() {
		int i = 1;
		String[] resultChamp = null;
		for (String l : champ) {
			resultChamp[i] = l;
			i++;
		}
		return resultChamp;
	}

	public String getNameBdd() {
		return nameBdd;
	}
}
