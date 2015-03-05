package calendar;

import java.util.List;
import java.util.Scanner;
import org.joda.time.LocalDate;

public class CalendarProgram {

	private Person p;
	private GeneralCal c;
	private Scanner s;
	private CalendarPrinter printer;
	
	public void printInvitations(){
//		LocalDate date;
		if(p.invites.isEmpty()){
			System.out.println("Currently no invitations.");
			return;
		}
		for (Invitation inv : p.invites) {
//			date = inv.meeting.getDate();
			System.out.println(inv);
		}
		String invMenu = "Enter the invitation's index number to respond, or press Enter to exit to main menu.";
		String input = s.nextLine();
		while(true){
			if(input.isEmpty()){
				return;
			}
			if(Integer.parseInt(input)<=p.invites.size()&&Integer.parseInt(input)>0){
				System.out.println("Responding to the following invitation:\n"+p.invites.get(Integer.parseInt(input)));
			}
		}
	}
	
	public void init(){
		c = new GeneralCal();
		p = new Person(c);
		s = new Scanner(System.in);
		printer = new CalendarPrinter();
	}
	
	public String getMenu(){
		return "Menu:\n1. View invitations\n2. Add meeting\n3. View single day\n4. Next month\n5. Previous month\n";
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
				default:
					break;
				}
			}
		}catch(Exception e){
			return;
		}
	}
	
	public void showSingleDay(LocalDate toView){
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
	
	public void printPersonList(){
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
					System.out.println(g);
					System.out.println("Your groups. Type 'add <groupname>' to add all members of a group, or simply type a group's name to see its members.");
					}
				}else{
					System.out.println("You are not a member of any groups.");
				}
				//group printing implementation goes here
				break;
			case 2: //full person list printing implementation goes here
			case 3: 
				System.out.println("Not implemented!");
//				userInput = s.nextLine();
				//searching implementation goes here
				break;
			default:
			}
			userInput = s.nextLine();
		}
		
	}
	
	public void inputForMeeting(boolean today){
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
//		Meeting meeting = new Meeting(localDateToEnter,this.p,startInput,endInput, titleInput);
		System.out.println("Add description or press enter to skip...");
		String desc = s.nextLine();
		if(desc.isEmpty()){
			meeting.setDescription("No description");
		}else{
			meeting.setDescription(desc);
		}
		System.out.println("Add attendees now?\n1. Yes\n2. No");
		String peopleInput = s.nextLine();
		try{
			if(Integer.parseInt(peopleInput) ==1){
				printPersonList();
			}
		}catch(Exception e){
			
		}
//				for(Person p : persons){
//					System.out.println(p.getIDno()+"\t"+p.getName());
//				}
//				}else if(persons.contains(addInput)){
//					meeting.addPerson(persons.get(addInput));
//					System.out.println("Person added!");
//				}
		p.createMeeting(meeting);
		System.out.println("Meeting created! Summary:");
		System.out.println("Date: "+localDateToEnter);
		System.out.println(meeting);
	}
	
	public void createUser(){
		System.out.println("Creating new user...");
		System.out.println("Enter IDno...");
		while(true){
			try{
				p.setIDno(Integer.parseInt(s.nextLine().trim()));
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
	
	public void run(){
		System.out.println("Welcome!");
		System.out.println("Choose an action...\n1. Login\n2. New User");
		String choice1 = s.nextLine().trim();
		while(true){
			try{
				if(Integer.parseInt(choice1)==1){
					System.out.println("Not implemented!");			
				}else if(Integer.parseInt(choice1)!=2){
					System.out.println("Invalid command!");
				}else{
					createUser();
					break;
				}
			}catch(Exception e){
				System.out.println("Please enter a command.");
			}
			choice1 = s.nextLine().trim();
		}
		printer.print(c);
		System.out.println("Press enter to show menu.");
		while(true){
			int choice=0;
			try{
				choice = Integer.parseInt(s.nextLine());
			}catch(Exception e){
				System.out.println(getMenu());
			}
			if(choice==1){
				printInvitations();
				printer.print(c);
			}else if(choice ==2){
				inputForMeeting(false);
				printer.print(c);
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
			}else if(choice ==4){
				c.rollMonth(true);
				printer.print(c);
			}else if(choice ==5){
				c.rollMonth(false);
				printer.print(c);
			}else if(choice == 6){
				System.out.println("Not yet implemented!");
			}
		}
				
				
		}	
	
	public static void main(String[] args) {
		CalendarProgram cp = new CalendarProgram();
		cp.init();
		cp.run();
	}
	
}
