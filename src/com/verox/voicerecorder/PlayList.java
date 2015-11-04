package com.verox.voicerecorder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.verox.voicerecorder.R.id;

public class PlayList extends Activity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {

	ImageButton back_btn;
	ListView playlist;
	Context context = null;
	String[] titles, dates, sizes, durations;
	Button play_pause_btn, stop_btn;
	File[] file = null;
	String path = null;
	String SelectedSongPath = null;
	int audioIndex, audioIndexLongPressed;
	//MediaPlayer
	Animation animation;
	AnimationSet set;
	LayoutAnimationController controller;
	private Handler mHandler = new Handler();;

	Utility utils;
	TextView total_time, current_time, songTitle;
	SeekBar audioProgressBar;
	int media_length;

	private MediaPlayer mPlayer;
	Typeface light, medium, thin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.playlist);
		context = getApplicationContext();

		Log.d("onCreate", "onCreate");

		utils = new Utility();

		playlist = (ListView) findViewById(R.id.playlist);

		set = new AnimationSet(true);

		animation = new AlphaAnimation(0.0f, 1.0f); 
		animation.setDuration(400);
		set.addAnimation(animation);

		animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
				);
		animation.setDuration(17);
		set.addAnimation(animation);
		controller = new LayoutAnimationController(set, 0.4f);
		//		playlist.setLayoutAnimation(controller);
		registerForContextMenu(playlist);

		light = Typeface.createFromAsset(getAssets(),"fonts/roboto-light.ttf"); 
		medium = Typeface.createFromAsset(getAssets(),"fonts/roboto-medium.ttf");
		thin = Typeface.createFromAsset(getAssets(),"fonts/roboto-thin.ttf");

		current_time = (TextView) findViewById(R.id.current_time);
		current_time.setTypeface(light);
		total_time = (TextView) findViewById(R.id.total_time);
		total_time.setTypeface(light);
		songTitle = (TextView) findViewById(R.id.songTitle);
		songTitle.setTypeface(medium);

		setListItems();

		playlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				if (mPlayer.isPlaying()) {
					mHandler.removeCallbacks(mUpdateTimeTask);
					mPlayer.pause();
					mPlayer.seekTo(0);
					mPlayer.stop();
					audioProgressBar.setProgress(0);
					Log.d("file stopped: ", file[audioIndex].getName());
					current_time.setText("00:00:00");
					total_time.setText("00:00:00");
					play_pause_btn.setBackgroundResource(R.drawable.record_player_play);
				}

				audioIndex = position;
				songTitle.setText(file[audioIndex].getName().toString());

				mPlayer.reset();
				playSong(audioIndex);
				play_pause_btn.setBackgroundResource(R.drawable.record_player_play_pressed);
				audioProgressBar.setEnabled(true);
				Log.d("playing a new file: ", file[audioIndex].getName());

			}
		});

		playlist.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				audioIndexLongPressed = position;

				if (mPlayer.isPlaying()) {
					mHandler.removeCallbacks(mUpdateTimeTask);
					mPlayer.pause();
					mPlayer.seekTo(0);
					mPlayer.stop();
					audioProgressBar.setProgress(0);
					Log.d("file stopped: ", file[audioIndex].getName());
					current_time.setText("00:00:00");
					total_time.setText("00:00:00");
					play_pause_btn.setBackgroundResource(R.drawable.record_player_play);
					audioProgressBar.setEnabled(false);
					//					mVisualizerView.clearRenderers();
				}

				// TODO Auto-generated method stub

				return false;
			}

		});

		play_pause_btn = (Button) findViewById(id.play_pause_btn);
		play_pause_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d("audioIndex", Integer.toString(audioIndex));
				if(mPlayer.isPlaying()){
					mHandler.removeCallbacks(mUpdateTimeTask);
					mPlayer.pause();
					media_length = mPlayer.getCurrentPosition();
					Log.d("paused at: ", Integer.toString(media_length));
					// Changing button image to play button
					play_pause_btn.setBackgroundResource(R.drawable.record_player_play);
					audioProgressBar.setEnabled(false);

				} else if (!mPlayer.isPlaying() && audioProgressBar.getProgress() != 0){
					Log.d("resumed at: ", Integer.toString(media_length));
					mPlayer.seekTo(media_length);
					mHandler.postDelayed(mUpdateTimeTask, 100);
					mPlayer.start();
					play_pause_btn.setBackgroundResource(R.drawable.record_player_play_pressed);
					audioProgressBar.setEnabled(true);

				} else if (file.length != 0) {
					mPlayer.reset();
					playSong(audioIndex);
					play_pause_btn.setBackgroundResource(R.drawable.record_player_play_pressed);
					audioProgressBar.setEnabled(true);
					Log.d("playing a new file: ", file[audioIndex].getName());

				} else {
					Toast.makeText(getApplicationContext(), "No audio files found", Toast.LENGTH_SHORT).show();
				}

			}
		});

		stop_btn  = (Button) findViewById(R.id.stop_btn);
		stop_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (mPlayer.isPlaying()) {
					mHandler.removeCallbacks(mUpdateTimeTask);
					mPlayer.seekTo(0);
					mPlayer.stop();
					audioProgressBar.setProgress(0);
					Log.d("file stopped: ", file[audioIndex].getName());
					current_time.setText("00:00:00");
					total_time.setText("00:00:00");
					play_pause_btn.setBackgroundResource(R.drawable.record_player_play);
					audioProgressBar.setEnabled(false);
					//				mVisualizerView.clearRenderers();
				} else if (!mPlayer.isPlaying() && audioProgressBar.getProgress() > 0){
					mHandler.removeCallbacks(mUpdateTimeTask);
					mPlayer.seekTo(0);
					mPlayer.stop();
					audioProgressBar.setProgress(0);
					Log.d("file stopped: ", file[audioIndex].getName());
					current_time.setText("00:00:00");
					total_time.setText("00:00:00");
					play_pause_btn.setBackgroundResource(R.drawable.record_player_play);
					audioProgressBar.setEnabled(false);
				}

			}
		});

		back_btn = (ImageButton) findViewById(R.id.back_btn);
		back_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent back = new Intent(PlayList.this, MainActivity.class);
				startActivity(back);
				overridePendingTransition(R.drawable.rotate_in,R.drawable.rotate_out);
				finish();

			}
		});

		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(this);

		audioProgressBar = (SeekBar) findViewById(R.id.audioProgressBar);
		audioProgressBar.setOnSeekBarChangeListener(this);
		audioProgressBar.setEnabled(false);

		//		mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);

		//		initTunnelPlayerWorkaround();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Options");
		if (v.getId() == R.id.playlist) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.playlist_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.d("AdapterContextMenuInfo: ", info.toString());

		switch(item.getItemId()) {
		//		case R.id.optn_shre:
		//
		//			Log.d("optn_shre", "optn_shre");
		//			return true;

		case R.id.optn_rename:

			Log.d("optn_rename", "optn_rename");
			getUserInput();
			return true;

		case R.id.optn_delete:

			Log.d("optn_delete", "optn_delete");
			deleteFile();

			if (file.length > 0) {
				audioIndex = file.length-1;
				songTitle.setText(file[audioIndex].getName().toString());
			}
			return true;

		default:

			return super.onContextItemSelected(item);
		}

	}

	private void setListItems() {

		titles = null;
		dates = null;
		sizes = null;
		durations = null;

		path = Environment.getExternalStorageDirectory().toString()+"/Easy voice recordings";
		Log.d("Files", "Path: " + path);
		File f = new File(path);        
		file = f.listFiles();
		Log.d("Files", "Size: "+ file.length);

		Arrays.sort(file, new Comparator<File>(){
			public int compare(File f1, File f2)
			{
				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			} });

		if (file.length > 0) {

			titles = new String[file.length];
			dates = new String[file.length];
			sizes = new String[file.length];
			durations = new String[file.length];

			for (int i=0; i < file.length; i++)
			{

				try {
					//Name
					titles[i] = file[i].getName().toString();

					//Size
					sizes[i] = String.format("%.2f", Math.ceil(file[i].length())/1024);

					//Duration
					// load data file
					MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
					metaRetriever.setDataSource(path + "/" + file[i].getName());

					// convert duration to minute:seconds
					String duration =  
							metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
					Log.d("duration", "duration: " + duration);
					long dur = Long.parseLong(duration);

					int secs = (int) (dur / 1000) % 60 ;
					int mins = (int) ((dur / (1000*60)) % 60);
					int hour = (int) ((dur / (1000*60*60)) % 24);

					String seconds = String.format("%1$02d", secs);
					String minutes = String.format("%1$02d", mins);
					String hours = String.format("%1$02d", hour);

					// close object
					metaRetriever.release();

					durations[i] = hours + ":" + minutes + ":" + seconds;

					// Date
					Date lastModDate = new Date(file[i].lastModified());
					dates[i] = Integer.toString(lastModDate.getDate()) + "/";
					dates[i] = dates[i] + Integer.toString(lastModDate.getMonth()) + "/";
					dates[i] = dates[i] + Integer.toString(lastModDate.getYear());

					Log.d("Files", "FileName: " + titles[i].toUpperCase());
					Log.d("Files", "FileSize: " + sizes[i]);
					Log.d("Files", "FileDuration: " + durations[i]);
					Log.d("Files", "FileDate: " + dates[i]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			playlist.setLayoutAnimation(controller);

			AdapterPlaylist apl = new AdapterPlaylist(context, titles, sizes, durations, dates);
			playlist.setAdapter(apl);

			// set first track in the list
			audioIndex = 0;
			songTitle.setText(file[audioIndex].getName().toString());

		}

	}

	private void deleteFile() {
		file[audioIndexLongPressed].delete();
		Toast.makeText(getApplicationContext(), "File " + file[audioIndexLongPressed].getName() + " has been deleted", 
				Toast.LENGTH_LONG).show();
		setListItems();
	}

	private void getUserInput() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				PlayList.this);

		alertDialogBuilder.setTitle("Enter name of the file");
		final EditText input = new EditText(PlayList.this);
		input.setText(file[audioIndexLongPressed].getName().toString());
		input.setSelection(input.getText().length());

		alertDialogBuilder.setView(input);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

		// set dialog message
		alertDialogBuilder
		.setCancelable(true)
		.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// get user input and set it to result

				String file_name = input.getText().toString().trim();

				String filePath = Environment.getExternalStorageDirectory().toString()+"/Easy voice recordings";
				File dir = new File(filePath); 
				if(dir.exists()){
					File from = new File(dir,file[audioIndexLongPressed].getName().toString());
					File to = new File(dir,file_name+".3gpp");
					if(from.exists())
						from.renameTo(to);
					setListItems();
				}

			}
		})
		.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {

				InputMethodManager imm = (InputMethodManager)getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}

	/**
	 * Function to play a song
	 * @param audioIndex - index of song
	 * */
	public void playSong(int audioIndex){
		// Play song
		try {

			SelectedSongPath = path + "/" + file[audioIndex].getName();
			Log.d("playing audio file: ", file[audioIndex].getName());

			try {
				mPlayer.setDataSource(SelectedSongPath);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mHandler.postDelayed(mUpdateTimeTask, 100);
			mPlayer.prepare();
			mPlayer.start();
			audioProgressBar.setEnabled(true);
			//			Updating progress bar
			updateProgressBar();

			//			mVisualizerView.link(mPlayer);

			// Start with just line renderer
			//			addCircleBarRenderer();

			// Displaying Song title
			//            String songTitle = songsList.get(audioIndex).get("songTitle");
			//            songTitleLabel.setText(songTitle);

			// Changing Button Image to pause image
			//            btnPlay.setImageResource(R.drawable.btn_pause);

			// set Progress bar values
			//            songProgressBar.setProgress(0);
			//            songProgressBar.setMax(100);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/**
	 * Background Runnable thread
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {

			long totalDuration = mPlayer.getDuration();
			long currentDuration = mPlayer.getCurrentPosition();

			//			Log.d("mUpdateTimeTask: ", Integer.toString(mPlayer.getDuration()));

			// Displaying Total Duration time
			total_time.setText(""+utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			current_time.setText(""+utils.milliSecondsToTimer(currentDuration));

			// Updating progress bar
			int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
			//Log.d("Progress", ""+progress);
			audioProgressBar.setProgress(progress);

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();

		Intent back = new Intent(PlayList.this, MainActivity.class);
		startActivity(back);
		overridePendingTransition(R.drawable.rotate_in,R.drawable.rotate_out);
		finish();
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		// TODO Auto-generated method stub

		mHandler.removeCallbacks(mUpdateTimeTask);
		Log.d("Completion Listener - Song Complete: ", file[audioIndex].getName());
		mPlayer.seekTo(0);
		audioProgressBar.setProgress(0);
		current_time.setText("00:00:00");
		total_time.setText("00:00:00");
		play_pause_btn.setBackgroundResource(R.drawable.record_player_play);
		audioProgressBar.setEnabled(false);
		//		mVisualizerView.clearRenderers();

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		mHandler.removeCallbacks(mUpdateTimeTask);
		if (mPlayer.isPlaying()) {
			audioProgressBar.setEnabled(true);
		} else {
			audioProgressBar.setEnabled(false);
		}

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		mHandler.removeCallbacks(mUpdateTimeTask);
		if (audioProgressBar.isEnabled()) {
			int totalDuration = mPlayer.getDuration();
			int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
			Log.d("currentPosition", Integer.toString(currentPosition));

			// forward or backward to certain seconds
			mPlayer.seekTo(currentPosition);

			// update timer progress again
			updateProgressBar();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		//		mPlayer = new MediaPlayer();
		audioProgressBar.setEnabled(true);
		super.onResume();
		Log.d("onResume", "onResume");

	}

	private void cleanUp()
	{
		if (mPlayer != null)
		{
			//			mVisualizerView.release();
			mPlayer.release();
			mPlayer = null;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mHandler.removeCallbacks(mUpdateTimeTask);

		if (mPlayer.isPlaying()) {
			mPlayer.pause();
			media_length = mPlayer.getCurrentPosition();
			Log.d("paused at: ", Integer.toString(media_length));
			// Changing button image to play button
			play_pause_btn.setBackgroundResource(R.drawable.record_player_play_pressed);
			audioProgressBar.setEnabled(false);
		}
		super.onPause();
	}

	@Override
	public void onDestroy(){
		cleanUp();
		super.onDestroy();
	}

}
