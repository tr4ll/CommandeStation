package fr.commandestation.alarme;

import java.io.Serializable;
import java.util.Calendar;

import fr.commandestation.station.Station;


public class Alarme extends Station implements Serializable{


	private static final long serialVersionUID = 1L;

	private AlarmAction action;
	private Calendar scheduleAlarm = null;

	public Calendar getScheduleAlarm() {
		return scheduleAlarm;
	}

	public void setScheduleAlarm(Calendar scheduleAlarm) {
		this.scheduleAlarm = scheduleAlarm;
	}

	public AlarmAction getAction() {
		return action;
	}

	public void setAction(AlarmAction action) {
		this.action = action;
	}

	public enum AlarmAction {
		ACTION_START("START"),
		ACTION_STOP("STOP");
		String code;
		AlarmAction(String code) {this.code=code;}
		public static AlarmAction getAlarmAction(String code) {
			for (AlarmAction a:AlarmAction.values()) {
				if (a.code.equalsIgnoreCase(code)) return a;
			}
			return null;
		}

		@Override
		public String toString() {
			return code;
		}
	}

}
