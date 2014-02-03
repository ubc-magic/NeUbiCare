package com.example.audiotowotkit;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.badlogic.gdx.audio.analysis.FFT;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ToggleButton;


public class AudioToWotKitActivity extends Activity {

	

	private static final int[] BAND_INDEXES = {2, 4, 7, 12, 24, 47, 93, 186, 372, 514};
	
	ToggleButton gatheringDataToggle = null;
	Button groundTruthSelecterButton = null;
	ProgressBar volumeBar = null;
	
	AudioToWotKitAsyncTask atwAsyncTask;
	ArrayAdapter<String> dataAdapter;
	
	ACTIVITY groundTruth = ACTIVITY.INVALID;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_to_wotkit);
        gatheringDataToggle = (ToggleButton)findViewById(R.id.gatheringDataToggle);
        volumeBar = (ProgressBar)findViewById(R.id.volumeBar);
        groundTruthSelecterButton = (Button)findViewById(R.id.groundTruthSelecterButton);
        groundTruthSelecterButton.setText(ACTIVITY.getDescription(ACTIVITY.INVALID));
        
      //create dropdown with the ACTIVITY enum values
        List<String> activityList = new ArrayList<String>();
        
        for(ACTIVITY a : ACTIVITY.values()){
			activityList.add(ACTIVITY.getDescription(a));
        }
        
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, activityList);;
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_audio_to_wotkit, menu);
        return true;
    }
    
public void groundTruthSelecterButtonListener(View w) {
	new AlertDialog.Builder(this)
	.setTitle("Select the current activity level")
	.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {

			// TODO: user specific action
			groundTruth = ACTIVITY.fromIndex(which);
			groundTruthSelecterButton.setText(ACTIVITY.getDescription(groundTruth));
			if(atwAsyncTask != null){
				atwAsyncTask.cancel(true);
				atwAsyncTask = new AudioToWotKitAsyncTask();
	    		atwAsyncTask.execute(groundTruth);
			}
			dialog.dismiss();
		}
	}).create().show();
}	

    public void gatheringDataToggleListener(View v){
    	//AudioContextAsyncTask aCAsyncTask = new AudioContextAsyncTask(this);
    	if(gatheringDataToggle.isChecked()){
    		atwAsyncTask = new AudioToWotKitAsyncTask();
    		atwAsyncTask.execute(groundTruth);
    	} else{
    		if(atwAsyncTask != null){
    			atwAsyncTask.cancel(true);
    			atwAsyncTask = null;
    		}
    	}
    }
    
    public static void addEnergyBands(short[] data, double[] energyBands, double[] tempEnergyBands, float[] fftIn, float[] fftOut, FFT fft){
		//do FFT
		for(int i = 0; i < data.length; ++i){ //convert to floats
			fftIn[i] = (float)data[i];
		}
		fft.forward(fftIn); //do FFT
		fftOut = fft.getSpectrum(); //get FFT result
		int currentBand = 0;
		
		for(int i = 0; i < fftOut.length; ++i){ //add FFT results to the correct energy band
			if(i >= BAND_INDEXES[currentBand]){
				++currentBand;
			}
			energyBands[currentBand] += fftOut[i];
		}
		
		/* THIS BLOCK INCLUDES NORMALIZATION
		for(int i = 0; i < fftOut.length; ++i){ //add FFT results to the correct energy band
			if(i >= BAND_INDEXES[currentBand]){
				++currentBand;
			}
			tempEnergyBands[currentBand] += fftOut[i];
		}
		double totalEnergy = 0;
		for(int i = 0; i < energyBands.length; ++i){
			totalEnergy += tempEnergyBands[i];
		}
		for(int i = 0; i < energyBands.length; ++i){
			energyBands[i] += tempEnergyBands[i] / totalEnergy;
		}
		*/
		
		
	}
    
    public static void read1sAudio(double[] energyBands, int windowsPerSecond, AudioRecord recorder, short[] data, double[] tempEnergyBands, float[] fftIn, float[] fftOut, FFT fft){
    	for(int i = 0; i < energyBands.length; ++i){ //clear energy band array, which is summed over following the second
			energyBands[i] = 0;
		}
		for(int i = 0; i < windowsPerSecond; ++i){
			recorder.read(data, 0, CONST.WINDOW_SIZE.val); //read window
    		addEnergyBands(data, energyBands, tempEnergyBands, fftIn, fftOut, fft);
		}
		for(int i = 0; i < energyBands.length; ++i){ //normalize result
			energyBands[i] /= windowsPerSecond;
		}
    }
    
    private void gatheringData(boolean gathering){
    	if(gathering){
    		//TODO: display gathering
    		Log.v("AudioToWotKit", "gathering data started");
    	} else{
    		//TODO: dispay not gathering
    		Log.v("AudioToWotKit", "gathering data stoppped");
    	}
    }
    
    private class AudioToWotKitAsyncTask extends AsyncTask<ACTIVITY, Integer, Void>{
    	String groundTruth;
    	    	
    	AudioRecord recorder;
    	int windowsPerSecond;
    	FFT fft = new FFT(CONST.WINDOW_SIZE.val, CONST.Fs.val);
    	double[] energyBands;
    	
    	public static final String HOST = "http://142.103.25.29:8080";
    	public static final String PATH = "/api/sensors/";
        public static final String ST_USERNAME = "dawood";
        public static final String ST_PASSWORD = "1234dawood";
        
        //TODO: set SENSOR_NAME based on input
        private static final String SENSOR_NAME = "audio0";
        
        private static final boolean POST_TO_WOTKIT = true;
        
    	@Override
    	protected void onPreExecute(){
    		gatheringData(true);    	
    	}
    	
    	@Override
    	protected Void doInBackground(ACTIVITY... groundTruth) {
    		this.groundTruth = groundTruth[0].toString();
    		//initialize audio
    		windowsPerSecond = CONST.Fs.val/CONST.WINDOW_SIZE.val;
    		recorder = new AudioRecord(AudioSource.MIC, CONST.Fs.val, AudioFormat.CHANNEL_IN_MONO, 
					AudioFormat.ENCODING_PCM_16BIT, CONST.WINDOW_SIZE.val*CONST.BUFFER_SIZE_MULT.val);
    		
    		short data[] = new short[CONST.WINDOW_SIZE.val];
    		energyBands = new double[CONST.NUMBER_OF_ENERGY_BANDS.val];
    		double[] tempEnergyBands = new double[CONST.NUMBER_OF_ENERGY_BANDS.val];
    		float[] fftIn = new float[CONST.WINDOW_SIZE.val];
    		float[] fftOut = null;
    		recorder.startRecording();
    		
    		while(!isCancelled()){
    			read1sAudio(energyBands, windowsPerSecond, recorder, data, tempEnergyBands, fftIn, fftOut, fft);
    			if(POST_TO_WOTKIT) postToWotKit();
//    			String out = Double.toString(energyBands[0]);
//    			for(int i = 1; i < energyBands.length; i++) out += " " + Double.toString(energyBands[i]);
//    			Log.v("bands", out);
    		}
			return null;
    	}
    	
    	private void postToWotKit(){
    		 HttpResponse response = null;
    	        
	        try {
	        	//URI address = new URI("http", null, HOST, 8080, "/wotkit/api/sensors/" + ST_USERNAME + "." + SENSOR_NAME + "/data", "value=50", null);
	            //Log.v("posting to wotkit", address.toString());
	        	//HttpPost post = new HttpPost(address.toString());
	        	DefaultHttpClient httpclient = new DefaultHttpClient();
	            httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(ST_USERNAME, ST_PASSWORD));
	            
	        	String url = HOST + PATH + ST_USERNAME + "." + SENSOR_NAME + "/data";
	        	Log.v("http Post", url);
	        	HttpPost httppost = new HttpPost(url);

	        	//TODO: params
	        	List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
	        	nvpList.add(new BasicNameValuePair("value", Integer.toString(0) ));
	        	for(int i = 0; i < CONST.NUMBER_OF_ENERGY_BANDS.val; i++){
	        		nvpList.add(new BasicNameValuePair("band"+Integer.toString(i), Double.toString(energyBands[i])));
	        	}
	        	nvpList.add(new BasicNameValuePair("ground", groundTruth));
	        	Log.v("ground truth", groundTruth);
	        	httppost.setEntity(new UrlEncodedFormEntity(nvpList));
	        	
	        	
	        	
	        	response = httpclient.execute(httppost);
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        } 
    	}
    	
    	@Override
    	protected void onCancelled(Void temp){
    		recorder.release();
    		gatheringData(false);
    	}
    	
    }
}