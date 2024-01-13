package se.oru.coordination.coordination_oru.gui;

public interface SceneUpdater {
    void update(Main main);
    SceneState getBackState();
    SceneState getNextState();
}
