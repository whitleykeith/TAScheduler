package comp110;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.List;

import com.google.gson.Gson;

import java.io.PrintWriter;

public class Parser {
	public Employee parseEmployee(String file) {
		File csv = new File(file);
		BufferedReader csvReader = null;
		String onyen = csv.getName().substring(0, csv.getName().length() - 4);
		String name = "";
		String gender = "";
		int capacity = 0;
		int level = 0;
		try {
			csvReader = new BufferedReader(new FileReader(csv));
			// parsing employee info
			name = csvReader.readLine().split(",")[1]; // ugly
			gender = csvReader.readLine().split(",")[1];
			capacity = Integer.parseInt(csvReader.readLine().split(",")[1]);
			level = Integer.parseInt(csvReader.readLine().split(",")[1]);
		} catch (Exception e) {
			System.err.println("Error parsing: " + name);
			e.printStackTrace();
			return null;
		}

		try {
			csvReader.readLine();
		} catch (IOException e1) {
			System.err.println("IO Error: " + name);
			e1.printStackTrace();
		} // throw away header line with days

		// read in schedule
		int[][] availability = new int[7][24];
		for (int hour = 0; hour < 24; hour++) {
			String scheduleLine = "";
			try {
				scheduleLine = csvReader.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.err.println("IO Error: " + name);
				e1.printStackTrace();
				return null;
			}
			for (int day = 0; day < 7; day++) {
				// Offset by 1 accounts for label in CSV
				try {
					availability[day][hour] = Integer.parseInt(scheduleLine.split(",")[day + 1]);
				} catch (NumberFormatException e) {
					System.err.println("Error:" + name);
					e.printStackTrace();
					return null;
				}
			}
		}
		try {
			csvReader.close();
		} catch (IOException e) {
			System.err.println("Error:" + name);
			e.printStackTrace();
			return null;
		}
		return new Employee(name, onyen, capacity, gender.equals("M") ? false : true, level, availability);
	}

	public Schedule parseSchedule(String jsonFile) {

		Gson gson = new Gson();
		BufferedInputStream reader = null;
		try {
			//we need to read in the raw bytes so we can specify size of buffer, the jsons are large
			reader = new BufferedInputStream(new FileInputStream(new File(jsonFile)), 65536);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String json = "";
		try {
			while (reader.available() > 0) {
				json += (char) reader.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		JsonWeek jsonweek = gson.fromJson(json, JsonWeek.class);
		//now we have the json we need to reconstruct the schedule object
		//first we will build the staff object
		Staff staff = null;
		try {
			staff = parseStaff("data/spring-17/staff/");
		} catch (IOException e) {
			e.printStackTrace();
		}
		//now build the week object
		Week week = new Week("Current Schedule");
		//grab a reference to the shifts array
		Shift[][] shifts = week.getShifts();
		for (int day = 0; day < 7; day++){
			for (int hour = 0; hour < 24; hour++){
				for (int i = 0; i < jsonweek.getShifts()[day][hour].getScheduled().length; i++){
					shifts[day][hour].add(this.getEmployeeByOnyen(jsonweek.getShifts()[day][hour].getScheduled()[i], staff));
				}
			}
		}
		return new Schedule(staff, week);
	}

	public void writeFile(Employee employee, String filename) throws IOException {

		// This works but I dont know if its ideal
		/*
		 * String filePath = new File("").getAbsolutePath(); //get project path
		 * String folder = "data/test/" + employee.getOnyen() + ".csv"; //get
		 * file location, currently just a test folder filePath =
		 * filePath.replace("src",folder); //replace src
		 */
		System.out.println("Writing Schedule to" + filename);
		PrintWriter fw = new PrintWriter(filename);
		StringBuilder sb = new StringBuilder();

		// Name
		sb.append("Name:,");
		sb.append(employee.getName());
		sb.append(",,,,,,\n");

		// Gender
		sb.append("Gender (enter M or F):,");

		if (employee.getIsFemale()) {
			sb.append("F");
		} else {
			sb.append("M");
		}
		sb.append(",,,,,,\n");

		// Capacity
		sb.append("Capacity:,");
		sb.append(employee.getCapacity());
		sb.append(",,,,,,\n");

		// Level
		sb.append("Level (1 - in 401; 2 - in 410/411; 3 - in major),");
		sb.append(employee.getLevel());
		sb.append(",,,,,,\n");

		// Week
		sb.append(",0. Sun,1. Mon,2. Tue,3. Weds,4. Thu,5. Fri,6. Sat\n");

		// Schedule
		int[][] avail = employee.getAvailability();

		// for loop is backwards to write line by line
		for (int hour = 0; hour < 24; hour++) {
			sb.append(hour);
			for (int day = 0; day < avail.length; day++) {
				sb.append(',');
				sb.append(avail[day][hour]);
			}
			sb.append('\n');
		}
		fw.write(sb.toString());
		fw.close();
	}

	public static void main(String[] args) {
		Parser parser = new Parser();
		parser.parseSchedule("testData/schedule.json");
		// Employee emp =
		// parser.parseEmployee("/home/keith/git/TAScheduler/data/spring-17/staff/aatieh.csv");
		//
		// try {
		// parser.writeFile(emp, "test.csv");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	private Staff parseStaff(String dir) throws IOException {
		Staff staff = new Staff();
		File csvDirectory = new File(dir);
		BufferedReader csvReader = null;
		int counter = 0;
		for (File csv : csvDirectory.listFiles()) {
			String onyen = csv.getName().substring(0, csv.getName().length() - 4);
			String name = "";
			String gender = "";
			int capacity = 0;
			int level = 0;
			try {
				csvReader = new BufferedReader(new FileReader(csv));
				// parsing employee info
				name = csvReader.readLine().split(",")[1]; // ugly
				gender = csvReader.readLine().split(",")[1];
				capacity = Integer.parseInt(csvReader.readLine().split(",")[1]);
				level = Integer.parseInt(csvReader.readLine().split(",")[1]);
				counter++;
			} catch (Exception e) {
				System.err.println("Error parsing: " + name + counter);
				e.printStackTrace();
			}

			csvReader.readLine(); // throw away header line with days

			// read in schedule
			int[][] availability = new int[7][24];
			for (int hour = 0; hour < 24; hour++) {
				String scheduleLine = csvReader.readLine();
				for (int day = 0; day < 7; day++) {
					// Offset by 1 accounts for label in CSV
					try {
						availability[day][hour] = Integer.parseInt(scheduleLine.split(",")[day + 1]);
					} catch (NumberFormatException e) {
						System.err.println("Error:" + name);
						e.printStackTrace();
					}
				}
			}
			staff.add(new Employee(name, onyen, capacity, gender.equals("M") ? false : true, level, availability));
		}
		return staff;
	}

	private Employee getEmployeeByOnyen(String onyen, Staff staff) {
		for (Employee e : staff) {
			if (e.getOnyen().equals(onyen)) {
				return e;
			}
		}
		// not able to find employee
		return null;
	}
}
