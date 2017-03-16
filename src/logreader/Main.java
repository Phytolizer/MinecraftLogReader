package logreader;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class Main {

	private static List<String> connectedPlayers;
	private static HashMap<String, TimeStamp> lastLogins;
	private static boolean printSessionLength = false;
	private static boolean printNumPlayersPerDay = false;

	public static void main(String[] args) {
		String location = ".";
	    for(int i = 0; i < args.length; i++) {
	    	String arg = args[i];
	    	if(arg.startsWith("-")) {
	    		String command = arg.substring(1);
	    		switch(command) {
				    case "loc":
				    	i++;
				    	if(isValidLocation(args[i]))
				    		location = args[i];
				    	else {
						    System.out.println("Bad location argument!");
						    System.exit(1);
					    }
					    break;
				    case "help":
				    	printHelpInformation();
				    	System.exit(0);
				    	break;
				    default:
				    	System.out.println("Unknown argument: " + command);
				    	break;
			    }
		    } else {
	    		if(arg.contains("s")) {
	    			printSessionLength = true;
			    }
			    if(arg.contains("n")) {
	    			printNumPlayersPerDay = true;
			    }
		    }
	    }
		File[] logs = getLogs(location);
	    connectedPlayers = new ArrayList<>();
		lastLogins = new HashMap<>();
		for(File log : logs) {
			try {
				printTimestamps(log);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
    }

	private static void printHelpInformation() {
		System.out.println("Minecraft Log Reader usage: ");
		System.out.println("Commands:");
		System.out.println("\t-loc <path>");
		System.out.println("\t Tells the reader where the logs are. Uses the current folder by default.");
		System.out.println("Flags:");
		System.out.println("\ts - Enables printing each session length for each user. Format: username\tlength, in seconds");
		System.out.println("\tn - Enables printing activity timestamps. Format: time\tplayercount, time in seconds since the day began.");
	}

	private static void printTimestamps(File log) throws FileNotFoundException {
		Scanner s = new Scanner(log);
		while(s.hasNext()) {
			String line = s.nextLine();

			if(line.contains(" logged in with entity id ")) {
				//print connect message
				String timeStamp = log.getName().substring(0, 10) + " " + line.substring(line.indexOf('[') + 1, line.indexOf(']'));
				String playerName = line.substring(line.indexOf("]: ") + 3);
				playerName = playerName.substring(0, playerName.indexOf("[/"));
				connectedPlayers.add(playerName);
				TimeStamp time = new TimeStamp(timeStamp);
				lastLogins.put(playerName, time);
				if(printNumPlayersPerDay) {
					int seconds = getSecondOfDay(timeStamp);
					System.out.println("" + seconds + "\t" + connectedPlayers.size());
				}
			} else if(line.contains(" lost connection: ")) {
				if(line.contains("Mod rejections"))
					continue;
				//print d/c message
				String timeStamp = log.getName().substring(0, 10) + " " + line.substring(line.indexOf('[') + 1, line.indexOf(']'));
				String playerName = line.substring(line.indexOf("]: ") + 3, line.indexOf(" lost conn"));
				if (playerName.contains("GameProfile")) {
					String cutString = playerName.substring(playerName.indexOf("name=") + 5);
					playerName = cutString.substring(0, cutString.indexOf(','));
				}
				connectedPlayers.remove(playerName);
				Duration sessionLength = lastLogins.get(playerName).subtractFrom(new TimeStamp(timeStamp));
				if(printSessionLength) System.out.println(sessionLength.getSeconds());
				if(printNumPlayersPerDay) {
					int seconds = getSecondOfDay(timeStamp);
					System.out.println("" + seconds + "\t" + connectedPlayers.size());
				}
			} else if(line.contains("[Server Shutdown Thread/INFO]: Stopping server")) {
				//d/c all connected players
				String timeStamp = log.getName().substring(0, 10) + " " + line.substring(line.indexOf('[') + 1, line.indexOf(']'));
				while(connectedPlayers.size() > 0) {
					connectedPlayers.remove(0);
				}
				if(printNumPlayersPerDay) {
					int seconds = getSecondOfDay(timeStamp);
					System.out.println("" + seconds + "\t" + connectedPlayers.size());
				}
			} else if(line.contains("]: Starting minecraft server version ")) {
				String timeStamp = log.getName().substring(0, 10) + " " + line.substring(line.indexOf('[') + 1, line.indexOf(']'));
				connectedPlayers = new ArrayList<>();
				if(printNumPlayersPerDay) {
					int seconds = getSecondOfDay(timeStamp);
					System.out.println("" + seconds + "\t" + connectedPlayers.size());
				}
			}
		}
	}

	private static File[] getLogs(String location) {
		File[] out;
		File folder = new File(location);
		if(folder.listFiles() == null || folder.listFiles().length == 0) {
			return new File[0];
		}
		//count
		int numLogs = 0;
		for(File f : folder.listFiles()) {
			if(f.getName().endsWith(".log")) {
				numLogs++;
			}
		}
		if(numLogs == 0) {
			return new File[0];
		}
		out = new File[numLogs];
		int i = 0;
		for(File f : folder.listFiles()) {
			if(f.getName().endsWith(".log")) {
				out[i] = f;
				i++;
			}
		}
		return out;
	}

	private static boolean isValidLocation(String loc) {
    	File f = new File(loc);
	    return f.exists() && f.isDirectory();
    }
	public static String formatDuration(Duration duration) {
		long seconds = duration.getSeconds();
		long absSeconds = Math.abs(seconds);
		String positive = String.format(
				"%d:%02d:%02d",
				absSeconds / 3600,
				(absSeconds % 3600) / 60,
				absSeconds % 60);
		return seconds < 0 ? "-" + positive : positive;
	}
	private static int getSecondOfDay(String timeStamp) {
		SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
		Date date = null, startOfDay = null;
		try {
			date = df.parse(timeStamp);
			startOfDay = df.parse(timeStamp.substring(0, 10) + " 00:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (int) (date.getTime() - startOfDay.getTime()) / 1000;
	}
}
