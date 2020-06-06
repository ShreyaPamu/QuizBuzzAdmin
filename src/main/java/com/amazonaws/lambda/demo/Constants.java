package com.amazonaws.lambda.demo;

import org.json.JSONObject;

public class Constants { 
	public static JSONObject error=new JSONObject("{\"error\":\"Invalied request\"}");
	public static String QuestionsTable = "Questions";
	public static String QuestionBankTable = "QuestionBank";
	public static String name="name";
	public static String AdminName="AdminName";
	public static String password="password";
	public static String qText="qText";
	public static String option1="option1";
	public static String option2="option2";
	public static String option3="option3";
	public static String option4="option4";
	public static String correctanswer = "correctanswer";
	public static String audioclipurl ="audioclipurl";
	public static String questionBankID = "questionBankID";
	public static String questionID = "questionID";
	public static String questionBankName = "questionBankName";
}
