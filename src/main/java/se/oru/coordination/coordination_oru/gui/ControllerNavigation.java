package se.oru.coordination.coordination_oru.gui;

import javafx.scene.control.Button;

public class ControllerNavigation {
    private final Button back = new Button("Back");
    private final Button next = new Button("Next");
    private final Button save = new Button("Save");
    private final Button reset = new Button("Reset");
    private final Button verify = new Button("Verify");
    private final Button run = new Button("Run");
    private final Main main;
    private final VerifyPlan verifyPlan = new VerifyPlan(this);
    final SaveProject saveProject = new SaveProject(this);
    private final RunProject runProject = new RunProject(this);
    private SceneState currentSceneState = SceneState.HOME;
    public ControllerNavigation(Main main) {
        this.main = main;
    }

    public void getNavigationController() {
        main.getNavigationButton().next.setOnAction(e -> clickNext());
        main.getNavigationButton().back.setOnAction(e -> clickBack());
        main.getNavigationButton().save.setOnAction(e -> saveProject.clickSave());
        main.getNavigationButton().reset.setOnAction(e -> clickReset());
        main.getNavigationButton().verify.setOnAction(e -> verifyPlan.clickVerify());
        main.getNavigationButton().run.setOnAction(e -> runProject.clickRun());
    }

    public void updateScene(SceneState newScene) {
        setScene(newScene);
        newScene.update(main);
    }

    public void clickBack() {
        var currentScene = getCurrentScene();
        var backState = currentScene.getBackState();
        if (backState != null) {
            updateScene(backState);
        }
    }

    public void clickNext() {
        var currentScene = getCurrentScene();
        var nextState = currentScene.getNextState();
        if (nextState != null) {
            updateScene(nextState);
        }
    }

    public void clickReset() {
        updateScene(SceneState.HOME);
        main.getNavigationButton().next.setDisable(true);
        main.getHomeScene().getFilePath().setText("");
        main.getDataStatus().reset();
    }

    public SceneState getCurrentScene() {
        return currentSceneState;
    }

    public void setScene(SceneState sceneState) {
        this.currentSceneState = sceneState;
    }

    public Button getBack() {
        return back;
    }

    public Button getNext() {
        return next;
    }

    public Button getSave() {
        return save;
    }

    public Button getReset() {
        return reset;
    }

    public Button getVerify() {
        return verify;
    }

    public Button getRun() {
        return run;
    }

    public Main getMain() {
        return main;
    }

    public void clickRun() {
        runProject.runProject();
    }
}
