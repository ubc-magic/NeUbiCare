package sensetecnic.wotkit.androwotkit;

import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends Activity  implements SensorEventListener{
 
	private SensorManager sensorManager;
	private static final String TAG = "AccelerometerReading";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		// add listener. The listener will be (this) class
		sensorManager.registerListener(this, 
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		
		/*	More sensor speeds (taken from api docs)
		    SENSOR_DELAY_FASTEST get sensor data as fast as possible
		    SENSOR_DELAY_GAME	rate suitable for games
		 	SENSOR_DELAY_NORMAL	rate (default) suitable for screen orientation changes
		*/
		
	}
	
public void onAccuracyChanged(Sensor sensor,int accuracy){
		
	}
	
	public void onSensorChanged(SensorEvent event){
		
		// check sensor type
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){			

			
			// assign directions
			float x=event.values[0];
			float y=event.values[1];
			float z=event.values[2];
			
			
                            //Trigger shake routine
                        	try {

                	    		BasicNameValuePair bnvp_x = new BasicNameValuePair("x", "" + (int)x);
                	    		BasicNameValuePair bnvp_y = new BasicNameValuePair("y", "" + (int)y);
                	    		BasicNameValuePair bnvp_z = new BasicNameValuePair("z", "" + (int)z);

                	    		String URL = "http://wotkit.sensetecnic.com/api/sensors/androwotkit.androwotkit1/data";
                	    			 		   HTTPConnection httpCon = new HTTPConnection(getApplicationContext(), URL);
                	    			           httpCon.execute(bnvp_x, bnvp_y, bnvp_z);

                	    		 }
                	    			 		   catch (Exception e)
                	    			 		   {
                	    			 			   Log.d(TAG, "Failure in posting the data to Sensetecnic. " + e.getMessage() );
                	    			 			   Toast.makeText(getApplicationContext(), "Failure in posting the data to Sensetecnic. " + e.getMessage(), Toast.LENGTH_LONG ).show(); 
                	    			 		   }
                        	
		}       
	}
		
			
	}
