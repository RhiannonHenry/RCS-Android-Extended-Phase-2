package org.gsm.rcsApp.activities;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.entity.StringEntity;
import org.gsm.RCSDemo.R;
import org.gsm.rcsApp.ServiceURL;
import org.gsm.rcsApp.misc.RCSJsonHttpResponseHandler;
import org.gsm.rcsApp.misc.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SplashActivity extends Activity {

	public static String userId=null;
	
	public static final String appCredentialUsername="9254711da4ce54ee165923c7a17b6068";
	public static final String appCredentialPassword="d/kX)6M8";
	
	static SplashActivity _instance=null;
	
	public static String notificationChannelURL=null;
	public static String notificationChannelResourceURL=null;
	
	public static ArrayList<String> notificationSubscriptions=new ArrayList<String>();  
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        _instance=this;
    }
    
    public void onStart() {
		super.onStart();
	
		final TextView splashStatusIndicator=(TextView) findViewById(R.id.splashStatusIndicator);
		splashStatusIndicator.setVisibility(View.VISIBLE);
		splashStatusIndicator.setText("enter username (mobile number)");		

        AsyncHttpClient client = new AsyncHttpClient();

		if (userId!=null) {
			/*
			 * De-register the previously logged in user
			 */

	        String auth;
			try {
				auth = android.util.Base64.encodeToString((SplashActivity.appCredentialUsername+":"+SplashActivity.appCredentialPassword).getBytes("UTF-8"), android.util.Base64.NO_WRAP);
				client.addHeader("Authorization", "Basic "+ auth);
				client.addHeader("Accept", "application/json");
			} catch (UnsupportedEncodingException e) {
				Log.e("SplashActivity","Authorization Failed: Failed to encode in Base64");
			}
	        
	        final String url=ServiceURL.unregisterURL(userId);
	        
	        Log.d("SplashActivity", "Unregistering user");
	        
	        client.delete(url, new RCSJsonHttpResponseHandler() {
	        	@Override
				public void onSuccess(String response, int statusCode) {
	        		Log.d("SplashActivity", "unregister::success status="+statusCode);
				}
	        });
		
			/*
			 * Clear previous notification subscriptions  
			 */
			if (notificationSubscriptions.size()>0) {
				for (final String durl:notificationSubscriptions) {
			        client.delete(durl, new RCSJsonHttpResponseHandler() {
			        	@Override
						public void onSuccess(String response, int statusCode) {
							Log.d("SplashActivity", "deleted subscription status="+statusCode+" response="+response);
						}
			        });
				}
				notificationSubscriptions.clear();
			}
			if (notificationChannelResourceURL!=null) {
				final String durl=notificationChannelResourceURL;
		        client.delete(durl, new RCSJsonHttpResponseHandler() {
		        	@Override
					public void onSuccess(String response, int statusCode) {
						Log.d("SplashActivity", "deleted notification channel status="+statusCode+" response="+response);
					}
		        });
		        notificationChannelResourceURL=null;
			}
	        userId=null;
		}
		
		MainActivity.stopMainActivity();
    }
    
    public void proceedToMain(View view) throws UnsupportedEncodingException {
    	EditText splashUsernameInput=(EditText) findViewById(R.id.splashUsernameInput);
		
		final TextView splashStatusIndicator=(TextView) findViewById(R.id.splashStatusIndicator);
 
		final String username=splashUsernameInput.getText().toString();
		
		splashStatusIndicator.setVisibility(View.INVISIBLE);
		
		if (username!=null && username.trim().length()>0) {
  
	        AsyncHttpClient client = new AsyncHttpClient();
	        String auth = android.util.Base64.encodeToString((SplashActivity.appCredentialUsername+":"+SplashActivity.appCredentialPassword).getBytes("UTF-8"), android.util.Base64.NO_WRAP);
	        final String url = ServiceURL.registerURL(username);
	        Log.d("SplashActivity","Username = "+SplashActivity.appCredentialUsername);
	        Log.d("SplashActivity","Password = "+SplashActivity.appCredentialPassword);
	        Log.d("SplashActivity","URL = "+url);
	        
	        client.addHeader("Authorization", "Basic "+ auth);
	        client.addHeader("Accept", "application/json");
			client.addHeader("Content-Type", "application/json");
	        client.post(url, new RCSJsonHttpResponseHandler() {
		        boolean successReceived=false;

	        	@Override
	            public void onSuccess(String response, int responseCode) throws UnsupportedEncodingException {
	        		Log.d("SplashActivity", "proceedToMain::success status="+responseCode);
	                if (responseCode==204){
						userId=username;
						registerForNotifications();
						Intent intent = new Intent(_instance, MainActivity.class);
						successReceived=true;
						startActivity(intent);
					} else if (responseCode==401) {
		    			splashStatusIndicator.setVisibility(View.VISIBLE);
		    			splashStatusIndicator.setText("invalid username (mobile number)");			            	
		            	successReceived=true;
	                }
	            }
	
				@Override
	            public void onStart() {
	                // Initiated the request
	    			splashStatusIndicator.setVisibility(View.VISIBLE);
	    			splashStatusIndicator.setText("sending login request");		
	            }
	        
	            @Override
	            public void onFailure(Throwable e, String response) {
	                // Response failed :(
	    			splashStatusIndicator.setVisibility(View.VISIBLE);
	    			splashStatusIndicator.setText("login request failed");
	    			System.out.println("Response "+response);
	    			System.out.println(e.toString());
	            }
	
	            @Override
	            public void onFinish() {
	                // Completed the request (either success or failure)
	            	if (!successReceived) {
		    			splashStatusIndicator.setVisibility(View.VISIBLE);
		    			splashStatusIndicator.setText("login request finished - unknown failure");
	            	}
	            }
	        });
		} else {
			splashStatusIndicator.setVisibility(View.VISIBLE);
			splashStatusIndicator.setText("enter username (mobile number)");		
		}

    }
    
    private void registerForNotifications() throws UnsupportedEncodingException {
        AsyncHttpClient client = new AsyncHttpClient();
        String auth = android.util.Base64.encodeToString((SplashActivity.appCredentialUsername+":"+SplashActivity.appCredentialPassword).getBytes("UTF-8"), android.util.Base64.NO_WRAP);
        final String url=ServiceURL.createNotificationChannelURL(userId);
        String jsonData="{\"notificationChannel\": {\"applicationTag\": \"myApp\", \"channelData\": {\"maxNotifications\": \"3\", \"type\": \"LongPollingData\"}, \"channelLifetime\": \"20\", \"channelType\": \"LongPolling\", \"clientCorrelator\": \"123\"}}";
        Log.d("SplashActivity", "register for notifications: URL = "+url);
        
        try {
			StringEntity requestData=new StringEntity(jsonData);
			Log.d("SplashActivity", "register for notifications: sending post request...");
			client.addHeader("Authorization", "Basic "+ auth);
			client.addHeader("Accept", "application/json");
	        client.post(_instance.getApplication().getApplicationContext(),
	        		url, requestData, "application/json", new RCSJsonHttpResponseHandler() {
	        	@Override
	            public void onSuccess(JSONObject response, int statusCode) throws UnsupportedEncodingException {
	        		Log.d("SplashActivity", "registerForNotifications::success = "+response.toString()+" statusCode="+statusCode);
	        		
	        		if (statusCode==201) {
	        			JSONObject notificationChannel=Utils.getJSONObject(response, "notificationChannel");
	        			String callbackURL=Utils.getJSONStringElement(notificationChannel, "callbackURL");
	        			notificationChannelResourceURL=Utils.getJSONStringElement(notificationChannel, "resourceURL");
	        			JSONObject channelData=Utils.getJSONObject(notificationChannel, "channelData");
	        			notificationChannelURL=channelData!=null?Utils.getJSONStringElement(channelData, "channelURL"):null;
	        			Log.d("SplashActivity", "callbackURL = "+callbackURL);
	        			Log.d("SplashActivity", "resourceURL = "+notificationChannelResourceURL);
	        			Log.d("SplashActivity", "channelURL = "+notificationChannelURL);
	        			
	        			Log.i("SplashActivity", "Subscribing User to Address Book Notifications...");
	        			subscribeToAddressBookNotifications(callbackURL);
	        			Log.i("SplashActivity", "Subscribing User to Session Notifications...");
	        			subscribeToSessionNotifications(callbackURL);
	        			Log.i("SplashActivity", "Subscribing User to Chat Notifications...");
	        			subscribeToChatNotifications(callbackURL);
	        			Log.i("SplashActivity", "Subscribing User to File Transfer Notifications...");
	        			subscribeToFileTransferNotifications(callbackURL);
	        		}else{
	        			Log.e("SplashActivity","Create Notification Channel Error: HTTP "+statusCode);
	        		}
	        	}
	        	
	        	@Override
    			public void onFailure(Throwable error, String content,
    					int responseCode) {
    				super.onFailure(error, content, responseCode);
    				Log.e("SplashActivity", "Response Code = "+responseCode);
    				Log.e("SplashActivity", "Error Creating Notification Channel", error);
    			}

	        });
		} catch (UnsupportedEncodingException e) { }

	}

    private void subscribeToFileTransferNotifications(String callbackURL) throws UnsupportedEncodingException{
    	try{
    		JSONObject fileTransferSubscription=new JSONObject();
			JSONObject callbackReference=new JSONObject();
			callbackReference.put("callbackData", userId);
			callbackReference.put("notifyURL", callbackURL);
			fileTransferSubscription.put("callbackReference", callbackReference);
			fileTransferSubscription.put("duration", (int) 0);
			String jsonData="{\"fileTransferSubscription\":"+fileTransferSubscription.toString()+"}";
			Log.d("SplashActivity", "Subscription request data = "+jsonData);
			
			AsyncHttpClient client = new AsyncHttpClient();
			String auth = android.util.Base64.encodeToString((SplashActivity.appCredentialUsername+":"+SplashActivity.appCredentialPassword).getBytes("UTF-8"), android.util.Base64.NO_WRAP);
			client.addHeader("Authorization", "Basic "+ auth);
			client.addHeader("Accept", "application/json");
			final String url = ServiceURL.createFileTransferSubscriptionURL(userId);
	        
	        try {
				StringEntity requestData=new StringEntity(jsonData);
		        
		        client.post(_instance.getApplication().getApplicationContext(),
		        		url, requestData, "application/json", new RCSJsonHttpResponseHandler() {
		        	@Override
		            public void onSuccess(JSONObject response, int statusCode) {
		        		Log.d("SplashActivity", "subscribeToFileTransferNotifications::success = "+response.toString()+" statusCode="+statusCode);
		        		if (statusCode==201) {
			        		String resourceURL=Utils.getResourceURL(Utils.getJSONObject(response, "fileTransferSubscription"));
			        		if (resourceURL!=null) notificationSubscriptions.add(resourceURL);
		        		}else{
		        			Log.e("SplashActivity", "subscribeToFileTransferNotifications::failure = "+response.toString()+" statusCode="+statusCode);
		        		}
		        	}
		        	@Override
		        	public void onFailure(Throwable error, String content, int responseCode){
		        		Log.e("SplashActivity", "subscribeToFileTransferNotifications::failure = "+error+" statusCode="+responseCode);
		        	}
		        });
			} catch (UnsupportedEncodingException e) { }	        
    	}catch(JSONException e){
    		
    	}
    }
    
	private void subscribeToAddressBookNotifications(String callbackURL) throws UnsupportedEncodingException {
		try {
			JSONObject abChangesSubscription=new JSONObject();
			JSONObject callbackReference=new JSONObject();
			callbackReference.put("callbackData", userId);
			callbackReference.put("notifyURL", callbackURL);
			abChangesSubscription.put("callbackReference", callbackReference);
			abChangesSubscription.put("duration", (int) 0);
			String jsonData="{\"abChangesSubscription\":"+abChangesSubscription.toString()+"}";
			Log.d("SplashActivity", "Subscription request data = "+jsonData);
			
	        AsyncHttpClient client = new AsyncHttpClient();
	        String auth = android.util.Base64.encodeToString((SplashActivity.appCredentialUsername+":"+SplashActivity.appCredentialPassword).getBytes("UTF-8"), android.util.Base64.NO_WRAP);
			client.addHeader("Authorization", "Basic "+ auth);
			client.addHeader("Accept", "application/json");
			
	        final String url=ServiceURL.createAddressBookChangeSubscriptionURL(userId);
	        try {
				StringEntity requestData=new StringEntity(jsonData);
		        
		        client.post(_instance.getApplication().getApplicationContext(),
		        		url, requestData, "application/json", new RCSJsonHttpResponseHandler() {
		        	@Override
		            public void onSuccess(JSONObject response, int statusCode) {
		        		Log.d("SplashActivity", "subscribeToAddressBookNotifications::success = "+response.toString()+" statusCode="+statusCode);
		        		if (statusCode==201) {
			        		String resourceURL=Utils.getResourceURL(Utils.getJSONObject(response, "abChangesSubscription"));
			        		if (resourceURL!=null) notificationSubscriptions.add(resourceURL);
		        		}
		        	}
		        });
			} catch (UnsupportedEncodingException e) { }

		} catch (JSONException e) {
		}
		
	}

	private void subscribeToSessionNotifications(String callbackURL) throws UnsupportedEncodingException {
		try {
			JSONObject sessionSubscription=new JSONObject();
			JSONObject callbackReference=new JSONObject();
			callbackReference.put("callbackData", userId);
			callbackReference.put("notifyURL", callbackURL);
			sessionSubscription.put("callbackReference", callbackReference);
			sessionSubscription.put("duration", (int) 0);
			String jsonData="{\"sessionSubscription\":"+sessionSubscription.toString()+"}";
			Log.d("SplashActivity", "Subscription request data = "+jsonData);
			
	        AsyncHttpClient client = new AsyncHttpClient();
	        String auth = android.util.Base64.encodeToString((SplashActivity.appCredentialUsername+":"+SplashActivity.appCredentialPassword).getBytes("UTF-8"), android.util.Base64.NO_WRAP);
			client.addHeader("Authorization", "Basic "+ auth);
			client.addHeader("Accept", "application/json");
			
	        final String url=ServiceURL.createSessionChangeSubscriptionURL(userId);
	        try {
				StringEntity requestData=new StringEntity(jsonData);
		        
		        client.post(_instance.getApplication().getApplicationContext(),
		        		url, requestData, "application/json", new RCSJsonHttpResponseHandler() {
		        	@Override
		            public void onSuccess(JSONObject response, int statusCode) {
		        		Log.d("SplashActivity", "subscribeToSessionNotifications::success = "+response.toString()+" statusCode="+statusCode);
		        		if (statusCode==201) {
			        		String resourceURL=Utils.getResourceURL(Utils.getJSONObject(response, "sessionSubscription"));
			        		if (resourceURL!=null) notificationSubscriptions.add(resourceURL);
		        		}
		        	}
		        });
			} catch (UnsupportedEncodingException e) { }

		} catch (JSONException e) {
		}
		
	}

	private void subscribeToChatNotifications(String callbackURL) throws UnsupportedEncodingException {
		try {
			JSONObject chatSubscription=new JSONObject();
			JSONObject callbackReference=new JSONObject();
			callbackReference.put("callbackData", userId);
			callbackReference.put("notifyURL", callbackURL);
			chatSubscription.put("callbackReference", callbackReference);
			chatSubscription.put("duration", (int) 0);
			String jsonData="{\"chatNotificationSubscription\":"+chatSubscription.toString()+"}";
			Log.d("SplashActivity", "Subscription request data = "+jsonData);
			
	        AsyncHttpClient client = new AsyncHttpClient();
//	        AuthScope authscope=new AuthScope(ServiceURL.serverName, ServiceURL.serverPort, AuthScope.ANY_REALM);
//	        client.setBasicAuth(SplashActivity.userId, SplashActivity.appCredentialPassword, authscope);        
	        String auth = android.util.Base64.encodeToString((SplashActivity.appCredentialUsername+":"+SplashActivity.appCredentialPassword).getBytes("UTF-8"), android.util.Base64.NO_WRAP);
			client.addHeader("Authorization", "Basic "+ auth);
			
	        final String url=ServiceURL.createChatSubscriptionURL(userId);
	        try {
				StringEntity requestData=new StringEntity(jsonData);
		        
		        client.post(_instance.getApplication().getApplicationContext(),
		        		url, requestData, "application/json", new RCSJsonHttpResponseHandler() {
		        	@Override
		            public void onSuccess(JSONObject response, int statusCode) {
		        		Log.d("SplashActivity", "subscribeToChatNotifications::success = "+response.toString()+" statusCode="+statusCode);
		        		if (statusCode==201) {
			        		String resourceURL=Utils.getResourceURL(Utils.getJSONObject(response, "chatNotificationSubscription"));
			        		if (resourceURL!=null) notificationSubscriptions.add(resourceURL);
		        		}
		        	}
		        });
			} catch (UnsupportedEncodingException e) { }

		} catch (JSONException e) {
		}
		
	}
	
	
	
}
