package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import auth.LoginManager;

import java.time.LocalDate;

import control.UserManager;
import control.ApplicantManager;
import control.ApplicationManager;
import control.EnquiryManager;
import control.OfficerRegistrationManager;
import control.ProjectManager;
import control.BookingManager;
import enums.ApplicationStatus;
import enums.OfficerRegistrationStatus;
import enums.RoomType;

import utils.CustomExceptions.*;
//import utils.CustomExceptions.IncorrectUsernameException;

public class Pages{
	
	private final static int WIDTH = 95;
	private final static int HEIGHT = 20;
	private final static String lineSeparator = System.lineSeparator();
	private final static StringBuilder pageBorder = border(WIDTH,HEIGHT,"*");
	
	
	public abstract class Page{
		protected String header = "";
		protected int headerRow = 3;
		protected int headerCol = 3;
		protected String data = "";
		protected int dataRow = 6;
		protected int dataCol = 3;
		protected String options = "";
		protected int optionsRow = HEIGHT - 6;
		protected int optionsCol = 3;
		protected String errorMsg = "";
		protected int errorMsgRow = HEIGHT- 3;
		protected int errorMsgCol = 3;
		
		Page(String header, String data, String options, String errorMsg){
			this.header = header;
			this.data = data;
			this.options = options;
			this.errorMsg = errorMsg;
		}
		
		protected String getHeader() {
			return header;
		}

		protected void setHeader(String header) {
			this.header = header;
		}
		
		protected void setHeaderPos(int headerRow, int headerCol) {
			this.headerRow = headerRow;
			this.headerCol = headerCol;
		}
		
		protected void setHeader(String header, int headerRow, int headerCol) {
			this.header = header;
			setHeaderPos(headerRow,headerCol);
		}

		protected String getData() {
			return data;
		}

		protected void setData(String data) {
			this.data = data;
		}
		
		protected void setDataPos(int dataRow, int dataCol) {
			this.dataRow = dataRow;
			this.dataCol = dataCol;
		}
		
		protected void setData(String data, int dataRow, int dataCol) {
			this.data = data;
			setDataPos(dataRow,dataCol);
		}

		protected String getOptions() {
			return options;
		}
		
		protected void setOptions(String options) {
			this.options = options;
		}
		
		protected void setOptionsPos(int optionsRow, int optionsCol) {
			this.optionsRow = optionsRow ;
			this.optionsCol = optionsCol ;
		}
		
		protected void setOptions(String options, int optionsRow, int optionsCol) {
			this.options = options;
			setOptionsPos(optionsRow,optionsCol);
		}
		
		protected String getErrorMsg() {
			return errorMsg;
		}

		protected void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}
		
		protected void setErrorMsgPos(int errorMsgRow, int errorMsgCol) {
			this.errorMsgRow = errorMsgRow ;
			this.errorMsgCol = errorMsgCol ;
		}
		
		protected void setErrorMsg(String errorMsg, int errorMsgRow, int errorMsgCol) {
			this.errorMsg = errorMsg;
			setErrorMsgPos(errorMsgRow,errorMsgCol);
		}
		
		public abstract User run(Scanner scanner);

		public void display() {
			clearScreen();
			System.out.print(pageBorder);
			moveCursorTo(headerRow,headerCol);
			System.out.print(header);
			moveCursorTo(dataRow, dataCol);
			System.out.print(data);
			moveCursorTo(optionsRow, optionsCol);
			System.out.print(options);
			moveCursorTo(errorMsgRow, errorMsgCol);
			System.out.print(colour("red",errorMsg));
			moveCursorTo(HEIGHT+2,0);
		}
		
		public void refresh() {
			clearScreen();
			this.loadData();
			this.display();
		}
		
		public void back(Page previous) {
			//previous.loadData();
			clearScreen();
			//previous.display();
		}
		
		public String input(Scanner sc) {
			String userInput = "";
			boolean validatedInput = true;
			while (validatedInput) {
				userInput = sc.nextLine().trim();
				if (!this.verifyInput(userInput)) {
					System.out.println("Your selection is invalid. "); // figure it how to handle it internally and cleanly
				} else {
					validatedInput = false;
				}
			}
			return userInput;
		}
		
		protected boolean verifyInput(String userInput) { // overriden and refined
			return true;
		}
		
		protected abstract void loadData(); //this one is overriden and refined
	}
	
	public class LoginPage extends Page{
		
		private LoginManager loginManager; //Based on the stage it will decide which screen to show and what messages. This is because login page is unique.
		private User authenticatedUser = null;
		
		public LoginPage(LoginManager loginManager){
			super("BTO MANAGEMENT SYSTEM LOGIN","NRIC: ","Password: ",""); // Persistent information
			this.loginManager = loginManager;
			this.loadData(); // First time setup
			}
		
		public User run(Scanner sc) {
			this.display();
			moveCursorTo(this.dataRow, this.dataCol + data.length());
			String inputNRIC = sc.nextLine();
			moveCursorTo(this.optionsRow, this.optionsCol + options.length());
			String inputPassword = sc.nextLine();
			moveCursorTo(HEIGHT+2,0);
			
			try {
				authenticatedUser = loginManager.login(inputNRIC,inputPassword);
			} catch (Exception e) {
				loadData(e);
			}
			return authenticatedUser;
		}
		
		protected void loadData() {
			this.setHeaderPos(3, midIndex(this.getHeader().length(),WIDTH));
			this.setDataPos(8,27);
			this.setOptionsPos(10,23);
		}
		
		
		private void loadData(Exception e) {
			String errMsg = "";
			if (e instanceof IncorrectUsernameException) {
				errMsg = "Invalid NRIC format. Please try again (e.g., S1234567A).";
			}
			if (e instanceof IncorrectLoginDetailsException) {
				errMsg = "Login Unsuccessful. Invalid NRIC or Password.";
			}
			this.setErrorMsg(errMsg,16, midIndex(errMsg.length(),WIDTH));
		}
		
		
	}
	
	
	private static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
	
	private static int midIndex(int strlen, int width) {
		return width/2 - strlen/2;
	}
	
	private static void moveCursorTo(int row, int col) {
		String ansiCommand = String.format("\033[" + row + ";" + col + "H");
		System.out.print(ansiCommand);
	}
	
	private static String colour(String col, String str) {
        String RESET = "\u001B[0m";
        String RED = "\u001B[31m";
        String GREEN = "\u001B[32m";
        String YELLOW = "\u001B[33m";
        
		switch (col){
			case "red":
				return RED + str + RESET;
			case "green":
				return GREEN + str + RESET;
			case "yellow":
				return YELLOW + str + RESET;
			default:
				return str;
		}
	}
	
	private static StringBuilder border(int width,int height, String borderChar) {
		final String horizontalBorder = borderChar.repeat(width) + lineSeparator;
		final String verticalBorder = borderChar + " ".repeat(width - 2) + borderChar + lineSeparator;
		
		StringBuilder sb = new StringBuilder(width);
		sb.append(horizontalBorder);
		for (int i = 0 ; i <= height-2; i++) {
			sb.append(verticalBorder);
		}
		sb.append(horizontalBorder);
		return sb;
	}
	
	
}