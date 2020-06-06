package com.amazonaws.lambda.demo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler extends Constants implements RequestHandler<Object, String> {
	static Context context;
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    JSONObject json;
    @Override
    public String handleRequest(Object input, Context ctx) {
    	context=ctx;
        context.getLogger().log("Input"+input);
        try {
       	 json=new JSONObject(""+input);
       	 return manageQuestions(json).toString(); 
        }catch(JSONException e) {
       	 e.printStackTrace();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	   return "Invalied Request";
    }

  //This method calls appropriate API method 
    private Object manageQuestions(JSONObject input) throws Exception {
		if(input==null) {
			return error;
		}else {
			try {
				switch(input.getString("name")) {
				case "login" : return login(input.getString(AdminName),input.getString(password));
				case "createQuestion": return createQuestion(input.getString(qText),input.getString(option1),
						input.getString(option2),input.getString(option3),input.getString(option4),
						input.getNumber(correctanswer),input.getString(audioclipurl),input.getNumber(questionBankID));
				case "readAllQuestions" : return readAllQuestions();
				case "readQuestion": return readQuestion(input.getNumber(questionID));
				case "deleteQuestion" :return deleteQuestion(input.getNumber(questionID));
				case "updateQuestion":return updateQuestion(input.getString(qText), input.getString(option1), input.getString(option2), input.getString(option3),
						input.getString(option4),input.getNumber(correctanswer),input.getString(audioclipurl),input.getNumber(questionBankID),input.getNumber(questionID));
			
				
				case "deleteQuestionBank":return deleteQuestionBank(input.getNumber(questionBankID));
				case "updateQuestionBank":return updateQuestionBank(input.getNumber(questionBankID), input.getString(questionBankName));
				case "createQuestionBank" : return createQuestionBank(input.getString(questionBankName));
				case "readQuestionBank" : return readQuestionBank();
				default:return error;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		return error;
	}
    
//This method authenticates the Admin   
    private boolean login(String AName, String pwd) throws Exception{
   	 Table table = dynamoDB.getTable("Admin");
   	 try{ 
   		 Item item=table.getItem(AdminName,AName);
   		 String pswd=item.getString(password);
   		 if(pswd.equals(pwd)) {
   			 return true;
   		 }
       	 }catch(Exception e) {
       			e.printStackTrace();
       	 }
   	   	return false;
   }

    
//This method creates a new question in a specified QuestionBank
	private boolean createQuestion(String qt, String op1, String op2, String op3, String op4, Number ca,
			String vurl, Number qbID) throws Exception{
		
		 Table table = dynamoDB.getTable(QuestionsTable);
	     try{ 
	    	 
	    	 ArrayList<String> options=new ArrayList<>();
	    	 options.add(op1);
	    	 options.add(op2);
	    	 options.add(op3);
	    	 options.add(op4);
	    	 Date today = new Date();
			 DateFormat df = new SimpleDateFormat("MMMM dd yyyy");
			 df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
			 String created = df.format(today);
			
	    	 Item item = new Item()
	    			    .withPrimaryKey(questionID,increment(1))
	    			    .withString(qText, qt)
	    			    .withList("options",options)
	    			    .withNumber(correctanswer,ca)
	    			    .withString(audioclipurl,vurl)
	    			    .withString("created", created)
	    			    .withString("lastUpdated", created)
	    			    .withNumber(questionBankID,qbID);
	    			    
	    	             
	    	  table.putItem(item);
	    	  context.getLogger().log("true");
	    	  return true;
	    	 }catch(Exception e) {	 
			      e.printStackTrace();
		     }
		 return false;
	}

//This method scans all the questions and returns JASONArray of Items in the Questions table
    private Object readAllQuestions() throws Exception{
    	Table table = dynamoDB.getTable(QuestionsTable);
    	ScanSpec scanSpec = new ScanSpec().withProjectionExpression(
    			"audioclipurl,correctanswer,created,lastupdated,options,qText,questionBankID,questionID");
    	JSONArray ja = new JSONArray();
            try {
                ItemCollection<ScanOutcome> items = table.scan(scanSpec);
                for(Item item:items) {
                	ja.put(item);
                } 

            }
            catch (Exception e) {
            	context.getLogger().log("Unable to scan the table:");
                System.err.println(e.getMessage());
                return error;
            }
            return ja;
    }

//This method returns an Item based on questionID given by Admin from Questions Table
    private Object readQuestion(Number qID) throws Exception{
    	Table table=dynamoDB.getTable(QuestionsTable);
		try {
			Item item=table.getItem(questionID,qID);
			
			context.getLogger().log(item.toJSONPretty());
			return new JSONObject(""+item.toJSONPretty());
		}catch (JSONException e) {
			context.getLogger().log("getItem failed");
			context.getLogger().log(e.getMessage());
		}		
		return error;
	}

//This method updates question attributes which are accordingly given by Admin in Question table
    private boolean updateQuestion(String qt, String op1, String op2, String op3,
    		 String op4, Number ca,String aurl, Number qbID,Number qID) throws Exception{
    	     
    		 Table table = dynamoDB.getTable(QuestionsTable);
    		 
    		 Item i = table.getItem(questionID,qID);
    		 if(i==null) {
    			 return false;
    		 }
    		 
    		 ArrayList<String> options=new ArrayList<>();
    		 options.add(op1);
    		 options.add(op2);
    		 options.add(op3);
    		 options.add(op4);
    		 Date today = new Date();
    		 DateFormat df = new SimpleDateFormat("MMMM dd yyyy");
    		 df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    		 String lu = df.format(today);
    		 UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(questionID, qID)
    			         .withUpdateExpression("set audioclipurl= :aurl, correctanswer=:ca, lastUpdated=:lu , options=:opt , questionBankID=:qbid , qText=:qtext")
    			         .withValueMap(new ValueMap().withNumber(":qbid", qbID).withString(":qtext",qt).withString(":lu", lu).withNumber(":ca", ca).withString(":aurl", aurl)
    			         .withList(":opt",options))
    			         .withReturnValues(ReturnValue.UPDATED_NEW);
    		 
    		  try {
    	           UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
    	           context.getLogger().log("UpdateItem succeeded:\n" + outcome.getItem().toJSONPretty());
    	           return true;
    	        }
    	        catch (Exception e) {
    	           e.printStackTrace();
    	        }
    		return false;
    	}

//This method deletes a question record based on the questionID given by Admin in Questions table
	private boolean deleteQuestion(Number qID) throws Exception{
		Table table = dynamoDB.getTable(QuestionsTable);
		
		Item i = table.getItem(questionID,qID);
		 if(i==null) {
			 return false;
		 }
		
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(questionID,qID);
		try {
            table.deleteItem(deleteItemSpec);
            context.getLogger().log("deleted successfully");
            return true;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

//This method creates a new question record in Questions table
    private Object createQuestionBank(String qbName) throws Exception{
    	Table table = dynamoDB.getTable(QuestionBankTable);
    	Item itm=getItemHelper(questionBankName);
      	 if(itm!=null) {
      		JSONObject alert=new JSONObject("{\"Message\":\"Question Bank Already Exists\"}");
      		 return alert;
      	 }
	     try{  
	    	 Date today = new Date();
			 DateFormat df = new SimpleDateFormat("MMMM dd yyyy");
			 df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
			 String created = df.format(today);
			 
	    	 Item item = new Item()
	    			    .withPrimaryKey(questionBankID,increment(2))
	    			    .withString("created", created)
	    			    .withString("lastUpdated", created)
	    			    .withString(questionBankName,qbName)
	    			    .withNumber("questionCount", 0);
	    			               
	    	 table.putItem(item);
	    	 context.getLogger().log("true");
	    	 return true;
	    	 }catch(Exception e) {	 
			      e.printStackTrace();
		     }
		return error;
	}
    
    private Item getItemHelper(String qbName) throws Exception{
    	Table table = dynamoDB.getTable(QuestionBankTable);
    	Index index = table.getIndex("questionBankName-index");
    	QuerySpec spec = new QuerySpec()
   			    .withKeyConditionExpression("#nm = :questionBankName")
   			    .withNameMap(new NameMap()
   			        .with("#nm", "questionBankName"))
   			    .withValueMap(new ValueMap()
   			        .withString(":questionBankName",qbName));
   	 ItemCollection<QueryOutcome> items = index.query(spec);
	   	 for(Item i:items) {
	   		 return i;
	   	 }
	   	 return null;
	 }

//This method returns a list of QuestionBank Items from QuestionBank table
    private Object readQuestionBank() throws Exception{
    	Table table = dynamoDB.getTable(QuestionBankTable);
    	ScanSpec scanSpec = new ScanSpec().withProjectionExpression(
    			"created,lastUpdated,questionBankID,questionBankName,questionCount");
    	JSONArray ja = new JSONArray();
            try {
                ItemCollection<ScanOutcome> items = table.scan(scanSpec);
                for(Item item:items) {
                	ja.put(item);
                } 

            }
            catch (Exception e) {
                context.getLogger().log("Unable to scan the table:");
                System.err.println(e.getMessage());
                return error;
            }
            return ja;
    }

//This method updates the questionBankName in QuestionBank table 
    private boolean updateQuestionBank(Number qbID,String qbName) throws Exception{
		Table table = dynamoDB.getTable(QuestionBankTable);
		Item i = table.getItem(questionBankID,qbID);
		 if(i==null) {
			 return false;
		 }
		
		 Date today = new Date();
		 DateFormat df = new SimpleDateFormat("MMMM dd yyyy");
		 df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
		 String lu = df.format(today);
		 UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(questionBankID, qbID)
			        .withUpdateExpression("set questionBankName= :qbname,lastUpdated=:lu")
			        .withValueMap(new ValueMap().withString(":qbname",qbName).withString(":lu", lu))
			        .withReturnValues(ReturnValue.UPDATED_NEW);
		try {
			 UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
			 context.getLogger().log("UpdateItem succeeded:\n" + outcome.getItem().toJSONPretty());
			 return true;
	    }catch (Exception e) {
	         e.printStackTrace();
	    }
		return false;
	}
 
//This method deletes a questionBank record from QuestionBank table
	private boolean deleteQuestionBank(Number qbID) throws Exception{
		Table table = dynamoDB.getTable(QuestionBankTable);
		
		Item i = table.getItem(questionBankID,qbID);
		 if(i==null) {
			 return false;
		 }
		
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
	            .withPrimaryKey(questionBankID,qbID);
		try {
            table.deleteItem(deleteItemSpec);
            context.getLogger().log("deleted successfully");
            return true;
		
		}catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return false;
	}

//This method is used to autoIncrement the counter of questionCounter and questionBankCounter in AutoIncrement table
	private Number increment(int check) throws Exception{
		Table table1 = dynamoDB.getTable("AutoIncrement");
		 Item itm = table1.getItem("counterID",1);
		if(check == 1) {
			 
			 Number num=itm.getNumber("questionCount");
			 long nl=num.longValue();
			 Number nll=nl+1;
			 context.getLogger().log(""+itm.toJSONPretty()); 

    		
			 UpdateItemSpec updateItemSpec = new UpdateItemSpec()
    				    .withPrimaryKey("counterID", 1)
    			        .withUpdateExpression("set questionCount= :qc")
    			        .withValueMap(new ValueMap().withNumber(":qc", nll))
    			        .withReturnValues(ReturnValue.UPDATED_NEW);
    		 
    		 
    	     try {
				table1.updateItem(updateItemSpec);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	     return nll;
		}
		 Number num=itm.getNumber("questionBankCount");
		 long nl=num.longValue();
		 Number nll=nl+1;
		 context.getLogger().log(""+itm.toJSONPretty()); 

		
		 UpdateItemSpec updateItemSpec = new UpdateItemSpec()
				    .withPrimaryKey("counterID", 1)
			        .withUpdateExpression("set questionBankCount= :qbc")
			        .withValueMap(new ValueMap().withNumber(":qbc", nll))
			        .withReturnValues(ReturnValue.UPDATED_NEW);
		 
		 
	     try {
			table1.updateItem(updateItemSpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	     return nll;
	}

}
//QBAdmin
//"{\"name\":\"createQuestion\",\"qText\":\"How ?\",\"option1\":\"a\",\"option2\":\"b\",\"option3\":\"c\",\"option4\":\"d\",\"correctanswer\":3,\"audioclipurl\":\"https:\",\"questionBankID\":5}"
//"{\"name\":\"readAllQuestions\"}"
//"{\"name\":\"readQuestion\",\"questionID\":99}"
//"{\"name\":\"createQuestionBank\",\"questionBankName\":\"default\"}"
//"{\"name\":\"readQuestionBank\"}"

//"{\"name\":\"deleteQuestion\",\"questionID\":\99\}"
//"{\"name\":\"deleteQuestionBank\",\"questionBankID\":1}"
//"{\"name\":\"updateQuestionBank\",\"questionBankID\":1,\"questionBankName\":\"TeluguSongs\"}"	
//"{\"name\":\"updateQuestion\",\"qText\":\"GuessWhichWhyWhat???\",\"option1\":\"op1\",\"option2\":\"op2\",\"option3\":\"op3\",\"option4\":\"op4\",\"correctanswer\":1,\"audioclipurl\":\"http:\",\"questionBankID\":1,\"questionID\":1}"
//"{\"name\":\"login\",\"AdminName\":\"Shreya\",\"password\":\"715d562072947b517ffe270fa382cdb90e781129846780687d39f308953f2cb5bd605dec692e8164c51b230d28975af51fe0cd433163e889c7bb95ce7558ec65\"}"	

