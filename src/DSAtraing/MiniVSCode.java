package DSAtraing;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Insets;
import javafx.scene.Scene;
//import java.lang.classfile.Label;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.util.Optional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import java.awt.Desktop;
import java.net.URI;

public class MiniVSCode extends Application {

    private TabPane tabPane;
    private TextArea console;
    private TextArea terminal;
    private boolean terminalVisible = true;

    private TreeView<String> projectTree;
    private Label statusBar;
    private StringBuilder inputBuffer = new StringBuilder();
    private File projectRoot = new File(".");
    private File currentDir = new File(System.getProperty("user.dir"));
    private Process currentProcess;

    private HashMap<Tab, File> fileMap = new HashMap<>();
    private Tomcat tomcat;

    // private int inputStartIndex = 0;
    // private String lastUserInput = "";
    // private volatile boolean waitingForInput = false;

    @Override
    public void start(Stage stage) {

        tabPane = new TabPane();
        // 🌟 FUTURISTIC: Darker editor background and tab style .
        tabPane.setStyle("""
                -fx-background-color:#0b0b0b;
                -fx-tab-min-height:30px;
                -fx-tab-max-height:30px;
                -fx-padding: 0;
                /* Note: Complex tab styling requires external CSS, but we set the base dark color */
                """);
        
        // ================= CONSOLE =================
        console = new TextArea();
        console.setEditable(false);
        // 🌟 FUTURISTIC: VS Code/Dark theme terminal-like console
        console.setStyle("""
                -fx-control-inner-background:#1e1e1e;
                -fx-text-fill:#99ff99; /* Lighter Green for output contrast */
                -fx-font-family: 'Consolas', monospace;
                -fx-font-size:14;
                -fx-highlight-fill: #007acc;
                -fx-highlight-text-fill: white;
                -fx-border-color: #3c3c3c;
                -fx-border-width: 0 0 1px 0; /* Separator line */
                """);

console.setOnKeyPressed(e -> {

    if (currentProcess == null) return;


    // BACKSPACE
    if (e.getCode() == KeyCode.BACK_SPACE) {
        if (inputBuffer.length() > 0) {
            inputBuffer.deleteCharAt(inputBuffer.length() - 1);
            Platform.runLater(() -> {
                int len = console.getText().length();
                console.deleteText(len - 1, len);
            });
        }
        e.consume();
        return;
    }

    // ENTER = SEND INPUT
    if (e.getCode() == KeyCode.ENTER) {
        e.consume();

        String input = inputBuffer.toString();

        // empty input JVM ko mat bhejo
        if (!input.isEmpty()) {
            sendInput(input);
        }

        inputBuffer.setLength(0);

        Platform.runLater(() -> console.appendText("\n"));
        return;
    }

    // NORMAL CHAR INPUT
    if (!e.isControlDown() && e.getText().length() > 0) {
        char ch = e.getText().charAt(0);
        inputBuffer.append(ch);
        Platform.runLater(() -> console.appendText(String.valueOf(ch)));
        e.consume();
    }
});


// console.addEventFilter(KeyEvent.KEY_TYPED, e -> {
//    // User sirf last input position ke baad hi type kar sake
//    if (console.getCaretPosition() < inputStartIndex) {
//        e.consume();
//        console.positionCaret(console.getText().length());
//    }
// });




        // ================= TERMINAL =================
        terminal = new TextArea();
        // 🌟 FUTURISTIC: VS Code/Dark theme terminal-like
        terminal.setStyle("""
                -fx-control-inner-background:#000000;
                -fx-text-fill:#00ff00;
                -fx-font-family: 'Consolas', monospace;
                -fx-font-size:14;
                """);

        terminal.appendText(currentDir.getAbsolutePath() + "> ");

        terminal.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String text = terminal.getText();
                String cmd = text.substring(text.lastIndexOf(">") + 1).trim();
                runTerminalCommand(cmd);
                e.consume();
            }
        });

        VBox consoleBox = new VBox(console, terminal);
        // 🌟 FUTURISTIC: Console/Terminal box background
        consoleBox.setStyle("-fx-background-color: #1e1e1e;");

        // ================= PROJECT TREE =================
        projectTree = new TreeView<>();
        projectTree.setPrefWidth(240);
        projectTree.setRoot(buildTree(projectRoot));
        // 🌟 FUTURISTIC: VS Code Activity Bar/File Explorer style
        projectTree.setStyle("""
                -fx-background-color:#252526;
                -fx-control-inner-background:#252526;
                -fx-text-fill: #cccccc;
                -fx-font-size: 13;
                -fx-border-color: #3c3c3c;
                -fx-border-width: 0 1px 0 0;
                """);

        projectTree.setOnMouseClicked(e -> {
            TreeItem<String> item = projectTree.getSelectionModel().getSelectedItem();
            if (item != null) {
                File f = new File(item.getValue());
                if (f.isFile() && f.getName().endsWith(".java")) {
                    openFileInTab(f);
                }
            }
        });

        // ================= ICON TOOLBAR =================
        Button newBtn = iconBtn("📄", "New File");
        Button openBtn = iconBtn("📂", "Open File");
        Button saveBtn = iconBtn("💾", "Save");
        Button runBtn  = iconBtn("▶", "Run Program");
        Button folderBtn = iconBtn("🗂", "Select Folder");
        Button terminalBtn = iconBtn("💻", "Toggle Terminal");

        ToolBar toolBar = new ToolBar(
                newBtn, openBtn, saveBtn, runBtn, folderBtn, terminalBtn
        );
        // 🌟 FUTURISTIC: Thicker, darker toolbar (Activity Bar)
        toolBar.setStyle("-fx-background-color:#333333; -fx-padding: 5 10 5 10; -fx-spacing: 10;");
        Button tomcatBtn = iconBtn("🌐", "Start Tomcat");
Button stopTomcatBtn = iconBtn("⛔", "Stop Tomcat");

toolBar.getItems().addAll(tomcatBtn, stopTomcatBtn);

tomcatBtn.setOnAction(e -> startEmbeddedTomcat());
stopTomcatBtn.setOnAction(e -> stopEmbeddedTomcat());
        newBtn.setOnAction(e -> createNewFileWithName());
        openBtn.setOnAction(e -> openFile(stage));
        saveBtn.setOnAction(e -> saveCurrentFile());
        runBtn.setOnAction(e -> runProgram());
        folderBtn.setOnAction(e -> selectProjectFolder(stage));

        terminalBtn.setOnAction(e -> {
            terminalVisible = !terminalVisible;
            terminal.setVisible(terminalVisible);
            terminal.setManaged(terminalVisible);
            console.setManaged(!terminalVisible); // Only show console when terminal is hidden
            console.setVisible(!terminalVisible); //
            statusBar.setText(terminalVisible ? "Terminal Opened" : "Console Opened");
        });
        
        // 🌟 Initialize console and terminal to split view
        terminal.setManaged(false); 
        terminal.setVisible(false);
        console.setManaged(true);
        console.setVisible(true);

        // ================= STATUS BAR =================
        statusBar = new Label("Ready");
        statusBar.setPadding(new Insets(6));
        // 🌟 FUTURISTIC: VS Code status bar style
        statusBar.setStyle(
                "-fx-background-color:#007acc;" + // Deep blue status bar
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-padding: 5 15 5 15;"
        );
        

        // ================= LAYOUT =================
        SplitPane hSplit = new SplitPane(projectTree, tabPane);
        SplitPane vSplit = new SplitPane(hSplit, consoleBox);
        vSplit.setOrientation(Orientation.VERTICAL);

        // 🌟 FUTURISTIC: Default split position
        hSplit.setDividerPosition(0, 0.2); // Project tree takes 20%
        vSplit.setDividerPosition(0, 0.75); // Editor takes 75%

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(vSplit);
        root.setBottom(statusBar);
        // 🌟 FUTURISTIC: Main application background
        root.setStyle("-fx-background-color:#1e1e1e;");

        stage.setScene(new Scene(root, 1350, 780));
        stage.setTitle("ABES JAVA EDITOR");

try {
    stage.getIcons().add(
        new javafx.scene.image.Image(
            new FileInputStream("DSAtraing/logo.png")
        )
    );
} catch (Exception e) {
    System.out.println("⚠ Logo load nahi ho paaya");
}

stage.show();


    }
    // ================= EMBEDDED TOMCAT =================
private void startEmbeddedTomcat() {
    try {
        if (tomcat != null) {
            statusBar.setText("Tomcat Already Running");
            return;
        }

        tomcat = new Tomcat();
        tomcat.setBaseDir("tomcat-temp");   // ⚠ IMPORTANT
        tomcat.setPort(9090);

        tomcat.getConnector(); 

        File base = new File(".");
        tomcat.addContext("", base.getAbsolutePath());

        tomcat.start();

        statusBar.setText("Tomcat Started at http://localhost:8080");

        Desktop.getDesktop().browse(new URI("http://localhost:8080"));

    } catch (Exception e) {
        e.printStackTrace();   // 🔥 DO NOT REMOVE
        statusBar.setText("Tomcat Start Failed");
    }
}

    // ================= ICON BUTTON MAKER =================
    private Button iconBtn(String icon, String tip) {
        Button btn = new Button(icon);
        btn.setTooltip(new Tooltip(tip));

        // Base style
        final String baseStyle = "-fx-background-color: transparent; -fx-text-fill: #cccccc; -fx-font-size: 18; -fx-min-width: 40; -fx-min-height: 32; -fx-padding: 0;";
        // Hover style
        final String hoverStyle = baseStyle + "-fx-background-color: #3c3c3c;";
        
        btn.setStyle(baseStyle);

        // 🌟 FUTURISTIC: Add hover effect using event handlers (internal approach)
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }

    // ================= TERMINAL EXECUTION =================
    private void runTerminalCommand(String command) {
        try {
            if (command.equals("pwd")) {
                terminal.appendText("\n" + currentDir.getAbsolutePath());
            } else if (command.startsWith("cd")) {
                File newDir = new File(currentDir, command.replace("cd", "").trim());
                if (newDir.exists()) currentDir = newDir;
                else terminal.appendText("\nDirectory not found");
            } else {
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
                pb.directory(currentDir);
                pb.redirectErrorStream(true);
                Process p = pb.start();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));

                String line;
                while ((line = br.readLine()) != null) {
                    String out = line;
                    Platform.runLater(() -> terminal.appendText("\n" + out));
                }
            }
        } catch (Exception e) {
            terminal.appendText("\n[Terminal Error]");
        } finally {
            Platform.runLater(() ->
                    terminal.appendText("\n" + currentDir.getAbsolutePath() + "> "));
        }
    }

 // ================= RUN PROGRAM (AUTO STOP) =================
private void runProgram() {
    try {
        saveCurrentFile();
        console.clear();
        inputBuffer.setLength(0);



        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        File file = fileMap.get(tab);
        if (file == null) return;

        String filePath = file.getAbsolutePath();
        String fileName = file.getName().replace(".java", "");
        String parent = file.getParentFile().getParent();


        // ✅ PACKAGE AUTO DETECTION
        String packageName = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("package ")) {
                    packageName = line
                            .replace("package", "")
                            .replace(";", "")
                            .trim();
                    break;
                }
            }
        }

        String fullClassName = packageName.isEmpty()
                ? fileName
                : packageName + "." + fileName;

        statusBar.setText("Running : " + fullClassName);

        ProcessBuilder pb = new ProcessBuilder(
    "cmd", "/c",
    "chcp 65001 > nul && " +
    "javac -d \"" + parent + "\" \"" + filePath + "\" && " +
    "java -Dfile.encoding=UTF-8 -cp \"" + parent + "\" " + fullClassName
);


        pb.redirectErrorStream(true);
        currentProcess = pb.start();

        new Thread(() -> {
            try (Reader reader = new InputStreamReader(
                    currentProcess.getInputStream(),
                    StandardCharsets.UTF_8)) {

                int ch;
                while ((ch = reader.read()) != -1) {
                    char c = (char) ch;

                    Platform.runLater(() -> {
    console.appendText(String.valueOf(c));
});

                }
            } catch (Exception ignored) {}
        }).start();

        new Thread(() -> {
            try {
                currentProcess.waitFor();
                Platform.runLater(() -> {
                    statusBar.setText("Finished");
                    //waitingForInput = false;
                    console.appendText("\n[Process Finished]\n");
                });
            } catch (Exception ignored) {}
        }).start();

    } catch (Exception e) {
        console.appendText("Run error\n");
    }
}

private void sendInput(String data) {
    try {
        if (currentProcess != null) {
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            currentProcess.getOutputStream(),
                            StandardCharsets.UTF_8));

            bw.write(data);
            bw.newLine();
            bw.flush();
        }
    } catch (Exception e) {
        console.appendText("\n[Input Send Error]\n");
    }
}



    private void newTab() {
        TextArea editor = createEditor();
        Tab tab = new Tab("Untitled");
        tab.setContent(editor);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void openFile(Stage stage) {
        FileChooser fc = new FileChooser();
        // 🌟 FUTURISTIC: Set initial directory to project root
        fc.setInitialDirectory(projectRoot);
        File file = fc.showOpenDialog(stage);
        if (file != null) openFileInTab(file);
    }

    private void openFileInTab(File file) {
        try {
            String text = new String(new FileInputStream(file).readAllBytes(), StandardCharsets.UTF_8);
            TextArea editor = createEditor();
            editor.setText(text);

            Tab tab = new Tab(file.getName());
            tab.setContent(editor);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            fileMap.put(tab, file);
        } catch (Exception ignored) {}
    }

    private void saveCurrentFile() {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == null) return;

        File file = fileMap.get(tab);
        if (file == null) {
            FileChooser fc = new FileChooser();
            // 🌟 FUTURISTIC: Set initial directory to project root
            fc.setInitialDirectory(projectRoot);
            file = fc.showSaveDialog(null);
            if (file == null) return;
            fileMap.put(tab, file);
        }

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file),
                        StandardCharsets.UTF_8))) {

            TextArea editor = (TextArea) tab.getContent();
            bw.write(editor.getText());
            statusBar.setText("Auto-Saved: " + file.getName());
        } catch (Exception ignored) {}
    }

    private void selectProjectFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            projectRoot = dir;
            projectTree.setRoot(buildTree(projectRoot));
            statusBar.setText("Project Folder Set: " + dir.getName());
        }
    }

  private TextArea createEditor() {
    TextArea editor = new TextArea();

    // 🌟 FUTURISTIC: Darker editor and better syntax-like colors
    editor.setStyle("""
            -fx-control-inner-background:#1e1e1e;
            -fx-text-fill:#d4d4d4; /* Lighter grey for better readability */
            -fx-font-family: 'Consolas', monospace;
            -fx-font-size:14;
            -fx-background-color:#1e1e1e;
            -fx-padding: 10;
            -fx-caret-color: white; /* Visible blinking cursor */
            """);

    // ✅ AUTO CLOSE CURLY BRACE
    editor.addEventFilter(KeyEvent.KEY_TYPED, e -> {
        if ("{".equals(e.getCharacter())) {
            int pos = editor.getCaretPosition();
            editor.insertText(pos, "}");
            editor.positionCaret(pos);
        }
    });

    // ✅ SMART AUTO INDENT ON ENTER
    editor.setOnKeyPressed(e -> {
        if (e.getCode() == KeyCode.ENTER) {
            e.consume();  // ❗ Default newline ko roko (double line fix)

            int caretPos = editor.getCaretPosition();
            String text = editor.getText();

            int lastNewLine = text.lastIndexOf("\n", caretPos - 1);
            String prevLine = (lastNewLine == -1)
                    ? text.substring(0, caretPos)
                    : text.substring(lastNewLine + 1, caretPos);

            String indent = prevLine.replaceAll("^(\\s*).*", "$1");

            if (prevLine.trim().endsWith("{")) {
                indent += "    ";    // 4 spaces
            }

            editor.insertText(caretPos, "\n" + indent);
        }
    });

    editor.textProperty().addListener((a, b, c) -> saveCurrentFile());
    return editor;
}



    private TreeItem<String> buildTree(File file) {
        TreeItem<String> root = new TreeItem<>(file.getPath());
        root.setExpanded(true);

        if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list != null) {
                for (File f : list) {
                    root.getChildren().add(buildTree(f));
                }
            }
        }
        return root;
    }
    private void createNewFileWithName() {

    TextInputDialog dialog = new TextInputDialog("MyFile.java");
    // 🌟 FUTURISTIC: Darker dialog theme applied inline
    dialog.getDialogPane().setStyle("""
        -fx-background-color: #1e1e1e; 
        -fx-text-fill: #d4d4d4; 
        -fx-border-color: #007acc;
        -fx-border-width: 1px;
    """);
    dialog.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #252526;");
    dialog.getDialogPane().lookup(".content").setStyle("-fx-text-fill: #cccccc;");
    
    // Attempt to style the input field
    TextField tf = (TextField) dialog.getDialogPane().lookup(".text-field");
    if (tf != null) {
        tf.setStyle("-fx-control-inner-background:#3c3c3c; -fx-text-fill:#d4d4d4;");
    }

    dialog.setTitle("New File");
    dialog.setHeaderText("Enter file name");
    dialog.setContentText("File name:");

    Optional<String> result = dialog.showAndWait();
    if (result.isEmpty()) return;

    String fileName = result.get().trim();
    if (!fileName.endsWith(".java")) fileName += ".java";

    // ✅ Class ka naam file se nikaalo
    String className = fileName.replace(".java", "");

    try {
        File newFile = new File(projectRoot, fileName);
        if (!newFile.exists()) newFile.createNewFile();

        // ✅ DEFAULT INTELLIJ-TYPE TEMPLATE
        String defaultCode =
                "package DSAtraing;\n\n" +
                "import java.util.*;\n\n" +
                "public class " + className + " {\n\n" +
                "    public static void main(String[] args) {\n\n" +
                "    }\n" +
                "}\n";

        // ✅ File ke andar code auto-write
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(newFile),
                        StandardCharsets.UTF_8))) {

            bw.write(defaultCode);
        }

        // ✅ Editor me bhi same code dikhao
        TextArea editor = createEditor();
        editor.setText(defaultCode);

        Tab tab = new Tab(newFile.getName());
        tab.setContent(editor);

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        fileMap.put(tab, newFile);

        projectTree.setRoot(buildTree(projectRoot));
        statusBar.setText("Created with template: " + newFile.getName());

    } catch (Exception ex) {
        statusBar.setText("File Create Error");
    }
}
    public static void main(String[] args) {
        launch(args);
    }
}