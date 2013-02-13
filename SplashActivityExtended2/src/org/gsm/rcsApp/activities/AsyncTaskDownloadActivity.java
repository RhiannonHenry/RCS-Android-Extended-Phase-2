package org.gsm.rcsApp.activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.gsm.RCSDemo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AsyncTaskDownloadActivity extends Activity {

	Button btn_start;
	ProgressBar progressBar;
	TextView txt_percentage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_async_task);
		progressBar = (ProgressBar) findViewById(R.id.progress);
		txt_percentage = (TextView) findViewById(R.id.txt_percentage);
		ShowDialogAsyncTask task = new ShowDialogAsyncTask();
		Intent intent = getIntent();
		String params = (String)intent.getExtras().getString("FILE_NAME");
		Log.d("AsyncTaskDownload", "File Name: "+params);
		task.execute(params);
	}

	private class ShowDialogAsyncTask extends AsyncTask<String, Integer, String> {

		int progress_status;

		@Override
		protected void onPreExecute() {
			// update the UI immediately after the task is executed
			super.onPreExecute();
			Toast.makeText(AsyncTaskDownloadActivity.this, "Starting download...",
					Toast.LENGTH_SHORT).show();
			progress_status = 0;
			txt_percentage.setText("downloading 0%");

		}

		@Override
		protected String doInBackground(String ...params) {
			String fileName = params[0];
			String response = null;
			int count = 0;
			try {
				URL url = new URL("http://28c51ebaaadfd10114b406e01025adde:O$wyL,4S@hanoi:8181/services/repo/user/ObtainUserFile?username=%2B441110000002&filename=tux1358850990171.png");
				URLConnection urlConnection = url.openConnection();
				urlConnection.connect();
				Log.d("AsyncTaskDownload", "Connected to URL: "+url.toString());
				int length = urlConnection.getContentLength();
				Log.d("AsyncTaskDownload","File size = "+length);
				Log.d("AsyncTaskDownload","Content Type = "+urlConnection.getContentType());
				Log.d("AsyncTaskDownload","Authentication = "+urlConnection.getHeaderField("Authorization"));
				// download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
				File SDCardRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				SDCardRoot.mkdirs();
				Log.d("AsyncTaskDownload","Saving file to path: "+SDCardRoot.getAbsolutePath());
				File file = new File(SDCardRoot,fileName);
				FileOutputStream output = new FileOutputStream(file);
				
				byte[] data = new byte[1024];
		        long total = 0;
		        
		        
		        while ( (count = input.read(data)) != -1) {  
		                total += count;
		                progress_status = (int)((total*100)/length);
                		publishProgress(progress_status);
		                output.write(data, 0, count);
		                
		        }
		        Log.d("AsyncTaskDownload","File Received...");
		        response =  "Success";
		        output.close();
			} catch (MalformedURLException e) {
				response =  "Failure";
		        Log.e("AsyncTaskDownload", "MalformedURLException:: when attempting to download attachment");
				e.printStackTrace();
			} catch (IOException e) {
				response =  "Failure";
				 Log.e("AsyncTaskDownload", "IOException:: when attempting to download attachment");
			     e.printStackTrace();   
			}
			Log.d("AsyncTaskDownload","***Response*** = "+response);
			return response;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			progressBar.setProgress(values[0]);
			txt_percentage.setText("downloading " + values[0] + "%");

		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(AsyncTaskDownloadActivity.this, "Downloaded",
					Toast.LENGTH_SHORT).show();
			txt_percentage.setText("download complete");
			finish();
		}
	}
}
