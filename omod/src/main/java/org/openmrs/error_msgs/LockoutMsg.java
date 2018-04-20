package org.openmrs.error_msgs;

public class LockoutMsg {
private final static String Lockout_Msg = " <div id=\"openmrs_error\"> too many failled logins, yourelocked out, try again later </div>";

public static String getLockoutMsg() {
	return Lockout_Msg;
}

}
