# 🚀 ABES Java Code Editor (MiniVSCode)

A lightweight Java-based IDE built using JavaFX.  
This project demonstrates how a real IDE manages:

- Code Editing
- Compilation & Execution
- File Management
- Embedded Web Server Integration (Tomcat)

---

# 🔥 Versions Overview

## ✅ Version 0.1
- JavaFX Based IDE
- Compile & Run Java Programs
- File Handling Support
- Packaged JAR Execution

## ✅ Version 0.2
- All features of v0.1
- Embedded Apache Tomcat Integration
- Local Server Start/Stop Support
- Web Project Execution

---

# 🛠 Requirements

- JDK 17+ (Tested on JDK 24)
- JavaFX SDK 25
- (For v0.2) Tomcat Embed JARs inside `lib/`

---

# 📦 Clone Project

```bash
git clone https://github.com/Arshiv341/ABES-java-code-editor.git
cd ABES-java-code-editor

▶️ How To Run
🔹 Version 0.1 (Using Packaged JAR)
Step 1
cd C:\MiniVSCodeProject\dist
Step 2
java --module-path "C:\MiniVSCodeProject\javafx-sdk-25\lib" \
--add-modules javafx.controls,javafx.fxml \
-jar MiniVSCode.jar
🔹 Version 0.1 (Manual Compile & Run)
Step 1
cd C:\MiniVSCodeProject\src
Step 2 (Compile)
"C:\Program Files\Java\jdk-24\bin\javac.exe" \
--module-path "C:\MiniVSCodeProject\javafx-sdk-25\lib" \
--add-modules javafx.controls,javafx.fxml \
DSAtraing\MiniVSCode.java
Step 3 (Run)
java --module-path "C:\MiniVSCodeProject\javafx-sdk-25\lib" \
--add-modules javafx.controls,javafx.fxml \
DSAtraing.MiniVSCode
🔹 Version 0.2 (With Embedded Tomcat)

⚠ Make sure all Tomcat embed JAR files are inside lib/
All versions must be SAME (e.g., 10.1.49)

Step 1 (Compile)
javac --module-path "javafx-sdk-25\lib" \
--add-modules javafx.controls,javafx.fxml \
-cp ".;lib/*" \
-d out \
src\DSAtraing\MiniVSCode.java
Step 2 (Run)
java --module-path "javafx-sdk-25\lib" \
--add-modules javafx.controls,javafx.fxml \
-cp ".;lib/*;out" \
DSAtraing.MiniVSCode
📁 Project Structure
MiniVSCodeProject/
│
├── src/
│   └── DSAtraing/
│       └── MiniVSCode.java
│
├── lib/                (Tomcat JARs for v0.2)
├── javafx-sdk-25/
├── dist/
│   └── MiniVSCode.jar
└── out/
🌐 Embedded Tomcat Details (v0.2)

Uses:

tomcat-embed-core

tomcat-embed-el

tomcat-embed-websocket

Server starts on:

http://localhost:8080
🎯 Learning Highlights

Java Modular Execution

JavaFX UI Architecture

Embedded Server Lifecycle

Dependency Conflict Debugging

IDE Architecture Understanding

👨‍💻 Author

Ritik Tiwari
Java Developer | Backend Enthusiast | System Builder

⭐ If You Like This Project

Give it a ⭐ on GitHub and connect on LinkedIn!


## 🚀 Steps to Run

### Step-1: Open File
![Open File](https://i.ibb.co/gb4Z3dfJ/69c21a0a9edd.png)

### Step-2: Select Version (Prefer 2)
![Select Version](https://i.ibb.co/gL9F9zB9/88615a685fda.png)

### Step-3: Software on Your Window
![Software Window](https://i.ibb.co/mC3X4RwY/d2eb5c60ffb0.png)


