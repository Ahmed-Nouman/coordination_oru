package se.oru.coordination.coordination_oru.gui;

public enum SceneState implements SceneUpdater {
    HOME {
        @Override
        public void update(Main main) {
            main.getPrimaryStage().setTitle("Coordination_ORU");
            main.getPrimaryStage().setScene(main.getHomeScene().get());
            main.getPrimaryStage().centerOnScreen();
            main.getNavigationButton().getNext().setDisable(false);
        }

        @Override
        public SceneState getBackState() {
            return null;
        }

        @Override
        public SceneState getNextState() {
            return MAP;
        }

    },
    MAP {
        @Override
        public void update(Main main) {
            main.getPrimaryStage().setTitle("Coordination_ORU: Setting up the map");
            main.getPrimaryStage().setScene(main.getMapScene().get());
            main.getPrimaryStage().centerOnScreen();
            main.getNavigationButton().getNext().setDisable(false);
        }

        @Override
        public SceneState getBackState() {
            return HOME;
        }

        @Override
        public SceneState getNextState() {
            return VEHICLE;
        }

    },
    VEHICLE {
        @Override
        public void update(Main main) {
            main.getPrimaryStage().setTitle("Coordination_ORU: Setting up the vehicles");
            main.getPrimaryStage().setScene(main.getVehicleScene().get());
            main.getPrimaryStage().centerOnScreen();
        }

        @Override
        public SceneState getBackState() {
            return MAP;
        }

        @Override
        public SceneState getNextState() {
            return EXPERIMENT;
        }

    },
    EXPERIMENT {
        @Override
        public void update(Main main) {
            main.getPrimaryStage().setTitle("Coordination_ORU: Setting up the simulation");
            main.getPrimaryStage().setScene(main.getSetupScene().get());
            main.getPrimaryStage().centerOnScreen();
        }

        @Override
        public SceneState getBackState() {
            return VEHICLE;
        }

        @Override
        public SceneState getNextState() {
            return null;
        }

    };
}
