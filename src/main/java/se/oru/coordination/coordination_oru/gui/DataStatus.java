package se.oru.coordination.coordination_oru.gui;

import se.oru.coordination.coordination_oru.utils.Heuristics;

public class DataStatus {
    private String projectFile = "";
    private ProjectData projectData;
    private ProjectData originalProjectData;
    private MapData mapData;
    private Boolean writeVehicleReports = false;
    private int simulationTime = 5;
    private int numberOfRuns = 1;
    private String reportsFolder = "";
    private Heuristics heuristics = new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST);
    private int vehicleCounter = 0;
    private Boolean isNewProject = false;

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

    public Boolean isNewProject() {
        return isNewProject;
    }

    public void setNewProject(Boolean newProject) {
        isNewProject = newProject;
    }
}