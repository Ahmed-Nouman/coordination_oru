package se.oru.coordination.coordination_oru.gui;
import se.oru.coordination.coordination_oru.gui.ProjectData.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

/**
 * The TabbedGUI class demonstrates a simple GUI with a tabbed layout
 * and provides shortcut functionality for navigation and actions.
 */
public class GUIInterface extends javax.swing.JFrame {

    private static JButton createButton;  // Class-level field to store the Create button
    private static JButton openButton;
    private static JButton nextButton;
    private static JButton backButton;
    private final JFrame frame;
    private final JTabbedPane tabbedPane;
    private boolean openActionPerformed = false; // Flag to track if "Open" action was performed
    private ProjectData projectData;
    private String selectedImagePath;  // Variable to store the selected image path
    private JPanel buttonPanel = new JPanel(new GridBagLayout());

    /**
     * Constructor for the GUIInterface. Initializes the GUI components.
     */
    public GUIInterface() {
        // Packing and displaying the frame
        frame = new JFrame("Coordination_ORU");
        frame.pack();
        frame.setMinimumSize(new Dimension(500, 400));  // Optional: set a minimum size
        frame.setLocationRelativeTo(null);  // Center the frame on screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        // Create the Project panel
        JPanel projectPanel = getProjectPanel();

        tabbedPane.addTab("Project", projectPanel);
        tabbedPane.addTab("Map", new JPanel());
        tabbedPane.addTab("Vehicles", new JPanel());
        tabbedPane.addTab("Simulation", new JPanel());

        frame.add(tabbedPane, BorderLayout.CENTER);

        // Disable all tabs in the JTabbedPane
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setEnabledAt(i, false);
        }

        // Initialize and configure menu bar
        initializeMenuBar();

        // Initialize and configure button panel
        initializeButtonPanel();

        setKeyBindings();

        frame.setVisible(true);
    }

    /**
     * The main method, entry point of the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIInterface::new);
    }

    private static void createAndShowImageFrame(String imagePath) {
        JFrame frame = new JFrame("Image Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            ImageIcon icon = new ImageIcon(image);

            JLabel label = new JLabel(icon);
            frame.add(label, BorderLayout.CENTER);

            frame.pack();
            frame.setLocationRelativeTo(null); // Center the frame on screen
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JPanel getProjectPanel() {

        // Create the Project panel and set layout
        JPanel projectPanel = new JPanel();
        projectPanel.setLayout(new BoxLayout(projectPanel, BoxLayout.Y_AXIS));

        // Welcome to label with bigger font and some vertical space above it
        JLabel welcomeMessage = new DisplayLine().createLine(Font.BOLD, 24, "Welcome to Coordination_ORU!");

        // Create a panel for the Create and Load buttons
        JPanel createOpenPanel = getCreateOpenPanel();

        projectPanel.add(welcomeMessage);
        projectPanel.add(createOpenPanel);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performCreateAction();
            }
        });

        // Add an ActionListener to the "Load" button to load a project file when clicked
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performOpenAction();
            }
        });

        return projectPanel;
    }

    private JPanel getCreateOpenPanel() {
        JPanel createOpenPanel = new JPanel(new GridBagLayout());
        GridBagConstraints createOpenConstraints = new GridBagConstraints();

        // Common constraints
        createOpenConstraints.weightx = 1.0;  // Distribute space evenly between the buttons
        createOpenConstraints.fill = GridBagConstraints.HORIZONTAL;  // Make buttons occupy the entire cell horizontally
        createOpenConstraints.gridy = 0;  // All buttons on the same row
        createOpenConstraints.insets = new Insets(0, 30, 0, 30);  // Some padding between the buttons

        createButton = new JButton("Create Project");
        createOpenConstraints.gridx = 0;  // Column 0
        createOpenPanel.add(createButton, createOpenConstraints);

        openButton = new JButton("Open Project");
        createOpenConstraints.gridx = 1;  // Column 1
        createOpenPanel.add(openButton, createOpenConstraints);

        createOpenPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return createOpenPanel;
    }

    /**
     * Initializes the menu bar with File and Help menus.
     */
    private void initializeMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu bar with File and Help
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem createProjectItem = new JMenuItem("Create Project");
        createProjectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

        JMenuItem loadProjectItem = new JMenuItem("Load Project");
        loadProjectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));

        JMenuItem saveProjectItem = new JMenuItem("Save Project");
        saveProjectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));

        // TODO: Add action listeners to these items as needed
        fileMenu.add(createProjectItem);
        fileMenu.add(loadProjectItem);
        fileMenu.add(saveProjectItem);
        fileMenu.addSeparator();  // Separates the 'Quit' item from others
        fileMenu.add(quitItem);

        menuBar.add(fileMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        JMenuItem contentsItem = new JMenuItem("Information");
        contentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));

        // TODO: Add action listener to display information
        helpMenu.add(aboutItem);
        helpMenu.add(contentsItem);

        menuBar.add(helpMenu);

        frame.setJMenuBar(menuBar);
    }

    /**
     * Initializes the button panel with Back, Next, Save, and Run buttons.
     */
    private void initializeButtonPanel() {
        GridBagConstraints constraints = new GridBagConstraints();

        // Common constraints
        constraints.weightx = 1.0;  // Distribute space evenly among all buttons
        constraints.fill = GridBagConstraints.HORIZONTAL;  // Make buttons occupy the entire cell horizontally
        constraints.gridy = 0;  // All buttons on the same row

        // Setting horizontal padding. Change the 10 to your desired spacing.
        int padding = 10;

        // Back button
        backButton = new JButton("Back");
        backButton.setEnabled(false); // Disable the button initially
        constraints.gridx = 0;  // Column 0
        constraints.insets = new Insets(0, 0, 0, padding);  // No padding before "Back"
        buttonPanel.add(backButton, constraints);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performBackAction();
            }
        });

        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.setEnabled(false);
        constraints.gridx = 1;  // Column 1
        constraints.insets = new Insets(0, padding, 0, padding);  // Padding on both sides
        buttonPanel.add(saveButton, constraints);

        // Run button
        JButton runButton = new JButton("Run");
        runButton.setEnabled(false);
        constraints.gridx = 2;  // Column 2
        constraints.insets = new Insets(0, padding, 0, padding);  // Padding on both sides
        buttonPanel.add(runButton, constraints);

        // Next button
        nextButton = new JButton("Next");
        nextButton.setEnabled(false);
        constraints.gridx = 3;  // Column 3
        constraints.insets = new Insets(0, padding, 0, 0);  // No padding after "Next"
        buttonPanel.add(nextButton, constraints);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performNextAction();
            }
        });

        // TODO: Add actionListeners for Save and Run as needed

        frame.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setVisible(false);
    }

    /**
     * Sets key bindings for the buttons.
     */
    private void setKeyBindings() {
        // Key bindings using InputMap and ActionMap
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();

        // Back action
        inputMap.put(KeyStroke.getKeyStroke("ctrl B"), "goBack");
        actionMap.put("goBack", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchTab(-1);
            }
        });

        // Next action
        inputMap.put(KeyStroke.getKeyStroke("ctrl N"), "goNext");
        actionMap.put("goNext", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchTab(1);
            }
        });

        // Save action
        inputMap.put(KeyStroke.getKeyStroke("ctrl S"), "save");
        actionMap.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Implement Save functionality
                System.out.println("Save action triggered.");
            }
        });

        // Run action
        inputMap.put(KeyStroke.getKeyStroke("ctrl R"), "run");
        actionMap.put("run", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Implement Run functionality
                System.out.println("Run action triggered.");
            }
        });
    }

    /**
     * Switches the active tab based on the given direction.
     *
     * @param direction -1 for previous tab, 1 for next tab
     */
    private void switchTab(int direction) {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (direction < 0 && selectedIndex > 0) {
            tabbedPane.setSelectedIndex(selectedIndex - 1);
        } else if (direction > 0 && selectedIndex < tabbedPane.getTabCount() - 1) {
            tabbedPane.setSelectedIndex(selectedIndex + 1);
        }
    }

    private void performCreateAction() {

        // Initialize the JFileChooser to the user's home directory
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));

        // Set it to files only (not directories)
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Choose a location to save the project");

        // Use a custom file filter to only allow files with .json extension
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showSaveDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Append .json extension if it doesn't already exist
            if (!selectedFile.getAbsolutePath().endsWith(".json")) {
                selectedFile = new File(selectedFile + ".json");
            }

            // Check if the file already exists
            if (selectedFile.exists()) {
                JOptionPane.showMessageDialog(frame, "A file with this name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (selectedFile.createNewFile()) {
                    // Write an initial empty JSON structure to the file
                    FileWriter writer = new FileWriter(selectedFile);
                    writer.write("{}");  // Write an empty JSON object
                    writer.close();

                    JOptionPane.showMessageDialog(frame, "Project file created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Move to the next action after successful file creation
                    performNextAction();

                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to create project file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException i) {
                i.printStackTrace();
                JOptionPane.showMessageDialog(frame, "An error occurred while creating the file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void getCreateMapPanel() {
        // Create the Map panel and set layout
        JPanel mapPanel = new JPanel();
        mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.Y_AXIS));

        // Create a label with instructions
        JLabel mapMessage = new DisplayLine().createLine(Font.PLAIN, 20, "Please select a valid map file:");
        // TODO Maybe it is better to show image path, may be not

        nextButton.setEnabled(false);

        // Create a button to open the file chooser dialog
        JButton chooseFileButton = new JButton("Choose File");
        chooseFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open a file chooser dialog for PNG files
                JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Files", "png");
                fileChooser.setFileFilter(filter);

                int result = fileChooser.showOpenDialog(frame);

                if (result == JFileChooser.APPROVE_OPTION) {
                    // User selected a PNG file
                    File selectedFile = fileChooser.getSelectedFile();
                    selectedImagePath = selectedFile.getAbsolutePath();  // Store the path in the variable
                    // Open the PNG file in a new window using the existing method
                    createAndShowImageFrame(selectedImagePath);

//                    projectData.setMap(selectedImagePath); FIXME
                    nextButton.setEnabled(true);
                }
            }
        });

        // Add components to the Map panel
        mapPanel.add(mapMessage);
        mapPanel.add(chooseFileButton);

        // Replace the content of the "Map" tab with the Map panel
        tabbedPane.setComponentAt(1, mapPanel); // Assuming "Map" tab index is 1
    }

    private void getCreateVehiclesPanel() {

        // Create the Vehicles panel
        JPanel vehiclesPanel = new JPanel(new BorderLayout());

        // Left side: Display message and Delete Vehicle button
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("Vehicles");
        leftPanel.add(messageLabel, BorderLayout.NORTH);
        JButton deleteVehicleButton = new JButton("Delete Vehicle");
        leftPanel.add(deleteVehicleButton, BorderLayout.SOUTH);

        // Right side: 14 rows with labels, components, and text fields, and Add Vehicle button
        JPanel rightPanel = new JPanel(new GridLayout(14, 2)); // 14 rows, 2 columns

        // Create labels for each row
        JLabel[] labels = {
                new JLabel("Name:"),
                new JLabel("Width (m):"),
                new JLabel("Length (m):"),
                new JLabel("Max. Velocity (m/s):"),
                new JLabel("Max. Acceleration (m/s^2):"),
                new JLabel("Color:"),
                new JLabel("Initial Location:"),
                new JLabel("Goal Location:"),
                new JLabel("Human Operated:"),
                new JLabel("Look Ahead Distance (m):")
        };

        // Create JComboBox for "Color" with specific options
        String[] colorOptions = {"Red", "Green", "Yellow", "Blue", "Black"};
        JComboBox<String> colorComboBox = new JComboBox<>(colorOptions);

        System.out.println(projectData);
        // Create JComboBox for "Initial Location" and "Goal Location" with 20 options
        String[] locationOptions = {"mainTunnelLeft", "mainTunnelRight", "entrance", "drawPoint12", "drawPoint13",
                "drawPoint14", "drawPoint27", "drawPoint28", "drawPoint29", "drawPoint29A", "drawPoint30",
                "drawPoint31", "drawPoint32", "drawPoint32A", "drawPoint33", "drawPoint34", "drawPoint35",
                "orePass1", "orePass2", "orePass3"};
        JComboBox<String> initialLocationComboBox = new JComboBox<>(locationOptions);
        JComboBox<String> goalLocationComboBox = new JComboBox<>(locationOptions);

        // Create text fields for other properties
        JTextField[] fields = {
                new JTextField(),
                new JTextField(),
                new JTextField(),
                new JTextField(),
                new JTextField()
        };

        // Create checkbox for "Human Operated"
        JCheckBox humanOperatedCheckBox = new JCheckBox("Human Operated");

        // Create label and text field for "Look Ahead Distance" initially set to not visible
        JLabel lookAheadDistanceLabel = new JLabel("Look Ahead Distance (m):");
        JTextField lookAheadDistanceField = new JTextField();
        lookAheadDistanceLabel.setVisible(false); // Initially not visible
        lookAheadDistanceField.setVisible(false); // Initially not visible

        // Add components to the right panel
        rightPanel.add(labels[0]);
        rightPanel.add(fields[0]); // Name
        rightPanel.add(labels[1]);
        rightPanel.add(fields[1]); // Width
        rightPanel.add(labels[2]);
        rightPanel.add(fields[2]); // Length
        rightPanel.add(labels[3]);
        rightPanel.add(fields[3]); // Max. Velocity
        rightPanel.add(labels[4]);
        rightPanel.add(fields[4]); // Max. Acceleration
        rightPanel.add(labels[5]);
        rightPanel.add(colorComboBox); // Color
        rightPanel.add(labels[6]);
        rightPanel.add(initialLocationComboBox); // Initial Location
        rightPanel.add(labels[7]);
        rightPanel.add(goalLocationComboBox); // Goal Location
        rightPanel.add(labels[8]);
        rightPanel.add(humanOperatedCheckBox); // Human Operated

        // Add an ActionListener to the checkbox to control the visibility of the "Look Ahead Distance" components
        humanOperatedCheckBox.addActionListener(e -> {
            boolean isSelected = humanOperatedCheckBox.isSelected();
            lookAheadDistanceLabel.setVisible(isSelected);
            lookAheadDistanceField.setVisible(isSelected);
            rightPanel.revalidate(); // Revalidate the panel to reflect the changes
        });

        // Add the "Look Ahead Distance" components to the right panel
        rightPanel.add(lookAheadDistanceLabel); // Look Ahead Distance Label
        rightPanel.add(lookAheadDistanceField); // Look Ahead Distance Text Field

        // Add Vehicle button to the bottom of the right panel
        JButton addVehicleButton = new JButton("Add Vehicle");
        rightPanel.add(addVehicleButton);

        // Create a JSplitPane to divide the JFrame horizontally
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.2); // Set the initial width ratio

        // Add the split pane to the Vehicles panel
        vehiclesPanel.add(splitPane, BorderLayout.CENTER);

        // Replace the content of the "Vehicles" tab with the Vehicles panel
        tabbedPane.setComponentAt(2, vehiclesPanel); // Assuming "Vehicles" tab index is 2
    }

    private void performOpenAction() {

        openActionPerformed = true;

        // Initialize the JFileChooser to the user's home directory
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));

        // Set it to allow selecting only files (not directories)
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Open a project file");

        // Use a custom file filter to only allow files with .json extension
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files", "json");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                // Read the JSON file into a JsonObject
                JsonObject jsonObject = parseJsonFromFile(selectedFile);

                // Use Gson to parse the JSON data into a ProjectData object
                projectData = new Gson().fromJson(jsonObject, ProjectData.class);
                String imagePath = getImageFilePath(projectData.getMap());
                String currentDirectory = System.getProperty("user.dir");
                String imageFilePath = currentDirectory + "/maps/" + imagePath;

                SwingUtilities.invokeLater(() -> createAndShowImageFrame(imageFilePath));

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "An error occurred while reading the file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        performNextAction();
    }

    private void getOpenMapPanel() {
        // Create the Map panel and set layout
        JPanel mapPanel = new JPanel();
        mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.Y_AXIS));

        // Create a label with instructions
        JLabel mapMessage = new DisplayLine().createLine(Font.PLAIN, 20, "Do you want to update the map file:");

        // Create a button to open the file chooser dialog
        JButton chooseFileButton = new JButton("Choose File");
        chooseFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open a file chooser dialog for PNG files
                JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Files", "png");
                fileChooser.setFileFilter(filter);

                int result = fileChooser.showOpenDialog(frame);

                if (result == JFileChooser.APPROVE_OPTION) {
                    // User selected a PNG file
                    File selectedFile = fileChooser.getSelectedFile();
                    selectedImagePath = selectedFile.getAbsolutePath();  // Store the path in the variable
                    // Open the PNG file in a new window using the existing method
                    createAndShowImageFrame(selectedImagePath);
                }
            }
        });

        // Add components to the Map panel
        mapPanel.add(mapMessage);
        mapPanel.add(chooseFileButton);

        // Replace the content of the "Map" tab with the Map panel
        tabbedPane.setComponentAt(1, mapPanel); // Assuming "Map" tab index is 1

    }

    private void getOpenVehiclesPanel() {
        // Create the Vehicles panel
        JPanel vehiclesPanel = new JPanel(new BorderLayout());

        // Left side: Display message and Delete Vehicle button
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("Vehicles");
        leftPanel.add(messageLabel, BorderLayout.NORTH);

        // Create a JTextArea to display robot names
        JTextArea VehicleNamesTextArea = new JTextArea(10, 20); // Rows, Columns
        VehicleNamesTextArea.setEditable(false); // Make it non-editable

        // Populate the JTextArea with robot names from ProjectData
//        var vehicles = projectData.getRobots();
//        System.out.println(vehicles);
//        for (Vehicle vehicle : vehicles) {
//            VehicleNamesTextArea.append(vehicle.getName() + "\n");
//        }

        leftPanel.add(new JScrollPane(VehicleNamesTextArea), BorderLayout.CENTER);

        JButton deleteVehicleButton = new JButton("Delete Vehicle");
        leftPanel.add(deleteVehicleButton, BorderLayout.SOUTH);

        // Right side: 14 rows with labels, components, and text fields, and Add Vehicle button
        JPanel rightPanel = new JPanel(new GridLayout(14, 2)); // 14 rows, 2 columns

        // Create labels for each row
        JLabel[] labels = {
                new JLabel("Name:"),
                new JLabel("Width (m):"),
                new JLabel("Length (m):"),
                new JLabel("Max. Velocity (m/s):"),
                new JLabel("Max. Acceleration (m/s^2):"),
                new JLabel("Color:"),
                new JLabel("Initial Location:"),
                new JLabel("Goal Location:"),
                new JLabel("Human Operated:"),
                new JLabel("Look Ahead Distance (m):")
        };

        // Create JComboBox for "Color" with specific options
        String[] colorOptions = {"Red", "Green", "Yellow", "Blue", "Black"};
        JComboBox<String> colorComboBox = new JComboBox<>(colorOptions);

        Map<String, Pose> listOfAllPoses = projectData.getListOfAllPoses();
        String[] locationOptions = listOfAllPoses.keySet().toArray(new String[0]);

        JComboBox<String> initialLocationComboBox = new JComboBox<>(locationOptions);
        JComboBox<String> goalLocationComboBox = new JComboBox<>(locationOptions);

        // Create text fields for other properties
        JTextField[] fields = {
                new JTextField(),
                new JTextField(),
                new JTextField(),
                new JTextField(),
                new JTextField()
        };

        // Create checkbox for "Human Operated"
        JCheckBox humanOperatedCheckBox = new JCheckBox("Human Operated");

        // Create label and text field for "Look Ahead Distance" initially set to not visible
        JLabel lookAheadDistanceLabel = new JLabel("Look Ahead Distance (m):");
        JTextField lookAheadDistanceField = new JTextField();
        lookAheadDistanceLabel.setVisible(false); // Initially not visible
        lookAheadDistanceField.setVisible(false); // Initially not visible

        // Add components to the right panel
        rightPanel.add(labels[0]);
        rightPanel.add(fields[0]); // Name
        rightPanel.add(labels[1]);
        rightPanel.add(fields[1]); // Width
        rightPanel.add(labels[2]);
        rightPanel.add(fields[2]); // Length
        rightPanel.add(labels[3]);
        rightPanel.add(fields[3]); // Max. Velocity
        rightPanel.add(labels[4]);
        rightPanel.add(fields[4]); // Max. Acceleration
        rightPanel.add(labels[5]);
        rightPanel.add(colorComboBox); // Color
        rightPanel.add(labels[6]);
        rightPanel.add(initialLocationComboBox); // Initial Location
        rightPanel.add(labels[7]);
        rightPanel.add(goalLocationComboBox); // Goal Location
        rightPanel.add(labels[8]);
        rightPanel.add(humanOperatedCheckBox); // Human Operated

        // Add an ActionListener to the checkbox to control the visibility of the "Look Ahead Distance" components
        humanOperatedCheckBox.addActionListener(e -> {
            boolean isSelected = humanOperatedCheckBox.isSelected();
            lookAheadDistanceLabel.setVisible(isSelected);
            lookAheadDistanceField.setVisible(isSelected);
            rightPanel.revalidate(); // Revalidate the panel to reflect the changes
        });

        // Add the "Look Ahead Distance" components to the right panel
        rightPanel.add(lookAheadDistanceLabel); // Look Ahead Distance Label
        rightPanel.add(lookAheadDistanceField); // Look Ahead Distance Text Field

        // Add Vehicle button to the bottom of the right panel
        JButton addVehicleButton = new JButton("Add Vehicle");
        rightPanel.add(addVehicleButton);

        // Create a JSplitPane to divide the JFrame horizontally
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.2); // Set the initial width ratio

        // Add the split pane to the Vehicles panel
        vehiclesPanel.add(splitPane, BorderLayout.CENTER);

        // Replace the content of the "Vehicles" tab with the Vehicles panel
        tabbedPane.setComponentAt(2, vehiclesPanel); // Assuming "Vehicles" tab index is 2
    }

    private JsonObject parseJsonFromFile(File file) throws IOException {
        // Use FileReader to read the JSON file
        try (FileReader reader = new FileReader(file)) {
            try (JsonReader jsonReader = new JsonReader(reader)) {
                Gson gson = new Gson();
                return gson.fromJson(jsonReader, JsonObject.class);
            } catch (JsonParseException e) {
                e.printStackTrace();
                throw new IOException("Error parsing JSON file", e);
            }
        }
    }

    public String getImageFilePath(String yamlFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(yamlFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("image:")) {
                    String[] parts = line.split(":");
                    if (parts.length >= 2) {
                        return parts[1].trim();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void performNextAction() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex < tabbedPane.getTabCount() - 1) {
            if (!openActionPerformed && selectedIndex == 0) { // Check if the "Open" action was not performed and the selected tab is "Map"
                getCreateMapPanel(); // Call the method to create and display the welcome message
            } else if (openActionPerformed && selectedIndex == 0) {
                buttonPanel.setVisible(true);
                nextButton.setEnabled(true);
                getOpenMapPanel();
            } else if (!openActionPerformed && selectedIndex == 1) {
                getCreateVehiclesPanel();
            } else if (openActionPerformed && selectedIndex == 1) {
                getOpenVehiclesPanel();
            }
            tabbedPane.setSelectedIndex(selectedIndex + 1);
            buttonPanel.setVisible(true);
            backButton.setEnabled(true);
            nextButton.setEnabled(true);
        }
        // Any other 'next' actions you'd like to perform...
    }


    private void performBackAction() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex > 0) {
            tabbedPane.setSelectedIndex(selectedIndex - 1);
        }
        if (selectedIndex == 1) {
            buttonPanel.setVisible(false);
        }
        // Any other 'back' actions you'd like to perform...
    }
}

