package com.asendi;

import java.util.Locale;
import java.util.Timer;

public class ASENDI {
	public static Locale TPLANG = Locale.ENGLISH;
	public static boolean isNetworkConnected = false;
	public static String PSWD = "", ID = "";
	public static String BASE_URL = "http://127.0.0.1:5555";
	public static String SIGNIN = BASE_URL + "/app/signin";
	public static String SIGNUP = BASE_URL + "/app/signup";
	public static String INFO = BASE_URL + "/app/info";
	public static String CONTACT = BASE_URL + "/app/contact";
	public static String FEED = BASE_URL + "/app/feed";
	public static String BALANCE = BASE_URL + "/app/balance";
	public static String TRANSFER = BASE_URL + "/app/transfer";
	public static String MPC = BASE_URL + "/app/mpc";
	public static String DLTACC = BASE_URL + "/app/dltacc";
	public static String REACC = BASE_URL + "/app/reacc";
	public static String MSK = BASE_URL + "/app/msk";
	public static String TRANSREC = BASE_URL + "/app/transrec";
	public static String LATEST_APK = BASE_URL + "/latest/apk";
	public static String CHECKAPP = BASE_URL + "/checkapp";
	public static Timer TIMER = new Timer();
	public static Timer TIMER2 = new Timer();
	public static boolean ISCONNECTED = false;
	public static String PINREC = "123456";
	
}