package com.sogeti.mci.mailsend.helper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/* Requires the creation of the following table:
 CREATE TABLE mailsender_logs (
 		  id int NOT NULL AUTO_INCREMENT,
		  date DATETIME DEFAULT NULL,
		  level varchar(10) DEFAULT NULL,
		  hash varchar(20) DEFAULT NULL,
		  message text,
		  stacktrace text,
		  PRIMARY KEY (`id`)
		); */

@Service
@Scope(value = "singleton")
public class LoggerDAO {
	
	private boolean logToLocalFile = true;
	
	private static final Logger log = Logger.getLogger(LoggerDAO.class.getName());
	
	private final static String QUERY_INSERTLOG = "INSERT INTO mailsender_logs (date, level, hash, message, stacktrace) VALUES (?,?,?,?,?)";

	/**
	 * The datasource onto MYSQL Database
	 */
	//private BasicDataSource ds = null;
	
	/**
	 * The URL to connect to MySQL Database
	 */
	private String url;
		
	/**
	 * The username for connecting to MySQL Database
	 */
	private String username;
	
	/**
	 * The password for connecting to MySQL Database
	 */
	private String password;
	
	/**
	 * The driver for connecting to MySQL Database
	 */
	private String driver;
	
	/**
	 * The log level
	 */
	private String level = null;
	
	public void init()  {
		
		try {
			Properties prop = new Properties();
			prop.load(this.getClass().getResourceAsStream("/config-db.properties"));
			driver 		= prop.getProperty("jdbc.driver");
			username 	= prop.getProperty("jdbc.username");
			password 	= prop.getProperty("jdbc.password");
			url  		= prop.getProperty("jdbc.url");
			level  		= prop.getProperty("log.level");
			
			Class.forName("com.mysql.jdbc.GoogleDriver");
			
		} catch (Exception e){
			log.severe(getStackTrace(e));
			//if (logToLocalFile) System.out.println(getStackTrace(e));
		}
	    /*ds = new BasicDataSource();
		ds.setDriverClassName(driver);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setUrl(url);*/
		log.info("Logger initialized with level " + level);
	
		//if (logToLocalFile) System.out.println("Logger initialized with level: " + level);
	}
	
	public void debug(String hash, String message, Exception e){
		if (level == null) init();
		if (level.equals("DEBUG")){
			String stacktrace = "";
			if (e != null){
				stacktrace = getStackTrace(e);
			} 
			logToDB("DEBUG", hash, message, stacktrace);
		}
	}

	public void debug(String hash, String message){
		if (level == null) init();
		if (level.equals("DEBUG")){
			logToDB("DEBUG", hash, message, "");
		}
	}

	public void info(String hash, String message, Exception e){
		if (level == null) init();
		if (level.equals("INFO") || level.equals("DEBUG")){
			String stacktrace = "";
			if (e != null){
				stacktrace = getStackTrace(e);
			} 
			logToDB("INFO", hash, message, stacktrace);
		}
	}

	public void info(String hash, String message){
		if (level == null) init();
		if (level.equals("INFO") || level.equals("DEBUG")){
			logToDB("INFO", hash, message, "");
		}
	}
	
	public void error(String hash, String message, Exception e){
		if (level == null) init();
	    String stacktrace = "";
		if (e != null){
			stacktrace = getStackTrace(e);
		} 
		logToDB("ERROR", hash, message, stacktrace);
	}
	
	public void error(String hash, String message){
		if (level == null) init();
		logToDB("ERROR", hash, message, "");
	}
	
	/**
	 * inserts the log into MySQL
	 * 
	 */
	private void logToDB(String level, String hash, String message, String stacktrace ) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("Europe/Berlin"); 
		sdf.setTimeZone(tz);
		Connection conn = null;
		PreparedStatement ps = null;
		/*if (ds == null) {
			init();
		}
		if (ds != null) {*/
			try {
				//log.info("About to ask for connection");
				conn = DriverManager.getConnection(url, username, password);
				//log.info("About to execute SQL request");
				//conn = ds.getConnection();
				ps = conn.prepareStatement(QUERY_INSERTLOG);
				ps.setString(1, sdf.format(new Date()));
				ps.setString(2, level);
				ps.setString(3, hash);
				ps.setString(4, message);
				ps.setString(5, stacktrace);
				int result = ps.executeUpdate();
				//log.info("log request sent with result: " + result + "\n" + message + "\n" + stacktrace);
			} catch (Exception e){
				//if (logToLocalFile) System.out.println(getStackTrace(e));
				log.severe(getStackTrace(e));
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e){
						log.severe(getStackTrace(e));
						//if (logToLocalFile) System.out.println(getStackTrace(e));
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e){
						log.severe(getStackTrace(e));
						//if (logToLocalFile) System.out.println(getStackTrace(e));
					}
				}
			}
		}
	//}
	
	/**
	 * Returns the stack trace as string
	 * @param the exception
	 * @return the stack trace as string
	 */
	private String getStackTrace(Exception e){
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}
}
