package utils;

public class CustomExceptions{

	public static class IncorrectUsernameException extends Exception {
	    public IncorrectUsernameException(String message) {
	        super(message);
	    }
	}
	
	public static class IncorrectLoginDetailsException extends Exception {
	    public IncorrectLoginDetailsException(String message) {
	        super(message);
	    }
	}

}