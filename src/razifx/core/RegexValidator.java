package razifx.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RegexValidator.java
 *
 * @since 1.0
 * @see java.util.regex.Matcher
 * @see java.util.regex.Pattern
 */
public class RegexValidator {

    private static Pattern pattern;
    private static Matcher matcher;

    /**
     * Is the email address entered by the user correct? Only gmail addresses
     * approved.
     *
     * @param email user's email address
     * @return true if email is correct
     */
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[gmail]+\\.[a-zA-Z]{2,}$";
        pattern = Pattern.compile(emailRegex);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Whether the user input data is a number or not.
     * 
     * @param str User input data.
     * @return true if the input data is only numbers.
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false; // Or handle empty string differently if needed
        }
        if(str.length()>2 || str.length()<1) {
            return false;
        }
        return str.matches("-?\\d+(\\.\\d+)?");  // Matches integers and decimals (positive and negative)
    }    
    
    /**
     * Whether the user input data is a day or not.
     * 
     * @param str User input data.
     * @return true if the input data is only only 2 or 1 digits.
     */
    public static boolean isNumericDay(String str) {
        if (str == null || str.isEmpty()) {
            return false; // Or handle empty string differently if needed
        }
        if(str.length()>2 || str.length()<1) {
            return false;
        }
        if (str.matches("-?\\d+(\\.\\d+)?")) {
            int day = Integer.parseInt(str);
            if (day>31 || day<1) {
                return false;
            }
        }
        return str.matches("-?\\d+(\\.\\d+)?");  // Matches integers and decimals (positive and negative)
    }
    
    /**
     * Whether the user input data is a year or not.
     * 
     * @param str User input data.
     * @return true if the input data is only 4 digits.
     */
    public static boolean isNumericYear(String str) {
        if (str == null || str.isEmpty()) {
            return false; // Or handle empty string differently if needed
        }
        if(str.length()>4 || str.length()<4) {
            return false;
        }
        if (str.matches("-?\\d+(\\.\\d+)?")) {
            int year = Integer.parseInt(str);
            if (year>1500 || year<1300) {
                return false;
            }
        }
        return str.matches("-?\\d+(\\.\\d+)?");  // Matches integers and decimals (positive and negative)
    }
    
    /**
     * Checks whether the user's password is at least 8 characters long.
     *
     * @param password
     * @return true if password is correct
     */
    public static boolean isValidPassword(String password) {
        if (password.length() >= 8) {
            return true;
        }
        return false;
    }

    /**
     * Whether the value entered by the user is valid as a decimal or not
     *
     * @param amount
     * @return true if amount is correct and true the input data was only
     * numbers and less than or equal to 13 digits.
     */
    public static boolean isValidAmount(String amount) {
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            return false; // if amount is not digits return false
        }

        if (amount.length() > 13) {
            return false;
        }
        String amountRegex = "^\\d*\\d+|\\d+\\d*$";
        Pattern pattern = Pattern.compile(amountRegex);
        Matcher matcher = pattern.matcher(amount);
        return matcher.matches();
    }

    /**
     * Check whether the national ID number entered by the user is correct or
     * not.
     *
     * @param nationalId
     * @return true If the input data was only numbers and less than or equal to
     * 13 digits.
     */
    public static boolean isValidNationalID(String nationalId) {
        return isValidAmount(nationalId);
    }
}
