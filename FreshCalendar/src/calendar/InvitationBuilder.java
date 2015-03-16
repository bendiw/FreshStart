package calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

public class InvitationBuilder extends Database {
	
	protected MeetingBuilder mb;
	protected PersonBuilder pb = new PersonBuilder();
	//database
	protected Statement stmt = null;
	protected ResultSet rs = null;
	protected String query = null;
	protected PreparedStatement pstmt = null;
	protected int ownerID;
	
	public boolean uploadInvitation(Invitation inv) throws Exception{
		try{
			System.out.println(inv.meeting.getMeetingID());
			openConnection();
			for (Person p : inv.getInvitedPersons()) {
				System.out.println(p.getUserID());
				
				pstmt = conn.prepareStatement("INSERT INTO Invitasjon (Bruker_brukerID, Møte_møteID) "
						+ " VALUES (?, ?)");
				pstmt.setInt(1, p.getUserID());
				pstmt.setInt(2, inv.meeting.getMeetingID());
				pstmt.execute();
//				pstmt.setBoolean(3, null);
//				pstmt.setBoolean(4, null);
			}
		}catch(Exception e){
			System.out.println("Could not upload to database. "+e);
			return false;
		}finally{
			closeConnection();
		}
		return true;
	}
	
	public void updateInvitation(int meetingID, boolean conf, boolean prio) throws Exception{
		try {
			openConnection();
			stmt = conn.createStatement();
			String update = "UPDATE Invitasjon SET bekreftet = " + conf + ", prioritet = "+prio+" WHERE Bruker_brukerID = " + this.ownerID + ";";
			stmt.executeUpdate(update);
		}catch(Exception e){
			System.out.println(e);
		} finally {
			closeConnection();
		}
	}
	
	
	public InvitationBuilder(int personID) throws Exception{
		this.ownerID = personID;
		this.mb = new MeetingBuilder(personID);
	}
	
	public boolean invitationIDExists(int meetingID, int brukerID) throws Exception {
		try {
			openConnection();
			pstmt = conn.prepareStatement("SELECT * FROM Invitasjon I WHERE I.Bruker_brukerID = " + brukerID + " AND I.Møte_møteID = " + meetingID + ";" );
			rs = pstmt.executeQuery();
			while (rs.next()) {
				return true;
			}
		} finally {
			closeConnection();
		}
		return false;
	}
	
	public ArrayList<Integer> addPendingIDtoList(int userID) throws Exception {
		ArrayList<Integer> list=new ArrayList<Integer>();
		try {
			openConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT Møte_møteID FROM Invitasjon WHERE Invitasjon.Bruker_brukerID = "+ userID + " AND Invitasjon.bekreftet IS NULL;");
			while(rs.next()) {
				list.add(rs.getInt("Møte_møteID"));
			}
		} finally {
			closeConnection();
		}
		return list;
	}
	
	public ArrayList<Integer> addOldIDtoList(int userID) throws Exception {
		ArrayList<Integer> list=new ArrayList<Integer>();
		try {
			openConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT Møte_møteID FROM Invitasjon WHERE Invitasjon.Bruker_brukerID = "+ userID + " AND Invitasjon.bekreftet IS NOT NULL;");
			while(rs.next()) {
				list.add(rs.getInt("Møte_møteID"));
			}
		} finally {
			closeConnection();
		}
		return list;
	}
	
	
	public ArrayList<Invitation> getAllPendingInvitations() throws Exception{
		ArrayList<Invitation> toReturn = new ArrayList<Invitation>();
		ArrayList<Integer> list = addPendingIDtoList(ownerID);
		for (int meetingID : list) {
			toReturn.add(getInvitation(meetingID, ownerID));
		}
		return toReturn;
	}
	
	public ArrayList<Invitation> getAllOldInvitations() throws Exception{
		ArrayList<Invitation> toReturn = new ArrayList<Invitation>();
		ArrayList<Integer> list = addOldIDtoList(ownerID);
		for (int meetingID : list) {
			toReturn.add(getInvitation(meetingID, ownerID));
		}
		return toReturn;
	}
	
//	public List<Group> getInvitedGroups(int meetingID){
//		ArrayList<Group> invGroups = new ArrayList<Group>();
//		
//		
//	}
	
	public List<Person> getInvitedPersons(int meetingID) throws Exception{
		ArrayList<Person> invited = new ArrayList<Person>();
			try {
				openConnection();
				pstmt = conn.prepareStatement("SELECT Bruker_brukerID FROM Invitasjon I WHERE I.Bekreftet = " + null +" AND I.Møte_møteID = "+meetingID+ ";");
				rs = pstmt.executeQuery();
				while (rs.next()) {
					int user = rs.getInt("Bruker_brukerID");
					Person toAdd = pb.getPerson(user);
					invited.add(toAdd);
//					this.position = rs.getString("stilling");
				}
				for (Person person : invited) {
					System.out.println(person);
				}
				} finally {
					closeConnection();
				}
			return invited;
	}
	
	public Invitation getInvitation(int meetingID, int brukerID) throws Exception{
		Invitation inv;
		Meeting meeting = new Meeting(new LocalDate());
		boolean confirmed=false;
		try {
			meeting = mb.getMeeting(meetingID);
			
		} catch(Exception e) {
			System.out.println("Could not find meeting");
		}
		List<Person> invited = getInvitedPersons(meetingID);
		if (invitationIDExists(meetingID, brukerID)) {
			try {
				openConnection();
				pstmt = conn.prepareStatement("SELECT * FROM Invitasjon I WHERE I.Møte_møteID = " + meetingID + " AND I.Bruker_brukerID = " +brukerID+ ";");
				rs = pstmt.executeQuery();
				while (rs.next()) {
					confirmed = rs.getBoolean("bekreftet");
//					this.position = rs.getString("stilling");
				}
				inv = new Invitation(meeting, confirmed, invited);
				} finally {
					closeConnection();
				}
			return inv;
		} else {
			throw new IllegalArgumentException("Person "+brukerID+" is not invited to this meeting.");
		}
	}
	
	public static void main(String[] args) throws Exception {
		InvitationBuilder ib = new InvitationBuilder(15);
		ib.updateInvitation(59, true, true);
	}
}
