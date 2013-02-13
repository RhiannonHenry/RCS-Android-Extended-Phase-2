package org.gsm.rcsApp.activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
		String fileName = (String) intent.getExtras().getString("FILE_NAME");
		String attachmentURL = (String) intent.getExtras().getString("ATTACHMENT_URL");
		Log.d("AsyncTaskDownload", "File Name: " + fileName);
		Log.d("AsyncTaskDownload", "URL: " + attachmentURL);
		task.execute(fileName, attachmentURL);
	}

	private class ShowDialogAsyncTask extends
			AsyncTask<String, Integer, String> {

		int progress_status;

		@Override
		protected void onPreExecute() {
			// update the UI immediately after the task is executed
			super.onPreExecute();
			Toast.makeText(AsyncTaskDownloadActivity.this,
					"Starting download...", Toast.LENGTH_SHORT).show();
			progress_status = 0;
			txt_percentage.setText("downloading 0%");

		}

		@Override
		protected String doInBackground(String... params) {
			//String endpoint = "http://28c51ebaaadfd10114b406e01025adde:O$wyL,4S@hanoi:8181/services/repo/user/ObtainUserFile?username=%2B441110000002&filename=tux1358850990171.png";
			
			String fileName = params[0];
			String attachmentURL = params[1];
			String response = null;
			try {
				String auth = android.util.Base64.encodeToString((SplashActivity.appCredentialUsername+":"+SplashActivity.appCredentialPassword).getBytes("UTF-8"), android.util.Base64.NO_WRAP);
				URL url = new URL(attachmentURL);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setRequestProperty("Authorization", "Basic " + auth);
				//InputStream reader = url.openStream();
				InputStream reader = urlConnection.getInputStream();
				Log.d("AsyncTaskDownload","Connected to URL: " + url.toString());
				int fileSize = getHeaderDetails(attachmentURL);
				File SDCardRoot = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				SDCardRoot.mkdirs();
				Log.d("AsyncTaskDownload",
						"Saving file to path: " + SDCardRoot.getAbsolutePath());
				File file = new File(SDCardRoot, fileName);
				FileOutputStream output = new FileOutputStream(file);

				byte[] data = new byte[1024];

				int count = 0;
				long totalDownloaded = 0;
				while ((count = reader.read(data)) > 0) {
					Log.d("AsyncTaskDownload", "Current count = " + count);
					totalDownloaded += count;
					progress_status = (int) ((totalDownloaded / fileSize) * 100);
					publishProgress(progress_status);
					output.write(data, 0, count);

				}
				Log.d("AsyncTaskDownload", "File Received...");
				response = "Success";
				output.close();
			} catch (MalformedURLException e) {
				response = "Failure";
				Log.e("AsyncTaskDownload",
						"MalformedURLException:: when attempting to download attachment");
				e.printStackTrace();
			} catch (IOException e) {
				response = "Failure";
				Log.e("AsyncTaskDownload",
						"IOException:: when attempting to download attachment");
				e.printStackTrace();
			}
			Log.d("AsyncTaskDownload", "***Response*** = " + response);
			return response;
		}

		private int getHeaderDetails(String endpoint) throws IOException {
			// TODO Auto-generated method stub
			URL url = new URL(endpoint);
			HttpURLConnection yc = (HttpURLConnection) url.openConnection();
			int fileSize = 0;

			try {
				// retrieve file size from Content-Length header field
				if (yc.getContentLength() >0){
					fileSize = yc.getContentLength();
				}else{
					fileSize=1;
				}
				
			} catch (NumberFormatException nfe) {
				Log.e("AsyncTaskDownload", "Error:: error getting the content length");
			}
			Log.d("AsyncTaskDownload", "Content Length: "+fileSize);
			Log.d("AsyncTaskDownload", "Auth: "+yc.getHeaderField("Authorization"));
			return fileSize;

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
