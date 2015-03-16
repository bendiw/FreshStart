package calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.joda.time.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Person extends Database implements NotificationListener{
	private GeneralCal cal;
	private String name;
	private String email;
	private String address;
	private int mobile;
	private String position;
	private int postnr;
	private int userID;
	private InvitationBuilder ib;
	private ArrayList<Group> groups = new ArrayList<Group>();
	protected List<Invitation> invites = new ArrayList<Invitation>();
	protected List<Notification> notifications = new ArrayList<Notification>();
	protected List<Invitation> oldInvites = new ArrayList<Invitation>();

	public void setCal(GeneralCal cal){
		this.cal=cal;
	}
	
	public List<Invitation> getInv(){
		return this.invites;
	}
	
	public List<Invitation> getOldInv(){
		return this.oldInvites;
	}
	
	//fra User
	String password;
	boolean newUser;
	
	Statement stmt = null;
	ResultSet rs = null;
	String query = null;
	PreparedStatement pstmt = null;
	
 
	public Person(int userID, String firstname, String lastname, String address, int mobile, String email, String position, int postnr) throws Exception{
//		setName(name);
		setName(firstname+" "+lastname);
		setEmail(email);
		this.userID=userID;
		this.address=address;
		this.position=position;
		this.mobile=mobile;
		this.postnr=postnr;
		ib = new InvitationBuilder(this.userID);		
	}
	
	
	public Person(String email, String password, boolean newUser) throws Exception {
		this.email = email;
		this.password = password;
		this.newUser = newUser;
		if(newUser) {
			if(emailExists(email)) {
				System.out.println("Eposten finnes allerede, pr�v en annen epost.");
				throw new IllegalArgumentException();
			} else {
				makeNewUser(email, password);
			}
			
		} else {
			if(emailExists(email)) {
				loginUser(email, password);
			} else {
				System.out.println("Eposten finnes ikke, pr�v en ny epost.");
				throw new IllegalArgumentException();
			}
		}
		ib = new InvitationBuilder(this.userID);			
	}
	
	public boolean makeNewUser(String email, String password) throws Exception {
		if(isValidEmail(email)) {
			try {
				openConnection();
				pstmt = conn.prepareStatement("INSERT INTO Bruker (epostadresse, passord) "
						+ " VALUES (?, ?)");
				pstmt.setString(1, email);
				pstmt.setString(2, password);
				
				pstmt.execute();
				return true;
			} finally {
				closeConnection();
			}	
		} else {
			System.out.println("Ugyldig email");
//			return false;
			throw new IllegalArgumentException();
		}
	}
	
	public void loginUser(String email, String password) throws Exception {
			try {
				openConnection();
				pstmt = conn.prepareStatement("SELECT * FROM Bruker B WHERE B.epostadresse = '" + email + "';");
				rs = pstmt.executeQuery();
				while (rs.next()) {
					this.password = rs.getString("passord");
					this.userID = rs.getInt("brukerID");
				}
				
				if (this.password.equals(password)) {
					System.out.println("Login successful");
				} else {
					System.out.println("Login unsuccessful, wrong password");
					throw new IllegalArgumentException();
				}
				
			} finally {
				closeConnection();
			}	
	}
	

	public List<Notification> getNotifications() {
		return notifications;
	}
	
	public boolean emailExists(String email) throws Exception {
		try {
			openConnection();
			pstmt = conn.prepareStatement("SELECT B.epostadresse FROM Bruker B WHERE B.epostadresse = '" + email + "';");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				return true;
			}
		} finally {
			closeConnection();
		}
		return false;
	}
	
	
	public String toString(){
		return "ID no.: "+this.userID+"\n"+"Name: "+name+"\n"+"Email: "+email;
	}
	
	public boolean isMemberOf(Group group){
		if(groups.contains(group)){
			return true;
		}
		return false;
	}
	
	public ArrayList<Group> getGroups(){
		return this.groups;
	}

	public GeneralCal getCalendar(){
		return this.cal;
	}
	
	public int getUserID(){
		return this.userID;
	}
	
	public void setUserID(int userID){
		this.userID=userID;
	}
	
	public Person(GeneralCal c){
		this.cal=c;
	}
	
	
	private boolean isValidName(String name){
		return true;
		}
	
	private boolean isValidEmail(String email){
		int atCount=0;
		if(email=="\0"){
			return true;
		}
		for (int i = 0; i < email.length(); i++) {
			char c = email.charAt(i);
			if(c=='@'){
				atCount++;
			}
		}
		if(atCount==0||atCount>1){
			return false;
		}
		return true;
	}
	public void setEmail(String email) {
//		if(isValidEmail(email)) {
			this.email = email;
////		} else {
//			throw new IllegalArgumentException("Invalid email");
//		}
	}

	public void setName(String name) {
		if (isValidName(name)){
			this.name = name;
		}else{
			throw new IllegalArgumentException("Invalid name!");
		}
	}
		
	public String getName(){
		return this.name;
	}
	
	public String getEmail(){
		return this.email;
	}

	
	public void createMeeting(Meeting m){
		cal.addMeeting(m);
	}

	public void newNotification(Notification n) {
		return;
		// TODO Auto-generated method stub
		
	}
	
	public void addInvitation(Invitation inv) {
		invites.add(inv);
		// trenger kanskje mer validering!

	}
	
	//respond (Boolean ans) setter automatisk pri til true
	//respond (Boolean ans, Boolean pri)
	//setPriority: dersom et møte endrer prioritet fra false til true,
	//settes pri til alle møter som krasjer til false
	
	
	
	
	
	public void respond(Invitation inv, boolean ans, boolean pri) throws Exception {
		if (ans == false) {
			Notification noti = new Notification(this);
			inv.meeting.meetingLeader.notifications.add(noti);
			System.out.println(invites.contains(inv));
			invites.remove(inv);
			System.out.println(invites.contains(inv));
			ib.updateInvitation(inv.meeting.getMeetingID(), ans, pri);
		}else {
			invites.remove(inv);
			oldInvites.add(inv);
			cal.addMeeting(inv.meeting);
			ib.updateInvitation(inv.meeting.getMeetingID(), ans, pri);
			if (cal.collidesWith(inv.meeting).isEmpty()) {
				inv.setPriority(true); //setter møtet til prioritet 1
				inv.meeting.addPerson(this);
				// varsle møteleder i meeting om at person har godtatt invitasjonen
			}else {
				if (pri == true) {
					for (Invitation i : oldInvites) {
						if(cal.collidesWith(inv.meeting).contains(i.meeting)){
							i.setPriority(false);
							i.meeting.removePerson(this);
							ib.updateInvitation(i.meeting.getMeetingID(), ans, pri);
						}
					}inv.setPriority(true);
					inv.meeting.addPerson(this);
					// varsle møteleder i meeting om at person har godtatt invitasjonen
				}else {
					inv.setPriority(false);
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Meeting m = new Meeting(new LocalDate());
		Person lars = new Person("larshbj@gmail.com", "kalender", false);
		m.setMeetingLeader(lars);
		m.setMeetingID(59);
		List<Person> p = new ArrayList<Person>();
		p.add(lars);
		Invitation inv = new Invitation(m,p);
		lars.respond(inv, false, false);
		System.out.println(lars.getUserID());
		System.out.println(lars.ib.ownerID);
	}
}
