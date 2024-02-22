package se.oru.coordination.coordination_oru.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vividsolutions.jts.geom.Coordinate;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.utility.logging.MetaCSPLogging;
import se.oru.coordination.coordination_oru.Mission;
import se.oru.coordination.coordination_oru.TrajectoryEnvelopeCoordinator;
import se.oru.coordination.coordination_oru.motionplanning.AbstractMotionPlanner;
import se.oru.coordination.coordination_oru.simulation2D.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.LookAheadVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class collects utility methods for storing {@link Mission}s, regulating their dispatch, maintaining locations
 * and paths (i.e., a roadmap), finding the shortest paths through the roadmap, and extracting information from YAML files.
 * 
 * @author fpa
 *
 */
public class Missions {

	protected static HashMap<String,Pose> locations = new HashMap<String, Pose>();
	protected static HashMap<String,PoseSteering[]> paths = new HashMap<String, PoseSteering[]>();
	private static final Logger metaCSPLogger = MetaCSPLogging.getLogger(Missions.class);
	protected static HashMap<Integer,ArrayList<Mission>> missions = new HashMap<Integer, ArrayList<Mission>>();
	protected static HashMap<Integer,MissionDispatchingCallback> mdcs = new HashMap<Integer, MissionDispatchingCallback>();
	protected static HashMap<Mission,ArrayList<Mission>> concatenatedMissions = new HashMap<Mission, ArrayList<Mission>>();
	protected static SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	protected static String mapYAML = null;
	protected static String mapImageFilename = null;
	protected static BufferedImage map = null;
	protected static double mapResolution = -1;
	protected static Coordinate mapOrigin = null;

	protected static double minPathDistance = -1;
	
	protected static Thread missionDispatchThread = null;
	protected static HashSet<Integer> dispatchableRobots = new HashSet<Integer>();
	protected static HashMap<Integer,Boolean> loopMissions = new HashMap<Integer,Boolean>();

	/**
	 * Re-sample a given path so that the minimum distance between path poses is the value
	 */
	public static PoseSteering[] resamplePath(PoseSteering[] path) {
		if (minPathDistance < 0) return path;
		ArrayList<PoseSteering> ret = new ArrayList<PoseSteering>();
		PoseSteering lastAdded = path[0];
		ret.add(lastAdded);
		for (int i = 1; i < path.length; i++) {
			Coordinate p1 = lastAdded.getPose().getPosition();
			Coordinate p2 = path[i].getPose().getPosition();
			if (p2.distance(p1) > minPathDistance) {
				lastAdded = path[i];
				ret.add(path[i]);
			}
		}
		return ret.toArray(new PoseSteering[ret.size()]);
	}


	public static String[] getNearLocations(Pose p) {
        ArrayList<String> ret = new ArrayList<>(Missions.getLocationsAndPoses().keySet());
		
		ret.sort((arg0, arg1) -> {
            double dist0 = Missions.getLocationPose(arg0).distanceTo(p);
            double dist1 = Missions.getLocationPose(arg1).distanceTo(p);
            return Double.compare(dist0, dist1);
        });
		
		return ret.toArray(new String[0]);
	}
	
	/**
	 * Get the image of the current map, if set.
	 * @return The image of the current map, <code>null</code> if no map is known.
	 */
	public static BufferedImage getMap() {
		return Missions.map;
	}
	
	/**
	 * Get the resolution of the current map, if set.
	 * @return The resolution of the current map, <code>0</code> if no map is known.
	 */
	public static double getMapResolution() {
		return Missions.mapResolution;
	}

	/**
	 * Get the origin of the current map, if set.
	 * @return The origin of the current map, <code>null</code> if no map is known.
	 */
	public static Coordinate getMapOrigin() {
		return Missions.mapOrigin;
	}

	/**
	 * Save the current map (if known) as PNG with the corresponding YAML descriptor.
	 * @param fileName The name of the image/YAML files to save.
	 */
	public static void saveMap(String fileName) {
		if (Missions.map == null) throw new Error("Cannot save map as no map is known");
		File imageFilename = new File(fileName+".png");
		String yamlFilename = fileName + ".yaml";
		Missions.mapYAML = "\nimage: " + imageFilename;
		Missions.mapYAML += "\nresolution: " + Missions.mapResolution;
		Missions.mapYAML += "\norigin: [" + Missions.mapOrigin.x + ", " + Missions.mapOrigin.y + ", 0]";
		//TODO: make these static members of Missions and load them as well
		Missions.mapYAML +=  "\nnegate: 0";
		Missions.mapYAML += "\noccupied_thresh: 0.3";
		try { 
			ImageIO.write(Missions.map, "png", imageFilename);
			PrintWriter writer = new PrintWriter(yamlFilename);
			writer.println(Missions.mapYAML);
			writer.close();
		}
		catch (IOException e) { e.printStackTrace(); }
	}
	
	private static String extractZipFile(String fileName) {
		String json = "";
		try {
	        byte[] buffer = new byte[1024];
	        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileName));
	        ZipEntry zipEntry = zis.getNextEntry();
	        while(zipEntry != null){
	            String oneFileName = zipEntry.getName();
	            int len;
	            ArrayList<Byte> bytes = new ArrayList<Byte>();
	            while ((len = zis.read(buffer)) > 0) {
	                for (int i = 0; i < len; i++) bytes.add(buffer[i]);
	            }
            	byte[] dataBytes = new byte[bytes.size()];
            	for (int i = 0; i < bytes.size(); i++) dataBytes[i] = bytes.get(i);
	            if (oneFileName.endsWith(".json")) json = new String(dataBytes);
	            else {
	            	ByteArrayInputStream bais = new ByteArrayInputStream(dataBytes);
	            	Missions.map = ImageIO.read(bais);
	            	//Missions.mapImageFilename = oneFileName;
	            }
	            zipEntry = zis.getNextEntry();
	        }
	        zis.closeEntry();
	        zis.close();
		}
		catch (IOException e) { 
			e.printStackTrace(); 
			throw new Error("Unable to estract the ZIP file: " + e);
		}
        return json;
	}
	
	private static void makeZipFile(String ... fileNames) {
		try {
			File f = new File(fileNames[fileNames.length-1]);
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
			for (int i = 0; i < fileNames.length-1; i++) {
				ZipEntry e = new ZipEntry(fileNames[i]);
				out.putNextEntry(e);
				File entryFile = new File(fileNames[i]);
				byte[] data = Files.readAllBytes(entryFile.toPath());
				out.write(data, 0, data.length);
				out.closeEntry();
			}
			out.close();
		}
		catch (IOException e) { e.printStackTrace(); }
	}
	
	private static class ScenarioContainer {
		private final String locationsJSON;
		private final String pathsJSON;
		private final String missionsJSON;
		private final String mapYAMLJSON;
		private final String mapImageFilenameJSON;
		private final String mapResolutionJSON;
		private final String mapOriginJSON;
		private ScenarioContainer() {
			this.missionsJSON = Missions.getJSONString(Missions.missions);
            this.pathsJSON = Missions.getJSONString(Missions.paths);
            this.locationsJSON = Missions.getJSONString(Missions.locations);
            this.mapImageFilenameJSON = Missions.getJSONString(Missions.mapImageFilename);
            this.mapYAMLJSON = Missions.getJSONString(Missions.mapYAML);
            this.mapOriginJSON = Missions.getJSONString(Missions.mapOrigin);
            this.mapResolutionJSON = Missions.getJSONString(Missions.mapResolution);
		}
	}
	
	/**
	 * Save the current scenario. A scenario consists of all known locations and paths (a roadmap),
	 * all known missions, and a map of the environment. These are stored in a ZIP file with the
	 * given name, and can be loaded via a call to {@link Missions#loadScenario(String)}.
	 * @param scenarioName The name of the archive in which to save the current scenario.
	 */
	public static void saveScenario(String scenarioName) {
        try {
        	String zipFilename = scenarioName+".zip";
            metaCSPLogger.info("Saving scenario in ZIP file: " + zipFilename);
            String jsonFilename = scenarioName + ".json";
            PrintWriter writer = new PrintWriter(jsonFilename);
            ScenarioContainer sc = new ScenarioContainer();
            String scenarioJSON = Missions.getJSONString(sc);
            writer.println(scenarioJSON);
            writer.close();
            
            //Create map files if the map is only in memory for some reason...
            if (Missions.map != null && (Missions.mapImageFilename == null || Missions.mapYAML == null)) {
            	Missions.saveMap(scenarioName);
            	Missions.setMap(scenarioName+".yaml");
            }
            
            if (Missions.mapImageFilename != null) {
            	makeZipFile(Missions.mapImageFilename,jsonFilename,zipFilename);
            }
            else {
            	metaCSPLogger.info("Saving scenario without map because no map is known - use setMap() method to set one if you want to save the map along with the scenario.");
            	makeZipFile(jsonFilename,zipFilename);
            }
        }
        catch (IOException e) { 
        	e.printStackTrace(); 
        	throw new Error("Unable to save the scenario: " + e);
        }		
	}
	
	/**
	 * Load a scenario. A scenario contains one or more of the following:
	 * locations, paths, missions, and a map of the environment.
	 * Scenarios are stored in ZIP files, which can be saved via a call
	 * to {@link Missions#saveScenario(String)}.
	 * @param scenarioName The name of the scenario to load.
	 */
	@SuppressWarnings("unchecked")
	public static void loadScenario(String scenarioName) {
		String zipFilename = scenarioName+".zip";
		System.out.println("Loading scenario from ZIP file: " + zipFilename);
		String json = Missions.extractZipFile(zipFilename);
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
		ScenarioContainer sc = gson.fromJson(json, ScenarioContainer.class);
		Type collectionType = new TypeToken<HashMap<Integer,ArrayList<Mission>>>(){}.getType();
		Missions.missions = (HashMap<Integer, ArrayList<Mission>>) parseJSONString(collectionType, sc.missionsJSON);
		collectionType = new TypeToken<HashMap<String,Pose>>(){}.getType();
		Missions.locations = (HashMap<String,Pose>) parseJSONString(collectionType, sc.locationsJSON);
		collectionType = new TypeToken<HashMap<String,PoseSteering[]>>(){}.getType();
		Missions.paths = (HashMap<String,PoseSteering[]>) parseJSONString(collectionType, sc.pathsJSON);
		Missions.mapImageFilename = (String)parseJSONString(String.class, sc.mapImageFilenameJSON);
		//Missions.map = ImageIO.read(new File(Missions.mapImageFilename));
		Missions.mapYAML = (String)parseJSONString(String.class, sc.mapYAMLJSON);
		Missions.mapResolution = (Double)parseJSONString(Double.TYPE, sc.mapResolutionJSON);
		Missions.mapOrigin = (Coordinate)parseJSONString(Coordinate.class, sc.mapOriginJSON);
		Missions.buildGraph();		
	}
	
	private static String getJSONString(Object o) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
		String json = gson.toJson(o);
		return json;
	}
	
	private static Object parseJSONString(Type t, String json) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
		return gson.fromJson(json, t);
	}
	
	/**
	 * Set the current map.
	 * @param mapYAMLFile A file containing the description of the current map and a pointer to its image.
	 */
	public static void setMap(String mapYAMLFile) {
		try {
			File file = new File(mapYAMLFile);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st;
			Missions.mapYAML = "";
			while((st=br.readLine()) != null){
				if (!st.trim().startsWith("#") && !st.trim().isEmpty()) {
					Missions.mapYAML += st+"\n";
					String key = st.substring(0, st.indexOf(":")).trim();
					String value = st.substring(st.indexOf(":")+1).trim();
					if (key.equals("image")) Missions.mapImageFilename = file.getParentFile()+File.separator+value;
					else if (key.equals("resolution")) Missions.mapResolution = Double.parseDouble(value);
					else if (key.equals("origin")) {
						String x = value.substring(1, value.indexOf(",")).trim();
						String y = value.substring(value.indexOf(",")+1, value.indexOf(",", value.indexOf(",")+1)).trim();
						Missions.mapOrigin = new Coordinate(Double.parseDouble(x),Double.parseDouble(y));
					}
				}
			}
			br.close();
			Missions.map = ImageIO.read(new File(Missions.mapImageFilename));
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	/**
	 * Get the pose of the initial location of each robot's first {@link Mission}.
	 * @return The pose of the initial location of each robot's first {@link Mission}.
	 */
	public static HashMap<Integer,Pose> getInitialPoses() {
		HashMap<Integer,Pose> ret = new HashMap<Integer, Pose>();
		for (Integer robotID : missions.keySet()) {
			Mission m = Missions.peekMission(robotID);
			if (m != null) ret.put(robotID, m.getFromPose());
		}
		return ret;
	}
	
	/**
	 * Get the IDs of robots involved in at least one {@link Mission}.
	 * @return The IDs of robots involved in at least one {@link Mission}.
	 */
	public static int[] getIDsOfRobotsWithMissions() {
		int[] ret = new int[missions.keySet().size()];
		int index = 0;
		for (int robotID : missions.keySet()) {
			ret[index++] = robotID;
		}
		return ret;
	}
	
	private static void buildGraph() {
		
		graph = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		metaCSPLogger.info("Updating the roadmap...");
		
		for (String oneLoc : locations.keySet()) {
			graph.addVertex(oneLoc);
			metaCSPLogger.info("Added vertex " + oneLoc);
		}
		
		for (String from : locations.keySet()) {
			for (String to : locations.keySet()) {
				if (!from.equals(to)) {
					if (isKnownPath(from, to)) {
						DefaultWeightedEdge e = graph.addEdge(from, to);
						//PoseSteering[] path = loadKnownPath(from, to);
						PoseSteering[] path = paths.get(from+"->"+to);
						graph.setEdgeWeight(e, path.length);
						metaCSPLogger.info("Added edge " + e);
					}
				}
			}	
		}
	}

	/**
	 * Get the shortest path connecting given locations (two or more). The path between successive pairs of locations
	 * is computed with Dijkstra's algorithm, where edge weights are path lengths.
	 * @param locations At least two location names.
	 * @return The shortest path connecting given locations.
	 */
	public static PoseSteering[] getShortestPath(String ... locations) {
		if (locations.length < 2) throw new Error("Please provide at least two locations for path extraction!");
        var dijkstraShortestPath = new DijkstraShortestPath<>(graph);
        var overallShortestPath = new ArrayList<PoseSteering>();
		for (int k = 0; k < locations.length-1; k++) {
            var gp = dijkstraShortestPath.getPath(locations[k], locations[k+1]);
		    if (gp == null) return null;
            var oneShortestPath = gp.getVertexList();
            var allPoses = new ArrayList<PoseSteering>();
		    for (int i = 0; i < oneShortestPath.size()-1; i++) {
		    	//PoseSteering[] onePath = loadKnownPath(oneShortestPath.get(i),oneShortestPath.get(i+1));
                var onePath = paths.get(oneShortestPath.get(i)+"->"+oneShortestPath.get(i+1));
		    	if (i == 0) allPoses.add(onePath[0]);
                allPoses.addAll(Arrays.asList(onePath).subList(1, onePath.length - 1));
		    	if (i == oneShortestPath.size()-2) allPoses.add(onePath[onePath.length-1]);
		    }
		    if (k == 0) overallShortestPath.add(allPoses.get(0));
		    for (int i = 1; i < allPoses.size(); i++) {
		    	overallShortestPath.add(allPoses.get(i));
		    }
		}
		return overallShortestPath.toArray(new PoseSteering[overallShortestPath.size()]);
	}
	
	/**
	 * Get the path between two locations if such a path exists in the roadmap.
	 * @param from The name of the source location.
	 * @param to The name of the target location.
	 * @return The path between the given locations, <code>null</code> if the roadmap does not contain this edge.
	 */
	public static PoseSteering[] getPath(String from, String to) {
		return paths.get(from+"->"+to);
	}
	
	/**
	 * Get all the {@link Mission}s currently known for one robot
	 * @param robotID A robot identifier
	 * @return All the {@link Mission}s currently known for one robot
	 */
	public static ArrayList<Mission> getMissions(int robotID) {
		return missions.get(robotID);
	}
	
	/**
	 * Ascertain whether there is at least one {@link Mission} for a given robot. 
	 * @param robotID The ID of a robot.
	 * @return <code>true</code> iff the robot with the given ID has a mission.
	 */
	public static boolean hasMissions(int robotID) {
		return missions.containsKey(robotID) && !missions.get(robotID).isEmpty();
	}
	
	/**
	 * Remove one or more {@link Mission}s.
	 * @param m The mission(s) to remove.
	 */
	public static void removeMissions(Mission ... m) {
		for (Mission mis : m) missions.get(mis.getRobotID()).remove(mis);
	}
	
	/**
	 * Set the pose of a location.
	 * @param locationName The name of the location.
	 * @param p The pose of the location.
	 */
	@Deprecated
	public static void setLocation(String locationName, Pose p) {
		locations.put(locationName, p);
	}
	
	/**
	 * Get all the {@link Mission}s currently known
	 * @return All the {@link Mission}s currently known
	 */
	public static HashMap<Integer,ArrayList<Mission>> getMissions() {
		return missions;
	}

	/**
	 * Enqueue a new {@link Mission} for a robot.
	 * @param m The mission to enqueue.
	 */
	public static void enqueueMission(Mission m) {
		if (!missions.containsKey(m.getRobotID())) missions.put(m.getRobotID(), new ArrayList<Mission>());
		missions.get(m.getRobotID()).add(m);
	}

	/**
	 * Get the i-th mission for a given robot.	
	 * @param robotID A robot identifier.
	 * @param missionNumber The mission to get.
	 * @return The i-th mission for a given robot.
	 */
	public static Mission getMission(int robotID, int missionNumber) {
		return missions.get(robotID).get(missionNumber);
	}
	
	/**
	 * Dequeue the first mission from the queue of a given robot. 
	 * @param robotID The ID of the robot.
	 * @return The first mission from the queue of a given robot.
	 */
	public static Mission dequeueMission(int robotID) {
		if (hasMissions(robotID) && !missions.get(robotID).isEmpty()) {
			Mission m = missions.get(robotID).get(0);
			missions.get(robotID).remove(0);
			return m;
		}
		return null;
	}

	/**
	 * Pop the first mission from the queue of a given robot, but do NOT remove the mission from the queue. 
	 * @param robotID The ID of the robot.
	 * @return The first mission from the queue of a given robot.
	 */
	public static Mission peekMission(int robotID) {
		if (hasMissions(robotID) && !missions.get(robotID).isEmpty()) {
            return missions.get(robotID).get(0);
		}
		return null;
	}

	/**
	 * Normalize an angle to be within (-PI,PI).
	 * @param th The angle to normalize
	 * @return A value within (-PI,PI)
	 */
	public static double wrapAngle180b(double th) {
		double ret = Math.atan2(Math.sin(th), Math.cos(th));
		if (Math.abs(ret-Math.PI) < 0.00001) return Math.PI-0.01;
		return ret;
	}

	/**
	 * Normalize an angle to be within [-PI,PI).
	 * @param th The angle to normalize
	 * @return A value within [-PI,PI)
	 */
	public static double wrapAngle180(double th) {
		double ret = Math.atan2(Math.sin(th), Math.cos(th));
		if (Math.abs(ret-Math.PI) < 0.00001) return -Math.PI;
		return ret;
	}

	/**
	 * Get the known locations and their poses.
	 * @return All known locations and their poses.
	 */
	@Deprecated
	public static HashMap<String,Pose> getLocations() {
		return locations;
	}

	/**
	 * Get the known locations and their poses.
	 * @return All known locations and their poses.
	 */
	public static HashMap<String,Pose> getLocationsAndPoses() {
		return locations;
	}
	
	/**
	 * Load location and path data from a file.
	 * @param fileName The file to load the data from.
	 */
	@Deprecated
	public static void loadLocationAndPathData(String fileName) {
		Missions.loadRoadMap(fileName);
	}
	
	/**
	 * Load a roadmap stored in a give directory or file.
	 * @param path The directory or file to load the roadmap from.
	 * If a directory is given, the filename is assumed to he "roadmap.txt"
	 * (the same convention used in saving, see {@link #saveRoadMap(String)}).
	 */
	public static void loadRoadMap(String path) {
		try {
			String fileOnly;
			String pathOnly;
			File f = new File(path);
			if (f.isDirectory()) {
				pathOnly = path;
				if (!pathOnly.endsWith(File.separator)) pathOnly += File.separator;
				fileOnly = "roadmap.txt";
			}
			else {
				pathOnly = f.getAbsolutePath().substring(0,f.getAbsolutePath().lastIndexOf(File.separator))+File.separator;
				fileOnly = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(File.separator)+1);
			}
			Scanner in = new Scanner(new FileReader(pathOnly+fileOnly));
			
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				if (line.length() != 0 && !line.startsWith("#")) {
					String[] oneline = line.split(" |\t");
					Pose ps = null;
					if (line.contains("->")) {
						PoseSteering[] knownPath = loadPathFromFile(pathOnly+oneline[3]);
						paths.put(oneline[0]+oneline[1]+oneline[2], knownPath);
						//paths.put(oneline[0]+oneline[1]+oneline[2], oneline[3]);
						//metaCSPLogger.info("Loaded path: " + oneline[0]+oneline[1]+oneline[2] + " --> " + oneline[3]);
					}
					else {
						ArrayList<String> newLineContents = new ArrayList<String>();
						for (int i = 0; i < oneline.length; i++) {
							if (!oneline[i].trim().equals("")) newLineContents.add(oneline[i].trim()); 
						}
						String locationName = newLineContents.get(0);
						ps = new Pose(
								Double.parseDouble(newLineContents.get(1)),
								Double.parseDouble(newLineContents.get(2)),
								Double.parseDouble(newLineContents.get(3)));
						locations.put(locationName, ps);
					}
				}
			}
			in.close();
		}
		catch (FileNotFoundException e) { 
			e.printStackTrace(); 
			throw new Error("Unable to load the required scenario: " + e);
		}
		Missions.buildGraph();
	}
	
	/**
	 * Save all locations and paths into a given directory.
	 * @param pathname The name of a directory in which to store the data.
	 */
	@Deprecated
	public static void saveLocationAndPathData(String pathname) {
		Missions.saveRoadMap(pathname);
	}
	
	/**
	 * Save the current roadmap into a given directory. All known paths referred to
	 * in the roadmap are saved in the given directory. The roadmap itself is saved
	 * in a file named "roadmap.txt" within the given directory.
	 * @param pathname The name of a directory in which to store the roadmap data.
	 */
	public static void saveRoadMap(String pathname) {
		if (new File(pathname).exists()) throw new Error("Directory \"" + pathname + "\" exists, will abort saving");
		if (!new File(pathname).mkdir()) throw new Error("Could not make path \"" + pathname + "\"");
		if (!pathname.endsWith(File.separator)) pathname += File.separator;
		String st = "#Locations\n";
		for (Entry<String,Pose> entry : locations.entrySet()) {
			st += entry.getKey() + "\t" + entry.getValue().getX() + "\t" + entry.getValue().getY() + "\t" + entry.getValue().getTheta() + "\n";
		}
		st+="\n#Paths\n";
		for (Entry<String,PoseSteering[]> entry : paths.entrySet()) {
			String pathFilename = entry.getKey().replaceAll("->", "-")+".path";
			st += entry.getKey().replaceAll("->", " -> ") + "\t" + pathFilename + "\n";
			writePath(pathname+pathFilename, entry.getValue());
		}
        try {
        	String newFilename = pathname+"roadmap.txt";
            File file = new File(newFilename);
            PrintWriter writer = new PrintWriter(file);
            writer.write(st);
            writer.close();
            System.out.println("Saved roadmap " + newFilename);
        }
        catch (Exception ex) { ex.printStackTrace(); }
	}

	/**
	 * Add a path to the set of known paths.
	 * @param start The starting location of the path.
	 * @param goal The goal location of the path.
	 * @param path The path to add.
	 */
	@Deprecated
	public static void addKnownPath(String start, String goal, PoseSteering[] path) {
		Missions.addPathToRoadMap(start, goal, path);
	}
		
	/**
	 * Add a path to the current roadmap.
	 * @param start The starting location of the path.
	 * @param goal The goal location of the path.
	 * @param path The path to add.
	 */
	public static void addPathToRoadMap(String start, String goal, PoseSteering[] path) {
		if (!locations.containsKey(start) || !locations.containsKey(goal) || path == null || path.length == 0) throw new Error("Locations unknown or path is invalid (" + (start+"->"+goal) + ")!");
		paths.put(start+"->"+goal, path);
		Missions.buildGraph();
	}

	/**
	 * Remove a named location.
	 * @param locationName The name of the location to remove.
	 */
	@Deprecated
	public static void removeLocation(String locationName) {
		Missions.removeLocationFromRoadMap(locationName);
	}
	
	/**
	 * Remove a named location from the roadmap.
	 * @param locationName The name of the location to remove.
	 */
	public static void removeLocationFromRoadMap(String locationName) {
		locations.remove(locationName);
		ArrayList<String> toRemove = new ArrayList<String>();
		for (String key : paths.keySet()) {
			if (key.substring(0,key.indexOf("->")).equals(locationName)) toRemove.add(key);
			else if (key.substring(key.indexOf("->")).equals(locationName)) toRemove.add(key);
		}
		for (String key : toRemove) paths.remove(key);
		if (graph != null) graph.removeVertex(locationName);
	}
	
	/**
	 * Add a location to the roadmap.
	 * @param locationName The name of the location.
	 * @param pose The pose of the location.
	 */
	public static void addLocationToRoadMap(String locationName, Pose pose) {
		locations.put(locationName, pose);
	}
	
	/**
	 * Get locations from the data loaded by method loadLocationAndPathData()
	 * @param name The name of the location to get
	 * @return The {@link Pose} of the location
	 */
	@Deprecated
	public static Pose getLocation(String name) {
		Pose ret = locations.get(name);
		if (ret == null) throw new Error("Unknown location " + name);
		return ret;
	}
	
	/**
	 * Get the pose of a given named location in the roadmap.
	 * @param name The name of the location to get the pose of.
	 * @return The {@link Pose} of the location
	 */
	public static Pose getLocationPose(String name) {
		Pose ret = locations.get(name);
		if (ret == null) throw new Error("Unknown location " + name);
		return ret;
	}


	/**
	 * Get the mission following a given mission.
	 * @param m A mission.
	 * @return The mission following the given mission.
	 */
	public static Mission getNextMission(Mission m) {
		for (int i = 0; i < missions.get(m.getRobotID()).size(); i++) {
			if (missions.get(m.getRobotID()).get(i).equals(m)) return missions.get(m.getRobotID()).get((i+1)%missions.get(m.getRobotID()).size());
		}
		return null;
	}

	/**
	 * Get the mission preceding a given mission.
	 * @param m A mission.
	 * @return The mission preceding the given mission.
	 */
	public static Mission getPreviousMission(Mission m) {
		for (int i = 0; i < missions.get(m.getRobotID()).size(); i++) {
			if (missions.get(m.getRobotID()).get(i).equals(m)) {
				if (i == 0) return missions.get(m.getRobotID()).get(missions.get(m.getRobotID()).size()-1);
				return missions.get(m.getRobotID()).get((i-1));
			}
		}
		return null;
	}

	/**
	 * Get path files from the data loaded by method {@link Missions#loadLocationAndPathData(String)}.
	 * @param fromLocation The name of the source location.
	 * @param toLocation The name of the destination location.
	 * @return The name of the file where the path is stored.
	 */
	@Deprecated
	public static String getPathFile(String fromLocation, String toLocation) {
		//String ret = paths.get(fromLocation+"->"+toLocation);
		String ret = fromLocation+"->"+toLocation;
		if (!locations.containsKey(fromLocation)) throw new Error("Unknown location " + fromLocation);
		if (!locations.containsKey(toLocation)) throw new Error("Unknown location " + toLocation);
		File f = new File(ret);
		if(!f.exists() || f.isDirectory()) throw new Error("No path between " + fromLocation + " and " + toLocation);
		//if (ret == null) throw new Error("No path between " + fromLocation + " and " + toLocation);
		return ret;
	}

	/**
	 * Queries whether a path between two named locations is known.
	 * @param fromLocation The source location.
	 * @param toLocation The goal location.
	 * @return <code>true</code> iff a path between two locations is known.
	 */
	public static boolean isKnownPath(String fromLocation, String toLocation) {
		return paths.containsKey(fromLocation+"->"+toLocation);
	}
	
	/**
	 * Return a path between locations if available (throws error if locations and/or path are now known)
	 * @param fromLocation The source location
	 * @param toLocation The goal location
	 * @return The path between the two (known) locations
	 */
//	public static PoseSteering[] loadKnownPath(String fromLocation, String toLocation) {
//		if (!isKnownPath(fromLocation, toLocation)) throw new Error("No path between " + fromLocation + " and " + toLocation);
//		return loadPathFromFile(pathPrefix+getPathFile(fromLocation, toLocation));
//	}

	/**
	 * Get a property from a YAML file
	 * @param property The name of the property
	 * @param yamlFile The YAML file from which to get the property
	 * @return The value of the property in the YAML file
	 */
	public static String getProperty(String property, String yamlFile) {
		String ret = null;
		try {
			File file = new File(yamlFile);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String st;
			while((st=br.readLine()) != null){
				String key = st.substring(0, st.indexOf(":")).trim();
				String value = st.substring(st.indexOf(":")+1).trim();
				if (key.equals(property)) {
					ret = value;
					break;
				}
			}
			br.close();
		}
		catch (IOException e) { e.printStackTrace(); }
		return ret;
	}

	/**
	 * Exclude the given robots from the dispatching thread.
	 * @param robotIDs The IDs of the robots to be excluded.
	 */
	public static void stopMissionDispatchers(int ... robotIDs) {
		for (int robotID : robotIDs) {
			dispatchableRobots.remove(robotID);
			mdcs.remove(robotID);
		}
	}

	/**
	 * Save a path to a file.
	 * @param fileName The name of the file.
	 * @param path The path to save.
	 */
	public static void writePath(String fileName, ArrayList<PoseSteering> path) {
        try {
            File file = new File(fileName);
            System.out.println("Saved path file: " + file.getAbsolutePath());
            PrintWriter writer = new PrintWriter(file);
            for (PoseSteering ps : path) {
            	writer.println(ps.getPose().getX() + "\t" + ps.getPose().getY() + "\t" + ps.getPose().getTheta() + "\t" + ps.getSteering());
            }
            writer.close();
        }
        catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * Save a path to a file.
	 * @param fileName The name of the file.
	 * @param path The path to save.
	 */
	public static void writePath(String fileName, PoseSteering[] path) {
        try {
            File file = new File(fileName);
            System.out.println("Saved path file " + file.getAbsolutePath());
            PrintWriter writer = new PrintWriter(file);
            for (PoseSteering ps : path) {
            	writer.println(ps.getPose().getX() + "\t" + ps.getPose().getY() + "\t" + ps.getPose().getTheta() + "\t" + ps.getSteering());
            }
            writer.close();
        }
        catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * Tag a set of {@link Mission}s as concatenated, meaning that they should be executed in sequence.
	 * This marking will be accounted for by the dispatchers started via methods {@link Missions#startMissionDispatchers(TrajectoryEnvelopeCoordinator, int...)}
	 * and {@link Missions#startMissionDispatchers(TrajectoryEnvelopeCoordinator, boolean, int...)}.
	 * @param m The {@link Mission}s that should be considered concatenated.
	 */
	public static void concatenateMissions(Mission ... m) {
		ArrayList<Mission> toAdd = new ArrayList<Mission>();
        Collections.addAll(toAdd, m);
		concatenatedMissions.put(m[0], toAdd);
	}

	/**
	 * Add a {@link MissionDispatchingCallback} which defines methods to be called before and after mission dispatching.
	 * @param robotID The callback functions will be invoked whenever a mission for this robot is dispatched.
	 * @param cb The {@link MissionDispatchingCallback} to attach.
	 */
	public static void addMissionDispatchingCallback(int robotID, MissionDispatchingCallback cb) {
		mdcs.put(robotID, cb);
	}

	/**
	 * Include the given robots in the periodic mission dispatching thread (and start the thread if it is not started).
	 * The thread cycles through the known missions for each robot and dispatches as soon as the robot is free.
	 * This method will loop through all mission forever.
	 * @param tec The {@link TrajectoryEnvelopeCoordinator} that coordinates the missions.
	 * @param robotIDs The robot IDs which should be considered dispatchable.
	 */
	public static void startMissionDispatchers(final TrajectoryEnvelopeCoordinator tec, int ... robotIDs) {
		startMissionDispatchers(tec, true, robotIDs);
	}
	
	/**
	 * Include the given robots in the periodic mission dispatching thread (and start the thread if it is not started).
	 * The thread cycles through the known missions for each robot and dispatches as soon as the robot is free.
	 * @param tec The {@link TrajectoryEnvelopeCoordinator} that coordinates the missions.
	 * @param loop Set to <code>false</code> if missions should be de-queued once dispatched. 
	 * @param robotIDs The robot IDs which should be considered dispatchable.
	 */
	public static void startMissionDispatchers(final TrajectoryEnvelopeCoordinator tec, final boolean loop, int ... robotIDs) {
		
		for (int robotID : robotIDs) {
			dispatchableRobots.add(robotID);
			loopMissions.put(robotID, loop);
		}
		
		if (missionDispatchThread == null) {
			missionDispatchThread = new Thread() {
				@Override
				public void run() {
					while (true) {
						for (int robotID : dispatchableRobots) {
							if (Missions.hasMissions(robotID)) {
								Mission m = Missions.peekMission(robotID);
								if (m != null) {								
									synchronized(tec) {
										if (tec.isFree(m.getRobotID())) {
											//cat with future missions if necessary
											if (concatenatedMissions.containsKey(m)) {
												ArrayList<Mission> catMissions = concatenatedMissions.get(m);
												m = new Mission(m.getRobotID(), m.getFromLocation(), catMissions.get(catMissions.size()-1).getToLocation(), m.getFromPose(), catMissions.get(catMissions.size()-1).getToPose());
												ArrayList<PoseSteering> path = new ArrayList<PoseSteering>();
												for (int i = 0; i < catMissions.size(); i++) {
													Mission oneMission = catMissions.get(i);
													if (mdcs.containsKey(robotID)) mdcs.get(robotID).beforeMissionDispatch(oneMission);
													if (i == 0) path.add(oneMission.getPath()[0]);
													for (int j = 1; j < oneMission.getPath().length-1; j++) {
														path.add(oneMission.getPath()[j]);
													}
													if (i == catMissions.size()-1) path.add(oneMission.getPath()[oneMission.getPath().length-1]);
												}
												m.setPath(path.toArray(new PoseSteering[path.size()]));
											}
											else if (mdcs.containsKey(robotID)) mdcs.get(robotID).beforeMissionDispatch(m);							
										}
			
										//addMission returns true iff the robot was free to accept a new mission
										if (tec.addMissions(m)) {
											//tec.computeCriticalSectionsAndStartTrackingAddedMission();
											if (mdcs.containsKey(robotID)) mdcs.get(robotID).afterMissionDispatch(m);
											if (!loopMissions.get(robotID)) {
												Missions.removeMissions(m);
												System.out.println("Removed mission " + m);
												if (concatenatedMissions.get(m) != null) {
													for (Mission cm : concatenatedMissions.get(m)) {
														Missions.removeMissions(cm);
													}
												}
											}
											else {
												Missions.dequeueMission(m.getRobotID());
												Missions.enqueueMission(m);
											}
										}
									}
								}
							}
						}
						//Sleep for a little (sec 2)
						try { Thread.sleep(2000); }
						catch (InterruptedException e) { e.printStackTrace(); }
					}
				}
			};
			missionDispatchThread.start();
		}
	}

	/**
	 * Starts the {@link TrajectoryEnvelopeCoordinator} mission dispatchers with additional parameters for report writing.
	 * Writes robot reports to a specified directory if required.
	 * If {@code lookAheadDistance} is -1, it updates to the full distance of {@link LookAheadVehicle}.
	 *
	 * <p>This method may also write robot reports, if requested. The reports are written to
	 * {@code resultDirectory}, at an interval defined by {@code intervalInSeconds} and for a
	 * duration defined by {@code terminationInMinutes}. The name of the heuristic used in the
	 * current simulation run, defined by {@code heuristicName}, is also recorded.
	 *
	 * @param tec The {@link TrajectoryEnvelopeCoordinator} that coordinates the missions.
	 * @param writeReports {@code true} to write reports; {@code false} otherwise.
	 * @param intervalInSeconds The interval in seconds between consecutive reports.
	 * @param terminationInMinutes The termination time in minutes for writing reports.
	 * @param heuristicName The name of the heuristic used in the current simulation run.
	 * @param resultDirectory The directory where to write the robot reports.
	 */
	public static void startMissionDispatchers(TrajectoryEnvelopeCoordinator tec, boolean writeReports, double intervalInSeconds,
											   int terminationInMinutes, String heuristicName,
											   int inferenceCycleTime, String resultDirectory, double scaleAdjustment) {

		writeReports(tec, writeReports, intervalInSeconds,
				terminationInMinutes, heuristicName, inferenceCycleTime, resultDirectory, scaleAdjustment);

		addRobotsForLooping(tec);

		if (missionDispatchThread == null) {
			missionDispatchThread = new Thread("missionDispatchThread") {
				@Override
				public void run() {
					while (true) {
						for (int robotID : dispatchableRobots) {
							if (Missions.hasMissions(robotID)) {
								Mission m = Missions.peekMission(robotID);
								if (m != null) {
									synchronized(tec) {
										if (tec.isFree(m.getRobotID())) {
											//cat with future missions if necessary
											if (concatenatedMissions.containsKey(m)) {
												ArrayList<Mission> catMissions = concatenatedMissions.get(m);
												m = new Mission(m.getRobotID(), m.getFromLocation(), catMissions.get(catMissions.size()-1).getToLocation(), m.getFromPose(), catMissions.get(catMissions.size()-1).getToPose());
												ArrayList<PoseSteering> path = new ArrayList<PoseSteering>();
												for (int i = 0; i < catMissions.size(); i++) {
													Mission oneMission = catMissions.get(i);
													if (mdcs.containsKey(robotID)) mdcs.get(robotID).beforeMissionDispatch(oneMission);
													if (i == 0) path.add(oneMission.getPath()[0]);
													for (int j = 1; j < oneMission.getPath().length-1; j++) {
														path.add(oneMission.getPath()[j]);
													}
													if (i == catMissions.size()-1) path.add(oneMission.getPath()[oneMission.getPath().length-1]);
												}
												m.setPath(path.toArray(new PoseSteering[path.size()]));
											}
											else if (mdcs.containsKey(robotID)) mdcs.get(robotID).beforeMissionDispatch(m);
										}

										//addMission returns true iff the robot was free to accept a new mission
										if (tec.addMissions(m)) {
											//tec.computeCriticalSectionsAndStartTrackingAddedMission();
											if (mdcs.containsKey(robotID)) mdcs.get(robotID).afterMissionDispatch(m);
											if (!loopMissions.get(robotID)) {
												Missions.removeMissions(m);
												System.out.println("Removed mission " + m);
												if (concatenatedMissions.get(m) != null) {
													for (Mission cm : concatenatedMissions.get(m)) {
														Missions.removeMissions(cm);
													}
												}
											}
											else {
												Missions.dequeueMission(m.getRobotID());
												Missions.enqueueMission(m);
											}
										}
									}
								}
							}
						}
						//Sleep for a little (sec 2)
						try { Thread.sleep(2000); }
						catch (InterruptedException e) { e.printStackTrace(); }
					}
				}
			};
			missionDispatchThread.start();
		}
	}

	/**
	 * Include the given robots in the periodic mission dispatching thread (and start the thread if it is not started).
	 * The thread cycles through the known missions for each robot and dispatches as soon as the robot is free.
	 * This method will loop through all missions forever.
	 * @param tec The {@link TrajectoryEnvelopeCoordinator} that coordinates the missions.
	 */
	public synchronized static void startMissionDispatchers(final TrajectoryEnvelopeCoordinator tec) {
		startMissionDispatchers(tec, true, convertSetToIntArray(tec.getAllRobotIDs()));
	}

	public static void runMissionsIndefinitely(TrajectoryEnvelopeCoordinatorSimulation tec) {
		// Initialize a common stoppage time map for all robots with Double values
		Map<Integer, Double> commonStoppageTimes = new HashMap<>();
		commonStoppageTimes.put(0, 0.25); // 0.25 minutes (15 seconds) for mission number 0
//		commonStoppageTimes.put(1, 0.5);  // 0.5 minutes (30 seconds) for mission number 1

        var executorService = Executors.newScheduledThreadPool(tec.getAllRobotIDs().size());
		for (int robotID : convertSetToIntArray(tec.getAllRobotIDs())) {
			executorService.execute(() -> {
                var missionNumber = 0;
				var missions = Missions.getMissions(robotID);
                while (true) {
					if (missionNumber >= missions.size()) missionNumber = 0;
					missionNumber = handleMission(tec, missionNumber, missions);
					delayBetweenMission(missionNumber, commonStoppageTimes);
					threadSleep();
				}
			});
		}
	}

	private static int handleMission(TrajectoryEnvelopeCoordinatorSimulation tec, int missionNumber, List<Mission> missions) {
		var nextMission = missions.get(missionNumber);
		synchronized (tec) {
			if (tec.addMissions(nextMission)) {
				missionNumber++;
			}
		}
		return missionNumber;
	}

	private static void delayBetweenMission(int missionNumber, Map<Integer, Double> commonStoppageTimes) {
		if (missionNumber > 0) { // Apply stoppage time after the first mission
			double stoppageTimeInMinutes = commonStoppageTimes.getOrDefault(missionNumber - 1, 0.0);
			long stoppageTimeInMillis = (long) (stoppageTimeInMinutes * 60 * 1000);
			try {
				TimeUnit.MILLISECONDS.sleep(stoppageTimeInMillis);
			} catch (InterruptedException e) {
				e.getMessage();
			}
		}
	}

	private static void threadSleep() {
		try {
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			e.getMessage();
		}
	}

	public static void runMissionsOnce(TrajectoryEnvelopeCoordinatorSimulation tec) { //TODO: Assumption, This code assumes you have all the mission pre-planned and stored in the vehicles
		// Initialize a common stoppage time map for all robots with Double values
		Map<Integer, Double> commonStoppageTimes = new HashMap<>();
		commonStoppageTimes.put(0, 0.25); // 0.25 minutes (15 seconds) for mission number 0
//		commonStoppageTimes.put(1, 0.5);  // 0.5 minutes (30 seconds) for mission number 1

		var executorService = Executors.newScheduledThreadPool(tec.getAllRobotIDs().size());
		for (int robotID : convertSetToIntArray(tec.getAllRobotIDs())) {
			executorService.execute(() -> {
				var missionNumber = 0;
				var missions = Missions.getMissions(robotID);
                while (missionNumber < missions.size()) {
                    missionNumber = handleMission(tec, missionNumber, missions);
                    delayBetweenMission(missionNumber, commonStoppageTimes);
                    threadSleep(); // FIXME: Check if need it?
                }
			});
		}
		executorService.shutdown();
	}

	public static void runMissionsForDuration(TrajectoryEnvelopeCoordinatorSimulation tec, int durationInMinutes) {
		Map<Integer, Double> commonStoppageTimes = new HashMap<>();
		commonStoppageTimes.put(0, 0.25); // 15 seconds for mission number 0
		commonStoppageTimes.put(1, 0.5);  // 30 seconds for mission number 1

		var executorService = Executors.newScheduledThreadPool(tec.getAllRobotIDs().size());
		for (int robotID : convertSetToIntArray(tec.getAllRobotIDs())) {
			executorService.execute(() -> {
				var missionNumber = 0;
				var missions = Missions.getMissions(robotID);
				while (!Thread.currentThread().isInterrupted()) {
					if (missionNumber >= missions.size()) missionNumber = 0;
					missionNumber = handleMission(tec, missionNumber, missions);
					delayBetweenMission(missionNumber, commonStoppageTimes);
					threadSleep();
				}
			});
		}
		executorService.schedule(() -> {
			executorService.shutdownNow();
		}, durationInMinutes, TimeUnit.MINUTES);
	}

	/**
	 * Adds robots for mission looping, i.e., assigns the looping status of the missions for each robot.
	 * Robots of type {@code "AutonomousRobot"} have their missions set to loop.
	 *
	 * @param tec The {@link TrajectoryEnvelopeCoordinator} that coordinates the missions.
	 */
	private static void addRobotsForLooping(TrajectoryEnvelopeCoordinator tec) {
		for (int robotID : convertSetToIntArray(tec.getAllRobotIDs())) {
			dispatchableRobots.add(robotID);
			loopMissions.put(robotID, true);
//			if (Objects.equals(VehiclesHashMap.getVehicle(robotID).getType(), "AutonomousVehicle")) {
//				loopMissions.put(robotID, true);
//			}
//			else {
//				loopMissions.put(robotID, false);
//			}
		}
	}

	/**
	 * Converts a Set of Integers to an int array.
	 *
	 * @param set The Set of Integers to be converted.
	 * @return The resulting int array.
	 */
	public static int[] convertSetToIntArray(Set<Integer> set) {
		int[] array = new int[set.size()]; // Create a new int array with the same size as the Set
		int index = 0;
		for (int num : set) {
			array[index++] = num; // Store each element from the Set into the int array
		}
		return array; // Return the resulting int array
	}

	/**
	 * Writes robot reports to the specified directory based on the provided conditions.
	 * Robot reports are generated at an interval and for a duration defined by the user.
	 * The method also incorporates the heuristic information used for the simulation.
	 * The reported values, including the robot's pose and speed, can be scaled using the {@code scaleAdjustment} parameter.
	 *
	 * @param tec                  The {@link TrajectoryEnvelopeCoordinator} managing the missions.
	 * @param writeReports         If {@code true}, the method will write reports, otherwise, it won't.
	 * @param intervalInSeconds    The time interval (in seconds) between consecutive report generations.
	 * @param terminationInMinutes The maximum duration (in minutes) for which reports should be written.
	 * @param heuristicName        The name of the heuristic used during the simulation.
	 * @param inferenceCycleTime   The cycle time (in milliseconds) for updating the coordination thread.
	 * @param resultDirectory      The directory path where the robot reports will be saved.
	 * @param scaleAdjustment      A scaling factor to adjust the reported values of robot pose and speed.
	 */
	public static void writeReports(TrajectoryEnvelopeCoordinator tec,
									boolean writeReports, double intervalInSeconds, int terminationInMinutes,
									String heuristicName, int inferenceCycleTime, String resultDirectory,
									double scaleAdjustment) {
		double updatedLookAheadDistance = 0.0;
		double lookAheadDistance = 0.0;

		// For fully predictable path of the robot
		for (int robotID : convertSetToIntArray(tec.getAllRobotIDs())) {
			var robot = VehiclesHashMap.getVehicle(robotID);
			if (robot instanceof LookAheadVehicle) {
				var lookAheadRobot = (LookAheadVehicle) robot;
				lookAheadDistance = lookAheadRobot.getLookAheadDistance();
				if (lookAheadDistance < 0) {
					updatedLookAheadDistance = lookAheadRobot.getPlanLength();
				}
			}
		}

		// Write robot reports to ../results/... folder in .csv format
		if (writeReports) {
			System.out.println("Writing robot reports.");
			double distance = lookAheadDistance > 0 ? lookAheadDistance : updatedLookAheadDistance;

			String filePath = createFile(distance * scaleAdjustment, heuristicName, inferenceCycleTime, resultDirectory);
			var reportCollector = new RobotReportCollector();
			reportCollector.handleRobotReports(tec, filePath, (long) (1000 * intervalInSeconds),
					terminationInMinutes, scaleAdjustment);
		} else {
			System.out.println("Not writing robot reports.");
		}
	}

	/**
	 * Constructs a file path for saving robot reports. The method also creates the directory
	 * if it doesn't exist. The filename is derived from the count of autonomous and lookahead robots,
	 * the heuristic applied, and the lookahead distance.
	 *
	 * @param lookAheadDistance  The distance for which the robot plans its path ahead of its current position.
	 *                           Should be a positive number.
	 * @param heuristicName      The heuristic's name used in the simulation. Expected to be non-null.
	 * @param updateCycleTime    The cycle time for updating the coordination thread.
	 * @param resultDirectory    The directory path where the robot report files will be saved.
	 *                           If the path doesn't exist, the method attempts to create it.
	 * @return                   The full path, including directory and filename, where the report will be saved.
	 */

	public static String createFile(double lookAheadDistance, String heuristicName, int updateCycleTime,
									String resultDirectory) {

		Path directoryPath = Paths.get(resultDirectory);

		// Try creating the directory if it doesn't exist
		try {
			Files.createDirectories(directoryPath);
			System.out.println("Directory created successfully.");
		} catch (IOException e) {
			System.err.println("Error while creating directory: " + e.getMessage());
		}

		// Get the number of autonomous and lookahead robots
		int autonomousRobotCount = 0;
		int lookAheadRobotCount = 0;

		for (AbstractVehicle robot : VehiclesHashMap.getList().values()) {
			if (robot instanceof AutonomousVehicle) {
				autonomousRobotCount++;
			} else if (robot instanceof LookAheadVehicle) {
				lookAheadRobotCount++;
			}
		}

		// Generate the filename based on the number of autonomous and lookahead robots
		String fileName = "A" + autonomousRobotCount + "H" + lookAheadRobotCount +
				"_" + heuristicName.charAt(0) + "_" + (int) lookAheadDistance + "_" + updateCycleTime + "_";

		return resultDirectory + "/" + fileName;
	}

	/**
	 * Read a path from a file.
	 * @param fileName The name of the file containing the path
	 * @return The path read from the file
	 */
	public static PoseSteering[] loadPathFromFile(String fileName) {
		ArrayList<PoseSteering> ret = new ArrayList<PoseSteering>();
		try {
			Scanner in = new Scanner(new FileReader(fileName));
			String prevLine = "Gibberish";
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				if (!line.equals(prevLine)) {
					prevLine = line;
					if (line.length() != 0) {
						String[] oneline = line.split(" |\t");
						PoseSteering ps = null;
						if (oneline.length == 4) {
						ps = new PoseSteering(
								Double.parseDouble(oneline[0]),
								Double.parseDouble(oneline[1]),
								Double.parseDouble(oneline[2]),
								Double.parseDouble(oneline[3]));
						}
						else {
							ps = new PoseSteering(
									Double.parseDouble(oneline[0]),
									Double.parseDouble(oneline[1]),
									Double.parseDouble(oneline[2]),
									0.0);					
						}
						ret.add(ps);
					}
				}
			}
			in.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		PoseSteering[] retArray = ret.toArray(new PoseSteering[ret.size()]);
		return resamplePath(retArray);
	}

	/**
	 * Create a mission following a given mission. The mission will make the follower robot navigate from its
	 * current pose to the start pose of the leader's mission, after which the follower robot will follow the
	 * leader's mission.
	 * @param leaderMission The mission to follow.
	 * @param followerID The ID of the follower robot.
	 * @param followerStartingPose The current pose of the follower robot.
	 * @param mp The motion planner that should be used to compute the path from the follower robot's
	 * current pose to goal pose of the leader's mission, passing through the start pose of the leader's mission.
	 * @param computePathToLeaderGoal Set this to <code>true</code> iff the follower's path to the goal of the 
	 * leader's mission should be recomputed (otherwise the leader's path will be re-used).  
	 * @return
	 */
	public static Mission followMission(Mission leaderMission, int followerID, Pose followerStartingPose, AbstractMotionPlanner mp, boolean computePathToLeaderGoal) {
		mp.setStart(followerStartingPose);
		if (computePathToLeaderGoal) mp.setGoals(leaderMission.getFromPose(), leaderMission.getToPose());
		else mp.setGoals(leaderMission.getFromPose());
		if (!mp.plan()) return null;
		PoseSteering[] followerPath = mp.getPath();
		if (!computePathToLeaderGoal) {
			PoseSteering[] newPath = new PoseSteering[followerPath.length+leaderMission.getPath().length-1];
			int counter = 0;
			for (PoseSteering ps : followerPath) newPath[counter++] = ps;
			for (int i = 1; i < leaderMission.getPath().length; i++) newPath[counter++] = leaderMission.getPath()[i];
			followerPath = newPath;
		}
        return new Mission(followerID, followerPath);
	}
}
