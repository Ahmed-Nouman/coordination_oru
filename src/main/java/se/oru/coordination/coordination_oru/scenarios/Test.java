package se.oru.coordination.coordination_oru.scenarios;

import org.metacsp.multi.spatioTemporal.paths.Pose;
import se.oru.coordination.coordination_oru.forwardModel.ConstantAcceleration;
import se.oru.coordination.coordination_oru.utils.Task;
import se.oru.coordination.coordination_oru.forwardModel.ForwardModel;
import se.oru.coordination.coordination_oru.motionPlanning.VehiclePathPlanner;
import se.oru.coordination.coordination_oru.motionPlanning.ompl.ReedsSheppCarPlanner;
import se.oru.coordination.coordination_oru.coordinator.TrajectoryEnvelopeCoordinatorSimulation;
import se.oru.coordination.coordination_oru.simulation.BrowserVisualization;
import se.oru.coordination.coordination_oru.utils.Heuristics;
import se.oru.coordination.coordination_oru.utils.Missions;
import se.oru.coordination.coordination_oru.vehicles.AutonomousVehicle;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Test {

    public static final TrajectoryEnvelopeCoordinatorSimulation TEC = new TrajectoryEnvelopeCoordinatorSimulation(2000, 1000, 5, 2);

    public static void main(String[] args) throws Exception {
        String className = Thread.currentThread().getStackTrace()[Thread.currentThread().getStackTrace().length-1].getFileName().split("\\.")[0];
        String folderName = "paths/" + className + "/";
        final Pose mainTunnelLeft = new Pose(4.25,15.35, -Math.PI);
        final Pose mainTunnelRight = new Pose(78.05,24.75, Math.PI);
        final Pose drawPoint21 = new Pose(52.95,87.75,-Math.PI/2);
        final Pose orePass = new Pose(54.35,11.25,-Math.PI/2);
        final String map = "maps/mine-map-test.yaml";
        final ForwardModel model = new ConstantAcceleration(10.0, 100.0, 1000, 1000, 30);
        final var planner = new VehiclePathPlanner(map, ReedsSheppCarPlanner.PLANNING_ALGORITHM.RRTConnect,
                0.09, 30, 2.0, 0.1);

        var autonomousVehicle = new AutonomousVehicle("A1",1, Color.YELLOW, 10.0, 1.0,
                0.9, 0.65, drawPoint21, 2.4, 2, model);
//        autonomousVehicle.setGoals(new Pose[] {mainTunnelRight, drawPoint21});
//        autonomousVehicle.setGoals(new Pose[] {mainTunnelRight, drawPoint21});
        autonomousVehicle.addTask(new Task(0, new Pose[] {mainTunnelRight}, 1));
        autonomousVehicle.addTask(new Task(0.1, new Pose[] {drawPoint21}, 0));

//        var autonomousVehicle1 = new AutonomousVehicle("A2",1, Color.YELLOW, 10.0, 1.0,
//                0.9, 0.65, orePass, 2.4, 2, model);
//        autonomousVehicle1.addTask(new Task(0, new Pose[] {mainTunnelLeft}, 0));
//        autonomousVehicle1.addTask(new Task(0.1, new Pose[] {orePass}, 1));

//        autonomousVehicle.generatePlans(planner);
//        autonomousVehicle1.generatePlans(planner);
//        autonomousVehicle.savePlans(className);
//        autonomousVehicle1.savePlans(className);
        autonomousVehicle.loadPlans(folderName + "A1.path");
//        autonomousVehicle1.loadPlans(folderName + "A2.path");

        // Instantiate a trajectory envelope coordinator.
        // Need to set up infrastructure that maintains the representation
        TEC.setupSolver(0, 100000000);
        // Start the thread that checks and enforces dependencies at every clock tick
        TEC.startInference();

        TEC.setDefaultFootprint(autonomousVehicle.getFootprint());
        TEC.placeRobotsAtStartPoses();
//        tec.placeRobot(autonomousVehicle.getID(), autonomousVehicle.getPaths().get(0)[0].getPose()); //FIXME: DO Automatic placing of vehicles
//        tec.placeRobot(autonomousVehicle1.getID(), autonomousVehicle1.getPaths().get(0)[0].getPose());
        TEC.addComparator(new Heuristics(Heuristics.HeuristicType.MISSION_PRIORITY_FIRST).getComparator());
        TEC.setUseInternalCriticalPoints(false);
        TEC.setYieldIfParking(true);
        TEC.setBreakDeadlocks(true, false, false);

        // Set up a simple GUI (null means an empty map, otherwise provide yaml file)
        var viz = new BrowserVisualization();
        viz.setMap(map);
        viz.setFontScale(4);
        viz.setInitialTransform(11, 45, -3.5);
        TEC.setVisualization(viz);

//        var m1 = new Mission(autonomousVehicle.getID(), autonomousVehicle.getPaths().get(0));
//        var m2 = new Mission(autonomousVehicle.getID(), autonomousVehicle.getPaths().get(1));
//        var m3 = new Mission(autonomousVehicle1.getID(), autonomousVehicle1.getPaths().get(0));
//        var m4 = new Mission(autonomousVehicle1.getID(), autonomousVehicle1.getPaths().get(1));

//        Missions.enqueueMission(m1);
//        Missions.enqueueMission(m2);
//        Missions.enqueueMission(m3);
//        Missions.enqueueMission(m4);
        Missions.generateMissions();
        Missions.setMap(map);
        Missions.runTasks(TEC, -1);
        scheduleVehicleStop();
    }

    public static void scheduleVehicleStop() throws InterruptedException {
        // Create a scheduled executor service to manage timing
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Runnable task to stop the first vehicle after 2000 steps
        Runnable stopFirstVehicle = new Runnable() {
            private int stepCounter = 0;

            @Override
            public void run() {
                // The step at which to stop the first vehicle.
                int stopStep = 100;
                if (stepCounter == stopStep) {
                    System.out.println(TEC.trackers.get(1).getRobotReport().getPathIndex());
                    System.out.println("Stopping the first vehicle."); // Print the stopping message
                    TEC.trackers.get(1).pause(); // Stop tracking the first vehicle
//                    TEC.trackers.get(1).stopTracking();
//                    TEC.trackers.get(1).startTracking();
                    return; // Stop executing this runnable
                }
                stepCounter++;  // Increment the step counter

                // Reschedule this task to run again after 100 milliseconds if the scheduler is not shut down
                if (!scheduler.isShutdown()) {
                    scheduler.schedule(this, 100, TimeUnit.MILLISECONDS);
                }
            }
        };

        // Schedule the task to start after 2 seconds
        scheduler.schedule(stopFirstVehicle, 2, TimeUnit.SECONDS);

        // Keep main thread alive to allow time for the scheduler to complete tasks
        scheduler.awaitTermination(1, TimeUnit.HOURS);  // Wait for up to 1 hour for scheduler to terminate
    }


}
