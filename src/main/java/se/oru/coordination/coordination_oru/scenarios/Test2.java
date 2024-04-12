package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.utils.Mission;
import se.oru.coordination.coordination_oru.utils.Task;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;
import se.oru.coordination.coordination_oru.vehicles.VehiclesHashMap;

import java.awt.*;

public class Test2 {
    public static void main(String[] args) {

        final Pose mainTunnelLeft = new Pose(4.25,15.35, Math.PI);
        final Pose mainTunnelRight = new Pose(78.05,24.75, Math.PI);
        final Pose drawPoint15 = new Pose(9.75,83.95,Math.PI/2);
        final Pose drawPoint16 = new Pose(16.85,86.35,Math.PI/2);
        final Pose drawPoint21 = new Pose(52.95,87.75,Math.PI/2);
        final Pose drawPoint23 = new Pose(67.55,86.65,Math.PI/2);
        final Pose orePass = new Pose(54.35,11.25,Math.PI/2);
        final String map = "maps/mine-map-test.yaml";
        final ForwardModel model = new ConstantAcceleration(10.0, 1.0, 1000, 1000, 30);
        final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                0.09, 60, 2.0, 0.1);

        var chargingVehicle = new AutonomousVehicle("E",1, Color.BLUE, 5.0, 0.5,
                0.8, 0.5, mainTunnelLeft, 10, 2, model);
//        chargingVehicle.setGoals(new Pose[] {orePass, mainTunnelLeft, mainTunnelRight}); //FIXME: For getPlans 1) set Goal/Goals/Tasks and remove GoalPoses
        chargingVehicle.addTask(new Task(0.25, new Pose[] {drawPoint16}));
        chargingVehicle.addTask(new Task(0.25, new Pose[] {drawPoint23}));
        chargingVehicle.addTask(new Task(0.25, new Pose[] {mainTunnelLeft}));
        chargingVehicle.generatePlans(planner);

        var autonomousVehicle1 = new AutonomousVehicle("A1",2, Color.YELLOW, 5.0, 0.5,
                0.8, 0.5, drawPoint21, 10, 3, model);
        autonomousVehicle1.addTask(new Task(0.10, new Pose[] {orePass}));
        autonomousVehicle1.addTask(new Task(0.10, new Pose[] {drawPoint21}));
        autonomousVehicle1.generatePlans(planner);

//        var autonomousVehicle2 = new AutonomousVehicle("A2",2, Color.YELLOW, 5.0, 0.5,
//                0.8, 0.5, drawPoint15, 10, 0);
//        autonomousVehicle2.addTask(new Task(new Pose[] {orePass}, 3000));
//        autonomousVehicle2.addTask(new Task(new Pose[] {drawPoint15}, 6000));
//        autonomousVehicle2.generatePlans(YAML_FILE);

        // Instantiate a trajectory envelope coordinator.
        var tec = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, 5, 2);
        // Need to set up infrastructure that maintains the representation
        tec.setupSolver(0, 100000000);
        // Start the thread that checks and enforces dependencies at every clock tick
        tec.startInference();

        tec.setDefaultFootprint(chargingVehicle.getFootprint());
        tec.placeRobotsAtStartPoses();
        tec.addComparator(new Heuristics(Heuristics.HeuristicType.CLOSEST_FIRST).getComparator());
        tec.setUseInternalCriticalPoints(false);
        tec.setYieldIfParking(true);
        tec.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(map);
        viz.setFontScale(4);
        viz.setInitialTransform(11, 45, -3.5);
        tec.setVisualization(viz);

//        var lookAheadVehicleInitialPlan = lookAheadVehicle.getLimitedPath(lookAheadVehicle.getID(), predictableDistance, tec);
//        Missions.generateMissions();
        Missions.setMap(map);
        Missions.generateMissions();
//        tec.addMissions(mission);

        //Start a mission dispatching thread for each robot, which will run forever
//        for (int i = 1; i <= VehiclesHashMap.getList().size(); i++) {
//            Thread t = runTasksForEachRobot(i, tec);
//            //Start the thread!
//            t.start();
//        }

        Missions.runTasks(tec, -1);
    }

    private static Thread runTasksForEachRobot(int i, TrajectoryEnvelopeCoordinatorSimulation tec) {
        final int robotID = i;
        return new Thread() {
            int iteration = 0;
            @Override
            public void run() {
                while (true) {
                    Mission m = Missions.getMission(robotID, iteration%Missions.getMissions(robotID).size());
                    synchronized(tec) {
                        try {
                            Thread.sleep((long) VehiclesHashMap.getVehicle(i).getTasks().get(iteration).getTimeInMinutes());  //FIXME: This works
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (tec.addMissions(m)) iteration++;
                    }
                    //Sleep for a little (2 sec)
                    try { Thread.sleep(2000); }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }
            }
        };
    }

}
