package fr.commandestation.station;


import java.util.ArrayList;
import java.util.List;

public class Station extends CSerializable {


	private static final long serialVersionUID = 1L;
	
		private String numeroTel;
		private String demarrer;
		private String arreter;
		private String demarrerNotif;
		private String arreterNotif;
		private String nom;
		private int alarmeId;
		private String msgType;
		private boolean status;
		private List<String> telAutorise = new ArrayList<String>();

		
		/**
		 * Deserialization de la class
		 * @param serialize
		 * @throws ClassNotFoundException
		 */
	    public void populate (byte[] serialize)
	            throws Exception, ClassNotFoundException {
	        Station object = (Station)unserialize (serialize);	 
	        setNumeroTel(object.getNumeroTel());
	        setDemarrer(object.getDemarrer());
	        setArreter(object.getArreter());
	        setArreterNotif (object.getArreterNotif());
	        setDemarrerNotif(object.getDemarrerNotif());
	        setNom(object.getNom());
	        setAlarmeId(object.getAlarmeId());
	        setMsgType(object.getMsgType());
	        
	    }
	    
	    /**
	     * 
	     * @param nom Nom de la station
	     * @param numeroTel Numero de telephone de la station
	     * @param demarrer Texte qui permet de demarrer la station
	     * @param arreter Texte qui permet d'arreter la station
	     */
		public  Station(String nom,String numeroTel, String demarrer, String arreter) 
		{
			this.numeroTel = numeroTel;
			this.demarrer = demarrer;
			this.arreter = arreter;
			this.nom = nom;
		}
		
		/**
		 * Constructeur d'une station vide
		 */
		public  Station() {
		}
		
		public String getNumeroTel() {
			return numeroTel;
		}
		public void setNumeroTel(String numeroTel) {
			this.numeroTel = numeroTel;
		}
		public String getDemarrer() {
			return demarrer;
		}
		public void setDemarrer(String demarrer) {
			this.demarrer = demarrer;
		}
		public void setArreter(String arreter) {
			this.arreter = arreter;
		}	
		public String getArreter() {
			return arreter;
		}
		public void setArreterNotif(String arreter) {
			this.arreterNotif = arreter;
		}
		public void setDemarrerNotif(String demarrer) {
			this.demarrerNotif = demarrer;
		}
		public void setNom(String nom) {
			this.nom = nom;
		}	
		public void setMsgType(String msgtype) {	
			this.msgType = msgtype;		
		}
		public String getArreterNotif() {
			return arreterNotif;
		}
		public String getDemarrerNotif() {
			return demarrerNotif;
		}
		public String getNom() {
			return nom;
		}
		
		public String getMsgType() {
			return msgType;
		}
	
		public boolean getStatus()
		{
			return status;
		}
		public void setStatus(boolean state)
		{
			this.status = state;
		}	
		

		/**
		 * 
		 * @return le numero d'identifiant de l'alarme
		 */
		public int getAlarmeId() {
			return alarmeId;
		}

		/**
		 * 
		 * @param alarmeId le numero d'identifiant de l'alarme
		 */
		public void setAlarmeId(int alarmeId) {
			this.alarmeId = alarmeId;
		}

		public List<String> getTelAutorise() {
			return telAutorise;
		}

		public void setTelAutorise(List<String> telAutorise) {
			this.telAutorise = telAutorise;
		}
}


