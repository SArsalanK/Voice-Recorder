package com.verox.voicerecorder;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends Activity {

	private ToggleButton recorder_btn, record_finish_btn;
	private Boolean isRecording = false;
	private MediaRecorder myRecorder;
	private TextView notifier_txt;
	private int n = 1;
	String file_name, root;
	File file, myDir;
	SharedPreferences sharedPreferences;
	Button playlist_btn, cancel_btn;
	boolean isCancelled = false;
	int count = 0;

	//Timer
	private TextView recorder_timer, recorder_timer_milllisecs;
	private long startTime = 0L;
	private Handler customHandler = new Handler();
	long timeInMilliseconds = 0L;
	long timeSwapBuff = 0L;
	long updatedTime = 0L;
	
	Typeface light, medium;

	private AdView mAdView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		light = Typeface.createFromAsset(getAssets(),"fonts/roboto-light.ttf");
		medium = Typeface.createFromAsset(getAssets(),"fonts/roboto-medium.ttf"); 
		
		notifier_txt = (TextView) findViewById(R.id.notifier_txt);
		notifier_txt.setTypeface(light);
		recorder_btn = (ToggleButton) findViewById(R.id.recorder_btn);

		root = Environment.getExternalStorageDirectory().getAbsolutePath();
		myDir = new File(root + "/Easy voice recordings");    
		if (!myDir.exists()) {
			myDir.mkdirs();
			savePreferences("FileNum", 1);
		}

		loadSavedPreferences();

		recorder_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					boolean on = ((ToggleButton) v).isChecked();
					if (on) {

						myRecorder = new MediaRecorder();
						myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
						myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.DEFAULT);

						Log.d("Files", "myDir: " + myDir);

						file = new File (myDir, file_name + ".3gpp");
						myRecorder.setOutputFile(file.toString());

						record_finish_btn.setChecked(true);
						isRecording = true;
						start();

					} else {
						
						record_finish_btn.setChecked(false);
						isRecording = false;
						stop();
						
						Intent gotoPlaylist = new Intent(MainActivity.this, PlayList.class);
						startActivity(gotoPlaylist);
						//Set the transition -> method available from Android 2.0 and beyond  
						overridePendingTransition(R.drawable.rotate_in,R.drawable.rotate_out);
						finish();
					}
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		playlist_btn = (Button) findViewById(R.id.playlist_btn);
		playlist_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (isRecording) {
					record_finish_btn.performClick();
				}

				Intent gotoPlaylist = new Intent(MainActivity.this, PlayList.class);
				startActivity(gotoPlaylist);
				//Set the transition -> method available from Android 2.0 and beyond  
				overridePendingTransition(R.drawable.rotate_in,R.drawable.rotate_out);
				finish();
			}
		});

		record_finish_btn = (ToggleButton) findViewById(R.id.record_finish_btn);
		record_finish_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				recorder_btn.performClick();
			}
		});

		cancel_btn = (Button) findViewById(R.id.cancel_btn);
		cancel_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isRecording) {
					isCancelled = true;
					stop();
					recorder_btn.setChecked(false);
				}
			}
		});
		
		recorder_timer = (TextView) findViewById(R.id.recorder_timer);
		recorder_timer_milllisecs = (TextView) findViewById(R.id.recorder_timer_milllisecs);
		recorder_timer.setTypeface(light);
		recorder_timer_milllisecs.setTypeface(light);
		
		mAdView = (AdView) findViewById(R.id.adView);
		mAdView.setAdListener(new ToastAdListener(this));
		mAdView.loadAd(new AdRequest.Builder().build());
	}

	public void start(){
		try {
			recorder_timer.setText("00:00:00");
			recorder_timer_milllisecs.setText(":00");
			myRecorder.prepare();
			myRecorder.start();

			startTime = SystemClock.uptimeMillis();
			customHandler.postDelayed(updateTimerThread, 0);

			isRecording = true;
		} catch (IllegalStateException e) {
			// start:it is called before prepare()
			// prepare: it is called after start() or before setOutputFormat() 
			e.printStackTrace();
		} catch (IOException e) {
			// prepare() fails
			e.printStackTrace();
		}

		notifier_txt.setText("Recording..");

		Toast.makeText(getApplicationContext(), "Recording..", 
				Toast.LENGTH_LONG).show();
	}

	public void stop() {
		try {

			myRecorder.stop();
			customHandler.removeCallbacks(updateTimerThread);
			myRecorder.release();
			isRecording = false;

			notifier_txt.setText("Recording saved");

			if (isCancelled) {
				isCancelled = false;
				file.delete();
				recorder_timer.setText("00:00:00");
				recorder_timer_milllisecs.setText(":00");
				notifier_txt.setText("Recording discarded");
				Toast.makeText(getApplicationContext(), "Recording has been discarded", 
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), "File '"+ file_name +"' has been saved to " + "storage/Easy voice recordings", 
						Toast.LENGTH_LONG).show();
				int getInt = sharedPreferences.getInt("FileNum", n);
				getInt = getInt + 1;
				savePreferences("FileNum", getInt);
				file_name = "recording" + Integer.toString(sharedPreferences.getInt("FileNum", getInt));
			}

		} catch (IllegalStateException e) {
			//  it is called before start()
			e.printStackTrace();
		} catch (RuntimeException e) {
			// no valid audio/video data has been received
			e.printStackTrace();
		}
	}

	private void savePreferences(String key, Integer value) {
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	private void loadSavedPreferences() {
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		file_name = "recording" + Integer.toString(sharedPreferences.getInt("FileNum", n));
	}

	private Runnable updateTimerThread = new Runnable() {

		public void run() {

			timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			updatedTime = timeSwapBuff + timeInMilliseconds;

			int secs = (int) (updatedTime / 1000);
			int mins = secs / 60;
			int hours = mins / 60;
			secs = secs % 60;
			int milliseconds = (int) (updatedTime % 1000)/10;

			recorder_timer.setText("" + String.format("%02d", hours) + ":" + "" + String.format("%02d", mins) + ":"
					+ String.format("%02d", secs));
			recorder_timer_milllisecs.setText(":" + String.format("%02d", milliseconds));
			//			+ String.format("%02d", milliseconds))

			customHandler.postDelayed(this, 0);
		}

	};

	private void startTimer() {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// Do something after 5s = 5000ms
				count = 0;
			}
		}, 4000);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mAdView.pause();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mAdView.resume();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mAdView.destroy();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		if (count == 0) {
			Toast.makeText(getApplicationContext(), "Press again to exit", Toast.LENGTH_SHORT).show();
			count = count + 1;
			startTimer();
		} else {

			try {
				if (isRecording == true) {
					myRecorder.stop();
					customHandler.removeCallbacks(updateTimerThread);
					myRecorder.release();
					isRecording = false;
				}
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			super.onBackPressed();
		}
	}

}
