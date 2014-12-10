package com.crontasks;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class QuotesDailyTweeter {

	//david.hilbert.de@gmail.com @davidhilbert_de
	//wise.man.quoting@gmail.com @wiseman_quoting

	//SELECT ... WHERE to_char(contact.dob, 'MMDD') between '0615' and '0630'...
	//SELECT * from  MATHS_BIO where to_char(MATHS_BIO.birth_date, 'MM') = '12' and to_char(MATHS_BIO.birth_date, 'DD') = '19'
	private static String random_quotes_selecting_string ="select * from QUOTES order by random() limit 1";
	private static Connection con;
	private static String  credential_path = "/home/sduprey/My_Data/My_Twitter_Conf/twitter4j_wiseman.properties";

	public static void main(String[] args){
		//		Properties props = new Properties();
		//		FileInputStream in = null;      
		//		try {
		//			in = new FileInputStream("database.properties");
		//			props.load(in);
		//		} catch (IOException ex) {
		//			Logger lgr = Logger.getLogger(BenchmarkingController.class.getName());
		//			lgr.log(Level.FATAL, ex.getMessage(), ex);
		//		} finally {
		//			try {
		//				if (in != null) {
		//					in.close();
		//				}
		//			} catch (IOException ex) {
		//				Logger lgr = Logger.getLogger(BenchmarkingController.class.getName());
		//				lgr.log(Level.FATAL, ex.getMessage(), ex);
		//			}
		//		}
		// the following properties have been identified
		//		String url = props.getProperty("db.url");
		//		String user = props.getProperty("db.user");
		//		String passwd = props.getProperty("db.passwd");
		String url="jdbc:postgresql://localhost/QUOTESDB";
		String user="postgres";
		String passwd="mogette";
		String quote = "";
		try{
			con = DriverManager.getConnection(url, user, passwd);
			quote=fetching_today_quote();

		} catch (Exception e){
			System.out.println("Error instantiating either database or solr server");
			e.printStackTrace();
		}

		if (!"".equals(quote)){
			System.out.println("Sending quote to twitter : "+quote);
			sendTwit(quote);
		}
	}

	public static void sendTwit(String quote){

		Properties props = new Properties();
		FileInputStream in = null;      
		try {
			in = new FileInputStream(credential_path);
			props.load(in);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		String consumerKeystr = props.getProperty("oauth.consumerKey");
		String consumerSecretstr = props.getProperty("oauth.consumerSecret");
		String accessTokenstr = props.getProperty("oauth.accessToken");
		String accessTokenSecretstr = props.getProperty("oauth.accessTokenSecret");

		try {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKeystr)
			.setOAuthConsumerSecret(consumerSecretstr)
			.setOAuthAccessToken(accessTokenstr)
			.setOAuthAccessTokenSecret(accessTokenSecretstr);

			TwitterFactory twitterFactory = new TwitterFactory(cb.build());
			Twitter twitter = twitterFactory.getInstance();
			Status status = twitter.updateStatus(quote);
			System.out.println("Successfully updated the status to [" + status.getText() + "].");
			System.exit(0);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to get timeline: " + te.getMessage());
			System.exit(-1);
		} 
	}

	public static String fetching_today_quote() throws SQLException{
		String quote = "";
		System.out.println("Getting randomly a quote");
		PreparedStatement pst = con.prepareStatement(random_quotes_selecting_string);

		ResultSet rs = pst.executeQuery();
		if (rs.next()) {
			quote = rs.getString(1);

		}
		pst.close();
		return quote;
	}

}
