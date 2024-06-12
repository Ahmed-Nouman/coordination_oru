package se.oru.coordination.coordination_oru.gui;

import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.vehicles.AbstractVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.util.ArrayList;
import java.util.List;

public class DataStatus {
    private boolean isPlansVerified = false;
    private String projectFile = "";
    private ProjectData projectData = new ProjectData();
    private ProjectData originalProjectData = new ProjectData();
    private MapData mapData = new MapData();
    private Boolean writeVehicleReports = false;
    private int simulationTime = 5;
    private int numberOfRuns = 1;
    private String reportsFolder = "";
    private ReedsSheppCarPlanner.PLANNING_ALGORITHM pathPlanner = ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect;
    private Heuristics heuristics = new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST);
    private int vehicleCounter = 0;
    private final List<AbstractVehicle> vehicles = new ArrayList<>();

    public String getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(String projectFile) {
        this.projectFile = projectFile;
    }

    public ProjectData getProjectData() {
        return projectData;
    }

    public void setProjectData(ProjectData projectData) {
        this.projectData = projectData;
    }

    public ProjectData getOriginalProjectData() {
        return originalProjectData;
    }

    public void setOriginalProjectData(ProjectData orignalProjectData) {
        this.originalProjectData = orignalProjectData;
    }

    public MapData getMapData() {
        return mapData;
    }

    public void setMapData(MapData mapData) {
        this.mapData = mapData;
    }

    public Boolean getWriteVehicleReports() {
        return writeVehicleReports;
    }

    public void setWriteVehicleReports(Boolean writeVehicleReports) {
        this.writeVehicleReports = writeVehicleReports;
    }

    public int getSimulationTime() {
        return simulationTime;
    }

    public void setSimulationTime(int simulationTime) {
        this.simulationTime = simulationTime;
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    public void setNumberOfRuns(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }

    public String getReportsFolder() {
        return reportsFolder;
    }

    public void setReportsFolder(String reportsFolder) {
        this.reportsFolder = reportsFolder;
    }

    public ReedsSheppCarPlanner.PLANNING_ALGORITHM getPathPlanner() {
        return pathPlanner;
    }

    public void setPathPlanner(ReedsSheppCarPlanner.PLANNING_ALGORITHM pathPlanner) {
        this.pathPlanner = pathPlanner;
    }

    public Heuristics getHeuristics() {
        return heuristics;
    }

    public void setHeuristics(Heuristics heuristics) {
        this.heuristics = heuristics;
    }

    public int getVehicleCounter() {
        return vehicleCounter;
    }

    public void setVehicleCounter(int vehicleCounter) {
        this.vehicleCounter = vehicleCounter;
    }

    public boolean isPlansVerified() {
        return isPlansVerified;
    }

    public void setPlansVerified(boolean plansVerified) {
        isPlansVerified = plansVerified;
    }

    public void addVehicle(AbstractVehicle vehicle) {
        this.vehicles.add(vehicle);
    }

    public List<AbstractVehicle> getVehicles() {
        return vehicles;
    }

    public void reset() {
        this.isPlansVerified = false;
        this.projectFile = "";
        this.projectData = new ProjectData();
        this.originalProjectData = new ProjectData();
        this.mapData = new MapData();
        this.writeVehicleReports = false;
        this.simulationTime = 5;
        this.numberOfRuns = 1;
        this.reportsFolder = "";
        this.pathPlanner = ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect;
        this.heuristics = new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST);
        this.vehicleCounter = 0;
        this.vehicles.clear();
    }

    //TODO: Add a method to check if all vehicles have saved paths
    //FIXME: The method should be if oldProjectData is same as new ProjectData, this method will not be needed.
    public boolean allVehiclesHavePaths() {
        return projectData.getVehicles().stream().allMatch(vehicle -> vehicle.getPathFile() != null);
    }

}