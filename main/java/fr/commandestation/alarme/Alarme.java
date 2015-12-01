package fr.commandestation.alarme;

import java.io.Serializable;

import fr.commandestation.station.Station;


public class Alarme extends Station implements Serializable{


	private static final long serialVersionUID = 1L;
	
	private int hour = 0;
	private int minute = 0;
	private boolean isEnable;
	private String action;
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinute() {
		return minute;
	}
	public void setMinute(int minute) {
		this.minute = minute;
	}
	public boolean isEnable() {
		return isEnable;
	}
	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}
	
	/**
	 * Permet de connaitre l'action effectu� par l'alarme
	 * @return action
	 */
	public String getAction() {
		return this.action;
	}
	/**
	 * Permet de configurer l'action a effectuer par l'alarme
	 * @param action "START" ou "STOP"
	 */
	public void setAction(String action) {
	
		if (action.contains("START"))
		{
			this.action = action;
		}
		else if (action.contains("STOP"))
		{
			this.action = action;
		}
		else
		{
			return;
		}
	
	}

}
