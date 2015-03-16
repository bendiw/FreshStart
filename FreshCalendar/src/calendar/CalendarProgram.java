package calendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;








import org.joda.time.LocalDate;

public class CalendarProgram {

	private Person p;
	private GeneralCal c;
	private Scanner s;
	private CalendarPrinter printer;
	private PersonBuilder pb;
	private PersonUpdater pu;
	private MeetingBuilder mb;
	private InvitationBuilder ib;
	
	public String getInvitationCount(){
		return p.getInv().size()+" pending invitations.";
	}
	
	public String getNotifications(){
		try{
			return p.getNotifications().size()+" pending notifications.";
		}catch(Exception e){
			return "No pending notifications.";
		}
	}
	
	
	
	public void init(){
		c = new GeneralCal();
//		p = new Person(c);
		s = new Scanner(System.in);
		printer = new CalendarPrinter();
		pb = new PersonBuilder();
		pu = new PersonUpdater();
	}
	
	public String getMenu(){
		return "Menu:\n1. View invitations\n2. Add meeting\n3. View single day\n4. Next month\n5. Previous month\n6. See notifications";
	}
	
	public String getDayName(LocalDate date){
		int day = date.getDayOfWeek();
		String dayName = "";
		switch (day) {
		case 1:
			dayName="Monday";
			break;
		case 2:
			dayName="Tuesday";
			break;
		case 3:
			dayName="Wednesday";
			break;
		case 4:
			dayName="Thursday";
			break;
		case 5:
			dayName="Friday";
			break;
		case 6:
			dayName="Saturday";
			break;
		case 7:
			dayName="Sunday";
			break;
		}
		return dayName;
	}
	
	public static void sortAgenda(List<Meeting> agenda)
	{
	     int j;                     
	     int key;          
	     int i; 
	     Meeting m;
	     for (j = 1; j < agenda.size(); j++){
	    	 m=agenda.get(j);
	    	 key = Integer.parseInt(agenda.get(j).getStartTime());
	    	 for(i = j - 1; (i >= 0) && (Integer.parseInt(agenda.get(i).getStartTime()) > key); i--){
	    		 agenda.set(i+1,agenda.get(i));
	          }
	          agenda.set(i+1,m);  
	     }
	}
	
	public void editMeeting(LocalDate date){
		System.out.println("Choose meeting to edit by entering title or hit Enter to exit..."); //will be changed to accept meetingID(int)
		Meeting toEdit=null;
		while(true){
			try{
				String input = s.nextLine();
				if(input.isEmpty()){
					return;
				}
				for (Meeting  m : c.getDayAgenda(date)) {
					if(input.toLowerCase().equals(m.getTitle().toLowerCase())){
						toEdit=m;
					}
				}
			}catch(Exception e){
				System.out.println("Could not find meeting. Try again or hit Enter to exit...");
			}
			if(toEdit!=null){
				break;
			}else{
				System.out.println("Could not find meeting. Try again or hit Enter to exit...");
			}
		}
		Meeting oldMeeting = new Meeting(new LocalDate());
		oldMeeting.cloneFields(toEdit);
		try{
			while(true){
				System.out.println("Editing:\n\n"+toEdit+"\n");
				System.out.println("Choose attribute to change or press Enter to exit:\n1. Title\n2. Start time\n3. End time\n4. Description\n5. Attending");
				int choice = Integer.parseInt(s.nextLine());
				switch (choice) {
				case 1:
					System.out.println("Enter new title...");
					String newTitle = s.nextLine();
					toEdit.setTitle(newTitle);
					System.out.println("Title updated!\n");
					break;
				case 2:
					System.out.println("Enter new start time, format HHMM...");
					while(true){
						try{
							String newStart = s.nextLine();
							toEdit.setStartTime(newStart);
							System.out.println("Updated start time!");
							break;
						}catch(Exception e){
							System.out.println("Invalid time");
						}
					}
					break;
				case 3:
					System.out.println("Enter new end time, format HHMM...");
					while(true){
						try{
							String newEnd = s.nextLine();
							toEdit.setEndTime(newEnd);
							System.out.println("Updated end time!");
							break;
						}catch(Exception e){
							System.out.println("Invalid time");
						}
					}
					break;
				case 4: 
					System.out.println("Enter new description...");
					String newDesc = s.nextLine();
					toEdit.setDescription(newDesc);
					System.out.println("Updated description!");
					break;
				case 5:
					printPersonList(toEdit.getMeetingID());
					break;
				default:
					break;
				}
			}
		}catch(Exception e){
			toEdit.fireNotification(new Notification(toEdit.getChanges(oldMeeting)));
			return;
		}
	}	
	
	public void showSingleDay(LocalDate toView) throws Exception{
		List<Meeting> agenda = c.getDayAgenda(toView);
		System.out.println("--- Viewing Single Day ---");
		System.out.println(getDayName(toView)+"\t"+toView.getDayOfMonth()+".\t"+printer.getMonthName(toView.getMonthOfYear()));
		if(!agenda.isEmpty()){
			sortAgenda(agenda);
			for (Meeting meeting : agenda) {
				System.out.println("------------------------------------------");
				System.out.println(meeting+"\n");
			}
		}else{
			System.out.println("No events");
		}
		System.out.println("------------------------------------------");
		System.out.println("1. Next day\n2. Previous day\n3. Edit or remove meeting\n4. Create meeting\nHit Enter to exit");
		while(true){
			String input = s.nextLine();
			if(input.isEmpty()){
				return;
			}
			if(Integer.parseInt(input)==1){
				showSingleDay(toView.plusDays(1));
			}else if(Integer.parseInt(input)==2){
				showSingleDay(toView.minusDays(1));
			}else if(Integer.parseInt(input)==3){
				editMeeting(toView);
				break;
			}else if(Integer.parseInt(input)==4){
				inputForMeeting(true);
//				showSingleDay(toView); Kan legges til dersom det er �nskelig � g� tilbake til single date. Nested call problem?
				break;
			}
			System.out.println("Invalid command!");
		}
	}
	
	public ArrayList<Person> printPersonList(int meetingID) throws Exception{
		List<Person> uninvPersons = pb.getUninvitedPersons(meetingID);
		List<Person> allPersons = pb.getAllPersons();
		ArrayList<Person> toInvite = new ArrayList<Person>();
		System.out.println("Select option or type 'done' to finish creating meeting...\n1. See your groups\n2. See full list of persons\n3. Search for person to add");
		String userInput = s.nextLine();
		while(true){
			if(userInput.toLowerCase().equals("done")){
				break;
			}
			switch (Integer.parseInt(userInput)) {
			case 1:
				if(!p.getGroups().isEmpty()){
					for (Group g : p.getGroups()) {
					System.out.println(g+"\n");
					System.out.println("Your groups. Type 'add <groupname>' to add all members of a group, or simply type a group's name to see its members.");
					}
				}else{
					System.out.println("You are not a member of any groups.");
				}
				//group printing implementation goes here
				break;
			case 2: //full person list printing implementation goes here
				try{
					for (Person people : allPersons) {
						boolean trigger = true;
						for (Person bros : uninvPersons) {
							System.out.println(people.getUserID() + "  "+bros.getUserID());
							if(bros.getUserID()==people.getUserID()){
								trigger = false;
								System.out.println("match!");
							}
							
						}
						if(p.getUserID()==this.p.getUserID()){
							break;
						}
						if(trigger){
							System.out.print("[Already invited] ");
						}
						System.out.println(p+"\n");
					}
					System.out.println("Add person by entering ID number. Press Enter to exit.");
					while(true){
						String input = s.nextLine();
						for (Person p : allPersons) {
							if(p.getUserID()==Integer.parseInt(input)){
							toInvite.add(p);
							System.out.println("Added "+p.getName()+" to list of invited persons.");
							}
						}
					}
				}catch(Exception e){
					return toInvite;
				}
				
				case 3: 
				System.out.println("Not implemented!");
//				userInput = s.nextLine();
				//searching implementation goes here
				break;
			default:
			}
			userInput = s.nextLine();
		}
		return toInvite;
	}
	
	public void inputForMeeting(boolean today) throws Exception{
		LocalDate localDateToEnter;
		if(!today){
			
		System.out.print("Creating meeting. Enter date or press enter for today.\nFormat for date is DD MM YYYY...\n");
		String date = s.nextLine();
		if(date.isEmpty()){
			localDateToEnter = new LocalDate();
			System.out.println("Set date for default (today).\n");
		}else{
			while(true){
				try{
					int[] dateInput=new int[3];
					String[] dateSplit = date.split(" ");
					for (int i = 0; i < 3; i++) {
						dateInput[i]=Integer.parseInt(dateSplit[i]);
					}
					localDateToEnter = new LocalDate(dateInput[2], dateInput[1], dateInput[0]);
					break;
				}catch(Exception e){
					System.out.println("Invalid date input format!");
				}
			}
		}
		}else{
			localDateToEnter=new LocalDate();
			System.out.println("Creating meeting on "+localDateToEnter+".");
		}
		Meeting meeting = new Meeting(localDateToEnter, this.p);
		System.out.println("Enter title...");
		String titleInput = s.nextLine();
		meeting.setTitle(titleInput);
		System.out.println("Enter start time, format HHMM...");
		String startInput, endInput = "";
		while(true){
			try{
				startInput = s.nextLine().substring(0, 4);
				meeting.setStartTime(startInput);
				break;
			}catch(Exception e){
				System.out.println("Invalid time format!");
			}
		}
		System.out.println("Enter end time, format HHMM");
		while(true){
			try{
				endInput=s.nextLine().substring(0, 4);
				meeting.setEndTime(endInput);
				break;
			}catch(Exception e){
				System.out.println("Invalid time format!");
			}
		}
		System.out.println("Available rooms:");
		List<Integer> roomList = mb.getAvailableRoomList(meeting.getDate(), meeting.convertStartTime(), meeting.convertEndTime());
		for (int roomID : roomList) {
			System.out.println(roomID);
		}
		System.out.println("Select room by typing room number or press Enter to skip.");
		while(true){
			try{
				String input = s.nextLine();
				if(roomList.contains(Integer.parseInt(input))){
					meeting.setRoom((Integer.parseInt(input)));
					System.out.println("Chose room "+input+".");
					break;
				}else{
					System.out.println("Room not in list.");
				}
			}catch(Exception e){
				meeting.setRoom(null);
				break;
			}
		}
		System.out.println("Add description or press enter to skip...");
		String desc = s.nextLine();
		if(desc.isEmpty()){
			meeting.setDescription("No description");
		}else{
			meeting.setDescription(desc);
		}
		int ID = mb.addMeeting(meeting);
		meeting.setMeetingID(ID);
		meeting.setMeetingLeader(p);
		System.out.println("Add attendees now?\n1. Yes\n2. No");
		String peopleInput = s.nextLine();
//				for(Person p : persons){
//					System.out.println(p.getIDno()+"\t"+p.getName());
//				}
//				}else if(persons.contains(addInput)){
//					meeting.addPerson(persons.get(addInput));
//					System.out.println("Person added!");
//				}
		if(meeting.getMeetingID()==0){
			System.out.println("Meeting creation failed! Something went wrong while uploading to DB.");
			return;
		}else{
			try{
				if(Integer.parseInt(peopleInput) ==1){
					List<Person>toInvite = printPersonList(meeting.getMeetingID());
					System.out.println(meeting.getMeetingID());
					ib.uploadInvitation(new Invitation(meeting, toInvite));
				}
			}catch(Exception e){
				
			}
		p.createMeeting(meeting);
		System.out.println("Meeting created! Summary:");
		System.out.println("Date: "+localDateToEnter);
		System.out.println(meeting);
		}
	}
	
	public void createUser(){
		System.out.println("Creating new user...");
		System.out.println("Enter userID...");
		while(true){
			try{
				p.setUserID(Integer.parseInt(s.nextLine().trim()));
				break;
			}catch(Exception e){
				System.out.println("IDnumber must be an integer!");
			}
		}
		System.out.println("Enter name...");
			while(true){
				try{
					String input = s.nextLine();
					p.setName(input);
					break;
				}catch(Exception e){
					System.out.println("Invalid input for name!");
				}
			}
		System.out.println("Personalia updated!");
	}
	
	public void updateMeetingList() throws Exception{
		this.c.clearMaps();
		List<Meeting> meetings = mb.getAllMeetings();
		this.c.mapMeetings(meetings);
		List<Meeting> attMeetings = mb.getAllAttMeetings();
		this.c.mapAttMeetings(attMeetings);
	}
	
	
	public void singleInvHandler(Invitation inv){
		System.out.println("Responding to following invitation:");
		System.out.println(inv);
		if(c.collidesWith(inv.meeting).isEmpty()){
			while(true){
				try{
					System.out.println("Press Enter to exit. Menu:\n1. Accept invitation\n2. Decline invitation");
					int choice = Integer.parseInt(s.nextLine());
					if(choice==1){
						System.out.println("Confirmed!");
						p.respond(inv, true, true);
						return;
					}else if(choice==2){
						System.out.println("Confirmed!");
						p.respond(inv,false,false);
						return;
					}else{
						System.out.println("Invalid command.");
					}
				}catch(Exception e){
					return;
				}
			}
		}
		System.out.println("Collides with following events:");
		for (Meeting m : c.collidesWith(inv.meeting)) {
			System.out.println(m);
		}
		while(true){
			try{
				System.out.println("Press Enter to exit. Menu:\n1. Attend this meeting, prioritized\n2. Attend this meeting, low priority\n3. Decline meeting");
				int choice = Integer.parseInt(s.nextLine());
				System.out.println(choice);
				if(choice==1){
					System.out.println("Confirmed!");
					p.respond(inv, true, true);
					return;
				}else if(choice==2){
					System.out.println("Confirmed!");
					p.respond(inv,true,false);
					return;
				}else if(choice==3){
					System.out.println("Confirmed!");
					p.respond(inv,false,false);
					return;
				}else{
					System.out.println("Invalid command.");
				}
			}catch(Exception e){
				return;
			}
		}
	}
	
	public void invitationHandler(){
		System.out.println("Current pending invitations:");
		for (Invitation inv : p.getInv()) {
			System.out.print((p.getInv().indexOf(inv)+1)+". ");
			System.out.println(inv+"\n\n");
		}
		try{
			System.out.println("Enter an invitation's number to respond, or press Enter to exit.");
			while(true){
				String input = s.nextLine();
				try{
					singleInvHandler(p.getInv().get(Integer.parseInt(input)-1));
					return;
				}catch(Exception e){
					break;
				}
			}
		}catch(Exception e){
			System.out.println(p.getInv().size());
			return;
		}		
	}
	
	public void run() throws Exception{
//		Person user1 = null;
//		System.out.println(user1);
		String choice1;
		while(p==null){
			System.out.println("Welcome!");
			System.out.println("Choose an action...\n1. Login\n2. New User");
			choice1 = s.nextLine().trim();
			try{
//				if(Integer.parseInt(choice1)==1){
//					System.out.println("Not implemented!");			
//				}else if(Integer.parseInt(choice1)!=2){
//					System.out.println("Invalid command!");
//				}else{
//					createUser();
//					break;
//				}
				System.out.println("Skriv inn email");
				String email = s.nextLine();
				System.out.println("Skriv inn passord");
				String password = s.nextLine();
				if(Integer.parseInt(choice1)==2) {
					System.out.println("Fornavn:");	
					String firstname = s.nextLine();
					System.out.println("Etternavn:");
					String lastname = s.nextLine();
					System.out.println("Adresse:");
					String address = s.nextLine();
					System.out.println("Mobilnummer:");
					int mobile = Integer.parseInt(s.nextLine());
					System.out.println("Postnummer:");
					int postnr = Integer.parseInt(s.nextLine());
					System.out.println("Stilling:");
					String position = s.nextLine();
					p = new Person(email, password, true);
					p.setCal(c);
					pu.updateAll(firstname, lastname, address, postnr, mobile, position, p.getEmail());
					System.out.println("\n\n" + "Grattis, du har n� en bruker!" + "\n" + "Velkommen til kalender din");
					break;
				} else {
					p = new Person(email, password, false);
					p.setCal(c);
					break;
				}
			}catch(IllegalArgumentException e) {
				System.out.println("Pr�v p� nytt" + "\n");
				continue;
			}catch(Exception e){
				System.out.println("Please enter a command.");
			}
			choice1 = s.nextLine().trim();
		}
		mb = new MeetingBuilder(p.getUserID());
		updateMeetingList();
		ib = new InvitationBuilder(p.getUserID());
		System.out.println(p.getUserID());
		printer.print(c);
		p.invites.clear();
		p.invites = ib.getAllPendingInvitations();
		p.oldInvites.clear();
		p.oldInvites = ib.getAllOldInvitations();
		System.out.println(getNotifications());
		System.out.println(getInvitationCount());
		System.out.println("Press enter to show menu.");
		while(true){
			p.invites.clear();
			p.invites = ib.getAllPendingInvitations();
			p.oldInvites.clear();
			p.oldInvites = ib.getAllOldInvitations();
			updateMeetingList();
			int choice=0;
			try{
				choice = Integer.parseInt(s.nextLine());
			}catch(Exception e){
				System.out.println(getMenu());
			}
			if(choice==1){
				invitationHandler();
				printer.print(c);
				System.out.println(getNotifications());
				System.out.println(getInvitationCount());
			}else if(choice ==2){
				inputForMeeting(false);
				printer.print(c);
				System.out.println(getNotifications());
				System.out.println(getInvitationCount());
			}else if(choice==3){
				System.out.println("Hit Enter for today, or input date to view, format DD MM YYYY...");
				LocalDate toView;
				while(true){
					try{
						String date = s.nextLine();
						if(date.isEmpty()){
								toView = new LocalDate();
								break;
						}else{
							int[] dateInput=new int[3];
							String[] dateSplit = date.split(" ");
							for (int i = 0; i < 3; i++) {
								dateInput[i]=Integer.parseInt(dateSplit[i]);
							}
							toView = new LocalDate(dateInput[2],dateInput[1],dateInput[0]);
							break;
						}
					}catch(Exception e){
							System.out.println("Format was wrong! Correct format for date is DD MM YYYY");
						}
					}
				showSingleDay(toView);
				printer.print(c);
				System.out.println(getNotifications());
				System.out.println(getInvitationCount());

			}else if(choice ==4){
				c.rollMonth(true);
				printer.print(c);
				System.out.println(getNotifications());
				System.out.println(getInvitationCount());


			}else if(choice ==5){
				c.rollMonth(false);
				printer.print(c);
				System.out.println(getNotifications());
				System.out.println(getInvitationCount());

			}else if(choice == 6){
				for (Notification n : p.getNotifications()) {
					System.out.println(n);
				}
			}
		}
	}
	
//	public static void main(String[] args) {
//		CalendarProgram cp = new CalendarProgram();
//		cp.init();
//		cp.run();
//	}
	
	public static void main(String[] args) throws Exception {
		CalendarProgram cp = new CalendarProgram();
		cp.init();
		cp.run();
	}
	
}
