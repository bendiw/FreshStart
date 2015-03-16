package calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;




import org.joda.time.LocalDate;

public class MeetingBuilder extends Database{
		
		private int meetingID;
		private String description;
		private String place;
		private String startTime;
		private String endTime;
		private Integer room;
		private LocalDate date;
		private Person meetingLeader;
		private String title;
		//database
		private Statement stmt = null;
		private ResultSet rs;
		private String query = null;
		private PreparedStatement pstmt = null;
		private int ownerID;
		ArrayList<Integer> list=new ArrayList<Integer>();
		private PersonBuilder pb = new PersonBuilder();
		
		public MeetingBuilder(int ownerID){
			this.ownerID=ownerID;
		}
		
		public int addMeeting(Meeting meeting) throws Exception{
			int toreturn=0;
			
			try {
				int roomID = meeting.getRoom();
				super.openConnection();
				pstmt = conn.prepareStatement("INSERT INTO Møte (tittel, beskrivelse, romID, fraTidspunkt, tilTidspunkt, dato, møtelederID) "
						+ " VALUES (?, ?, ?, ?, ?, ?, ?)", com.mysql.jdbc.PreparedStatement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, meeting.getTitle());
				pstmt.setString(2, meeting.getDescription());
				pstmt.setInt(3, meeting.getRoom());
				pstmt.setString(4, meeting.convertStartTime());
				pstmt.setString(5, meeting.convertEndTime());
				pstmt.setString(6, meeting.getDate().toString());
				pstmt.setInt(7, meeting.getMeetingLeader().getUserID());
				pstmt.execute();
				rs = pstmt.getGeneratedKeys();
				
				while(rs.next()){
					try{
						toreturn = ((int)(rs.getInt(rs.getRow())));
					}finally{
						
					}
				}
				} catch (Exception e) {
				closeConnection();
				super.openConnection();
				pstmt = conn.prepareStatement("INSERT INTO Møte (tittel, beskrivelse, fraTidspunkt, tilTidspunkt, dato, møtelederID) "
						+ " VALUES (?, ?, ?, ?, ?, ?)", com.mysql.jdbc.PreparedStatement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, meeting.getTitle());
				pstmt.setString(2, meeting.getDescription());
				pstmt.setString(3, meeting.convertStartTime());
				pstmt.setString(4, meeting.convertEndTime());
				pstmt.setString(5, meeting.getDate().toString());
				pstmt.setInt(6, meeting.getMeetingLeader().getUserID());
				pstmt.execute();
				rs = pstmt.getGeneratedKeys();
				while(rs.next()){
					try{
						
						toreturn = ((int)(rs.getInt(rs.getRow())));
					}finally{
						
					}
				}
				System.out.println("Could not upload to database. "+e);
			}
			finally {
				closeConnection();
			}
			return toreturn;
	}
	
	public ArrayList<Integer> getRoomList() throws Exception {
		super.openConnection();
		stmt = conn.createStatement();
		rs = stmt.executeQuery("SELECT romID FROM Rom;");
		while(rs.next()) {
			int toAdd = rs.getInt("romID");
			list.add(toAdd);
		}
		return list;
	}
	
	public ArrayList<Integer> getAvailableRoomList(LocalDate date, String start, String end) throws Exception {
		super.openConnection();
		stmt = conn.createStatement();
		rs = stmt.executeQuery("SELECT R.romID " 
				+ "FROM Rom R "
				+ "WHERE R.romID NOT IN (SELECT M.romID FROM Møte M WHERE M.dato = '" + date.toString() + "' AND '" + start + "' between M.fraTidspunkt AND M.tilTidspunkt "
				+ "OR '" + end + "' between M.fraTidspunkt AND M.tilTidspunkt);");
		while(rs.next()) {
			int toAdd = rs.getInt("romID");
			list.add(toAdd);
		}
		return list;
	}
		
		public ArrayList<Integer> addAttendingUserIDs(int meetingID) throws Exception {
			super.openConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT B.BrukerID FROM Bruker B, Invitasjon I, Møte M WHERE M.møteID = I.Møte_møteID AND I.Bruker_brukerID = B.BrukerID "
					+ "AND I.Bekreftet = 1 AND I.prioritet = 1 AND M.møteID = " + meetingID + ";"); 
			while(rs.next()) {
				int toAdd = rs.getInt("brukerID");
				list.add(toAdd);
			}
			return list;
		}
		
		public List<Person> getAllAttending(int meetingID) throws Exception{
			List<Person> toReturn = new ArrayList<Person>();
			ArrayList<Integer> list = addAttendingUserIDs(meetingID);
			for (int userID : list) {
				toReturn.add(pb.getPerson(userID));
			}
			return toReturn;
		}
		
		
			
		public boolean meetingIDExists(int meetingID) throws Exception {
			try {
				openConnection();
				pstmt = conn.prepareStatement("SELECT M.møteID FROM Møte M WHERE M.møteID = " + meetingID + ";");
				rs = pstmt.executeQuery();
				while (rs.next()) {
					return true;
				}
			} finally {
				closeConnection();
			}
			return false;
		}
		
		private String convertLocalDate(LocalDate date){
			return date +"";
		}
		
		public ArrayList<Person> getAttendees(int meetingID) throws Exception{
			ArrayList<Person> toReturn = new ArrayList<Person>();
			ArrayList<Integer> toFetch = new ArrayList<Integer>();
			try{
				super.openConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT Person_personID FROM Invitasjon WHERE Møte.møteID = "+meetingID+" AND Invitasjon.bekreftet = 1;");
				while(rs.next()) {
					toFetch.add(rs.getInt("Person_personID"));
				}
			}finally{
				for (Integer i : toFetch) {
					toReturn.add(pb.getPerson(i));
					closeConnection();
				}
			}
			return toReturn;
		}
		
		
		public ArrayList<Integer> addAttMeetingIDtoList() throws Exception {
			ArrayList<Integer> list = new ArrayList<Integer>();
				super.openConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT møteID FROM Møte, Invitasjon WHERE Møte.møteID = Invitasjon.Møte_møteID AND Invitasjon.Bruker_brukerID = "+ this.ownerID +
						" AND Invitasjon.bekreftet = 1;");
				while(rs.next()) {
					list.add(rs.getInt("møteID"));
				}
				closeConnection();
			return list;
		}
		
		public ArrayList<Integer> addMeetingIDtoList() throws Exception {
			ArrayList<Integer> list = new ArrayList<Integer>();
				super.openConnection();
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT møteID FROM Møte, Invitasjon WHERE Møte.møteID = Invitasjon.Møte_møteID AND Invitasjon.Bruker_brukerID = "+ this.ownerID + ";");
				while(rs.next()) {
					list.add(rs.getInt("møteID"));
				}
				closeConnection();
			return list;
		}
		
		public List<Meeting> getAllAttMeetings() throws Exception{
			List<Meeting> toReturn = new ArrayList<Meeting>();
			ArrayList<Integer> list = addAttMeetingIDtoList();
			for (int meetingID : list) {
				toReturn.add(getMeeting(meetingID));
			}
			return toReturn;
		}
		
		public List<Meeting> getAllMeetings() throws Exception{
			List<Meeting> toReturn = new ArrayList<Meeting>();
			ArrayList<Integer> list = addMeetingIDtoList();
			for (int meetingID : list) {
				toReturn.add(getMeeting(meetingID));
			}
			return toReturn;
		}
		
		public Meeting getMeeting(int meetingID) throws Exception{
			Meeting meeting=null;
			if (meetingIDExists(meetingID)) {
				try {
					openConnection();
					pstmt = conn.prepareStatement("SELECT * FROM Møte WHERE møteID = " + meetingID + ";");
					rs = pstmt.executeQuery();
					while (rs.next()) {
						this.meetingID = rs.getInt("møteID");
						this.description = rs.getString("beskrivelse");
						this.room = rs.getInt("romID");
						this.place = rs.getString("sted");
						this.title = rs.getString("tittel");
						String[] startDate = rs.getString("dato").split("-");
						this.date = new LocalDate(Integer.parseInt(startDate[0]),Integer.parseInt(startDate[1]),Integer.parseInt(startDate[2]));
						String endTime = rs.getString("tilTidspunkt").replace(":","").substring(0,4);
						String fromTime = rs.getString("fraTidspunkt").replace(":", "").substring(0,4);
						this.endTime = endTime;
						this.startTime=fromTime;
						this.meetingLeader = pb.getPerson(rs.getInt("møtelederID"));
					}
					meeting = new Meeting(date, meetingLeader, startTime, endTime, title);
					if(description !=null){
						meeting.setDescription(description);
					}
					meeting.setRoom(room);
					meeting.setAttending(getAttendees(this.meetingID));
					} finally {
						closeConnection();
					}
			} else {
				System.out.println("MøteID " + meetingID + " finnes ikke i databasen");
			}
			return meeting;
		}
		
		public static void main(String[] args) throws Exception{
			MeetingBuilder mb = new MeetingBuilder(15);
			System.out.println(mb.getAllMeetings());
		}
}
