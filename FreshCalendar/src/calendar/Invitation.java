package calendar;

import java.util.List;

public class Invitation {

	protected Meeting meeting;
	private List<Person> invitedPersons;
	private List<Group> invitedGroups;
	protected Boolean priority;
	protected boolean confirmed;
	
	public List<Person> getInvitedPersons(){
		return this.invitedPersons;
	}
	
	public Invitation(Meeting meeting, boolean confirmed, List<Person> invitedPersons){
		this.meeting = meeting;
		this.invitedPersons = invitedPersons;
		this.confirmed = confirmed;
	}
	
	public Invitation(Meeting m, List<Person> invitedPersons){
		this.meeting = m;
		this.invitedPersons=invitedPersons;
	}
	
	private void sendInviteToGroups() {
		for (Group g : invitedGroups) {
			for (Person p : g.members) {
				p.addInvitation(this);
			}
		}
	}
		
	private void sendInviteToPersons() {
		for (Person p : invitedPersons) {
			p.addInvitation(this);				
		}
	}
	
	public void setPriority(boolean pri) {
		this.priority = pri;
	}
	
	public String toString(){
		return "Invited by "+this.meeting.meetingLeader.getName()+" to:\n"+this.meeting.getDate().toString()+"\n"+this.meeting;
	}
	
	
	public static void main(String[] args) {
		System.out.println("hrei");
	}
		
	
	
}
