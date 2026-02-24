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


steps for run-->>
Step-1:: 
========open file=======
<img width="1214" height="768" alt="Screenshot 2026-02-24 142628" src="https://github.com/user-attachments/assets/824e2280-206f-4e32-acac-ac10ec5b60b6" />

Step-2::
select version 1 ,2 and 3 prefer 2
<img width="1476" height="751" alt="Screenshot 2026-02-24 142646" src="https://github.com/user-attachments/assets/be9272eb-7602-431b-91ad-b3c60cb4e142" />

Step-3::
Software on your window
<img width="1919" height="1077" alt="Screenshot 2026-02-24 142721" src="https://github.com/user-attachments/assets/40a194de-dadf-4d42-9eda-c53af86ef670" />


