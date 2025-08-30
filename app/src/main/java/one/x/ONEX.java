package one.x;

import java.util.Locale;
import java.util.Timer;

public class ONEX {
  public static Locale TPLANG = Locale.ENGLISH;
  public static String COUNTRY = "";
  public static String CURRENCY = "";
  public static boolean isNetworkConnected = false;
  public static String PSWD = "", ID = "";
  public static String BASE_URL = "http://127.0.0.1:5555";
  public static String SIGNIN = BASE_URL + "/app/signin";
  public static String SIGNUP = BASE_URL + "/app/signup";
  public static String INFO = BASE_URL + "/app/privacy-terms-info";
  public static String CONTACT = BASE_URL + "/app/contact-one-val";
  public static String FEED = BASE_URL + "/app/feed";
  public static String BALANCE = BASE_URL + "/app/user-last-stock";
  public static String TRANSFER = BASE_URL + "/app/transfer";
  public static String MPC = BASE_URL + "/app/modify-pin-or-password";
  public static String DLTACC = BASE_URL + "/app/delete-user";
  public static String REACC = BASE_URL + "/app/recover-account";
  public static String MSK = BASE_URL + "/app/modify-secret-key";
  public static String TRANSREC = BASE_URL + "/app/transactions-history";
  public static String LATEST_APK = BASE_URL + "/app/download-latest-apk";
  public static String CHECKAPP = BASE_URL + "/app/check-app-version";
  public static String GET_STAT = BASE_URL + "/app/mini-stat";
  public static String MBUY = BASE_URL + "/app/mtopup";
  public static String TBUY = BASE_URL + "/app/ttopup";
  public static String BUY_CAMPAIGN = BASE_URL + "/app/ncamp";
  public static Timer TIMER = new Timer();
  public static Timer TIMER2 = new Timer();
  public static boolean ISCONNECTED = false;
  public static String PINREC = "123456";
}
