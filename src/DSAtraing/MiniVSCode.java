package DSAtraing;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.util.Optional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import javafx.stage.Popup;

public class MiniVSCode extends Application {

    private TabPane tabPane;
    private TextArea console;
    private TextArea terminal;
    private boolean terminalVisible = true;

    private TreeView<String> projectTree;
    private Label statusBar;

    private File projectRoot = new File(".");
    private File currentDir = new File(System.getProperty("user.dir"));
    private Process currentProcess;

    private HashMap<Tab, File> fileMap = new HashMap<>();

    private int inputStartIndex = 0;
    private String lastUserInput = "";
    private volatile boolean waitingForInput = false;

    @Override
    public void start(Stage stage) {

        tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color:#1e1e1e;");

        // ================= CONSOLE =================
        console = new TextArea();
        console.setStyle("""
                -fx-control-inner-background:#0c0c0c;
                -fx-text-fill:#00ff00;
                -fx-font-family:Consolas;
                -fx-font-size:13;
                """);

   console.setOnKeyPressed(e -> {

    if (currentProcess != null && e.getCode() == KeyCode.ENTER) {
        e.consume();
    }

    if (currentProcess != null && waitingForInput && e.getCode() == KeyCode.ENTER) {

        int len = console.getText().length();

        lastUserInput = console.getText()
                .substring(inputStartIndex, len)
                .replace("\n", "")
                .replace("\r", "")
                .trim();

        inputStartIndex = len;
        waitingForInput = false;

        sendInput();
        lastUserInput = "";   // ✅ VERY IMPORTANT
    }
});


        // ================= TERMINAL =================
        terminal = new TextArea();
        terminal.setStyle("""
                -fx-control-inner-background:#000000;
                -fx-text-fill:#00ff00;
                -fx-font-family:Consolas;
                -fx-font-size:13;
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

        // ================= PROJECT TREE =================
        projectTree = new TreeView<>();
        projectTree.setPrefWidth(240);
        projectTree.setRoot(buildTree(projectRoot));
        projectTree.setStyle("""
                -fx-background-color:#1e1e1e;
                -fx-control-inner-background:#1e1e1e;
                -fx-text-fill:white;
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
        Button newBtn = iconBtn("＋", "New File");
        Button openBtn = iconBtn("📂", "Open File");
        Button saveBtn = iconBtn("💾", "Save");
        Button runBtn  = iconBtn("▶", "Run Program");
        Button folderBtn = iconBtn("🗂", "Select Folder");
        Button terminalBtn = iconBtn(">_", "Toggle Terminal");

        ToolBar toolBar = new ToolBar(
                newBtn, openBtn, saveBtn, runBtn, folderBtn, terminalBtn
        );
        toolBar.setStyle("-fx-background-color:#2b2b2b;");

        newBtn.setOnAction(e -> createNewFileWithName());
        openBtn.setOnAction(e -> openFile(stage));
        saveBtn.setOnAction(e -> saveCurrentFile());
        runBtn.setOnAction(e -> runProgram());
        folderBtn.setOnAction(e -> selectProjectFolder(stage));

        terminalBtn.setOnAction(e -> {
            terminalVisible = !terminalVisible;
            terminal.setVisible(terminalVisible);
            terminal.setManaged(terminalVisible);
            statusBar.setText(terminalVisible ? "Terminal Opened" : "Terminal Hidden");
        });

        // ================= STATUS BAR =================
        statusBar = new Label("Ready");
        statusBar.setPadding(new Insets(6));
        statusBar.setStyle(
                "-fx-background-color:#2b2b2b;" +
                "-fx-text-fill:white;"
        );

        // ================= LAYOUT =================
        SplitPane hSplit = new SplitPane(projectTree, tabPane);
        SplitPane vSplit = new SplitPane(hSplit, consoleBox);
        vSplit.setOrientation(Orientation.VERTICAL);

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(vSplit);
        root.setBottom(statusBar);
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

    // ================= ICON BUTTON MAKER =================
    private Button iconBtn(String icon, String tip) {
        Button btn = new Button(icon);
        btn.setTooltip(new Tooltip(tip));
        btn.setStyle("""
                -fx-background-color:#3c3f41;
                -fx-text-fill:white;
                -fx-font-size:16;
                -fx-min-width:40;
                -fx-min-height:32;
                -fx-background-radius:6;
        """);
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
            waitingForInput = false;

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            File file = fileMap.get(tab);
            if (file == null) return;

            String filePath = file.getAbsolutePath();
            String fileName = file.getName().replace(".java", "");
            String parent = file.getParent();

            statusBar.setText("Running : " + fileName);

            ProcessBuilder pb = new ProcessBuilder(
                    "cmd", "/c",
                    "chcp 65001 > nul && javac \"" + filePath + "\" && " +
                    "java -Dfile.encoding=UTF-8 -cp \"" +
                    parent + "\" " + fileName
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
            final char cc = c;

            Platform.runLater(() -> {
                // 💚 Pehle output dikhao
                console.appendText(String.valueOf(cc));

                // 💚 Yahin par, UI thread me hi inputStartIndex set karo
                if (cc == ':' && !waitingForInput) {
                    waitingForInput = true;
                    inputStartIndex = console.getText().length();
                }
            });
        }
    } catch (Exception ignored) {}
}).start();


            new Thread(() -> {
                try {
                    currentProcess.waitFor();
                    Platform.runLater(() -> {
                        statusBar.setText("Finished");
                        waitingForInput = false;
                        console.appendText("\n[Process Finished]\n");
                    });
                } catch (Exception ignored) {}
            }).start();

        } catch (Exception e) {
            console.appendText("Run error\n");
        }
    }

private void sendInput() {
    try {
        if (currentProcess != null) {

            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                            currentProcess.getOutputStream(),
                            StandardCharsets.UTF_8));

            if (lastUserInput.trim().isEmpty()) return;

bw.write(lastUserInput.trim());
bw.newLine();
bw.flush();

waitingForInput = false;
 
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
            statusBar.setText("Auto-Saved");
        } catch (Exception ignored) {}
    }

    private void selectProjectFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            projectRoot = dir;
            projectTree.setRoot(buildTree(projectRoot));
        }
    }

  private TextArea createEditor() {
    TextArea editor = new TextArea();

    editor.setStyle("""
            -fx-control-inner-background:#1e1e1e;
            -fx-text-fill:#dcdcdc;
            -fx-font-family:Consolas;
            -fx-font-size:14;
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
                indent += "    ";   // 4 spaces
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
