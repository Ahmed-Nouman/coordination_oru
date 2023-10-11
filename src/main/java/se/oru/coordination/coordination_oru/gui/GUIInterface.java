package se.oru.coordination.coordination_oru.gui;

import org.json.simple.parser.ParseException;
import se.oru.coordination.coordination_oru.gui_oru.GuiTool;
import se.oru.coordination.coordination_oru.gui_oru.interface1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The TabbedGUI class demonstrates a simple GUI with a tabbed layout
 * and provides shortcut functionality for navigation and actions.
 */
public class GUIInterface {

    private static JButton createButton;  // Class-level field to store the Create button
    private static JButton loadButton;
    private static JButton nextButton;
    private static JButton backButton;
    private final JFrame frame;
    private final JTabbedPane tabbedPane;

    /**
     * Constructor for the SimpleGUI. Initializes the GUI components.
     */
    public GUIInterface() {
        // Packing and displaying the frame
        frame = new JFrame("Coordination_ORU GUI Interface");
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

        // Initialize and configure button panel
        initializeButtonPanel();

        // Initialize and configure menu bar
        initializeMenuBar();

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

    private JPanel getProjectPanel() {

        // Create the Project panel and set layout
        JPanel projectPanel = new JPanel();
        projectPanel.setLayout(new BoxLayout(projectPanel, BoxLayout.Y_AXIS));

        // Welcome to label with bigger font and some vertical space above it
        JLabel welcomeMessage = getWelcomeMessage();

        // Additional instruction label
        JLabel instructionLabel = getInstructionLabel();

        // Create a panel for the Create and Load buttons
        JPanel createLoadPanel = getCreateLoadPanel();

        // Instruction to enter project name
        JLabel projectInstruction = getProjectInstruction();

        // Text field initialized with "project.json"
        JTextField projectName = getProjectName();

        projectPanel.add(welcomeMessage);
        projectPanel.add(instructionLabel);
        projectPanel.add(createLoadPanel);
        projectPanel.add(projectInstruction);
        projectPanel.add(Box.createVerticalStrut(5)); // Insert a vertical strut of 5 pixels here
        projectPanel.add(projectName);
        projectPanel.add(Box.createVerticalStrut(50));

        // Add an ActionListener to the "Create" button to enable the projectInstruction when clicked
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectInstruction.setVisible(true);
                projectName.setVisible(true);
                nextButton.setEnabled(true);
            }
        });

        // Add an ActionListener to the "Load" button to load a project file when clicked
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // Open the user's home directory

                int result = fileChooser.showOpenDialog(frame);  // Show the file open dialog

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    if (selectedFile.getName().endsWith(".json")) {
                        // The selected file has a .json extension
                        System.out.println("Selected JSON file: " + selectedFile.getAbsolutePath());
                        // You can proceed to read and process this JSON file

                        // If file read successfully, enable the buttons and perform the next action
                        performNextAction();
                        nextButton.setEnabled(true);
                        backButton.setEnabled(true);
                        // If the file was read successfully:
                    } else {
                        // Show an error message or a notification
                        JOptionPane.showMessageDialog(frame, "Please select a valid JSON file.", "Invalid File Type", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        return projectPanel;
    }

    private JLabel getWelcomeMessage() {

        String message = "<html><center>Welcome to the Coordination_ORU<br>GUI Interface!</center></html>";
        JLabel welcomeLabel = new JLabel(message, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font(welcomeLabel.getFont().getName(), Font.BOLD, 24));
        welcomeLabel.setBorder(new EmptyBorder(20, 0, 10, 0));  // Adjust space as per your requirement
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return welcomeLabel;
    }

    private JLabel getInstructionLabel() {
        JLabel instructionLabel = new JLabel("Please create/load the project file.", SwingConstants.CENTER);
        instructionLabel.setFont(new Font(instructionLabel.getFont().getName(), Font.PLAIN, 20));
        instructionLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return instructionLabel;
    }

    private JPanel getCreateLoadPanel() {
        JPanel createLoadPanel = new JPanel(new GridBagLayout());
        GridBagConstraints createLoadConstraints = new GridBagConstraints();

        // Common constraints
        createLoadConstraints.weightx = 1.0;  // Distribute space evenly between the buttons
        createLoadConstraints.fill = GridBagConstraints.HORIZONTAL;  // Make buttons occupy the entire cell horizontally
        createLoadConstraints.gridy = 0;  // All buttons on the same row
        createLoadConstraints.insets = new Insets(0, 30, 0, 30);  // Some padding between the buttons

        createButton = new JButton("Create");
        createLoadConstraints.gridx = 0;  // Column 0
        createLoadPanel.add(createButton, createLoadConstraints);

        loadButton = new JButton("Load");
        createLoadConstraints.gridx = 1;  // Column 1
        createLoadPanel.add(loadButton, createLoadConstraints);
        createLoadPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return createLoadPanel;
    }

    private JLabel getProjectInstruction() {
        JLabel projectNameInstruction = new JLabel("Enter the name of project file:", SwingConstants.CENTER);
        projectNameInstruction.setBorder(new EmptyBorder(5, 0, 0, 0));
        projectNameInstruction.setAlignmentX(Component.CENTER_ALIGNMENT);
        projectNameInstruction.setVisible(false);  // Disable the instruction label initially
        return projectNameInstruction;
    }

    private JTextField getProjectName() {
        JTextField projectNameField = new JTextField("project.json");

        int singleLineHeight = projectNameField.getPreferredSize().height; // Get the default preferred height (single line)
        projectNameField.setPreferredSize(new Dimension(300, singleLineHeight));
        projectNameField.setMinimumSize(new Dimension(300, singleLineHeight));
        projectNameField.setMaximumSize(new Dimension(300, singleLineHeight));
        projectNameField.setHorizontalAlignment(JTextField.CENTER); // Center the text inside the JTextField
        projectNameField.setVisible(false);
        return projectNameField;
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
        JPanel buttonPanel = new JPanel(new GridBagLayout());
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

    private void performLoadAction() {

    }

    private void performNextAction() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex < tabbedPane.getTabCount() - 1) {
            tabbedPane.setSelectedIndex(selectedIndex + 1);
            backButton.setEnabled(true);
        }
        // Any other 'next' actions you'd like to perform...
    }

    private void performBackAction() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex > 0) {
            tabbedPane.setSelectedIndex(selectedIndex - 1);
        }
        if (selectedIndex == 1) {
            backButton.setEnabled(false);
        }
        // Any other 'back' actions you'd like to perform...
    }

}

