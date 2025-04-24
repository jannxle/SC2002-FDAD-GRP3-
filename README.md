# BTO System Management Application
<img width="637" alt="image" src="https://github.com/user-attachments/assets/5b100708-4508-4d1f-b1d1-ebf974f5b08b"/>

## SC2002-FDAD-Group 3
> Login Page
<img width="486" alt="image" src="https://github.com/user-attachments/assets/29f4d2f4-9f92-4018-bf45-e63ab8e75cc7" />

## Project Structure overview: 
<img width="290" alt="image" src="https://github.com/user-attachments/assets/587dc5b7-0ec7-4ab5-9fa5-b2c036f820b5" />



## Scripts

> How to run our project from terminal

### 1. Clone from respository
```bash
git clone https://github.com/yourusername/SC2002-FDAD-GRP3-.git
```
```bash
cd SC2002-FDAD-GRP3-/BTO_application
```

### 2. Create a bin folder for compiled files
```bash
mkdir bin
```

### 3. Compile the Java source file
> For MacOS:
```bash
javac -d bin src/**/*.java
```
> For Windows:
```bash
javac -d bin src\auth\*.java src\boundary\*.java src\control\*.java src\entities\*.java src\enums\*.java src\main\*.java src\utils\*.java
```

### 4. Run the application
```bash
java -cp bin main.Main
```
