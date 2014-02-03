package sensetecnic.wotkit.androwotkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/** 
 * 
 * 
 * @author Dawood Al-Masslawi
 *
 */
public class HTTPConnection extends AsyncTask<BasicNameValuePair, Void, HttpResponse> {
    
    /* Sense Tecnic URL. */
    public static String SENSE_TECNIC = "http://wotkit.sensetecnic.com/api/sensors/androwotkit.androwotkit1/data";

    public String url;
    public static final String ST_USERNAME = "androwotkit";
    public static final String ST_PASSWORD = "androwotkit1234";
    
    Context c;

    /* Default constructor. */
    public HTTPConnection (Context c, String URL) {
        this.c = c;
        this.url = URL;
    }

    @SuppressWarnings("finally")
    @Override
    protected HttpResponse doInBackground(BasicNameValuePair... params) {

        HttpResponse response = null;
        
        try {

            /* Set up DefaultHttpClient object to do POST with authorization. */
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(ST_USERNAME, ST_PASSWORD));
            
            /* Configure parameters. */
            HttpPost httppost = new HttpPost(url);
            List<NameValuePair> nvp = new ArrayList<NameValuePair>(Arrays.asList(params));

            httppost.setEntity(new UrlEncodedFormEntity(nvp));
                        
            /* Do the post. */
            response = httpclient.execute(httppost);
			
            Toast.makeText(c, "HTTP Post!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(c, "Exception!" + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            return response;
        }
    }

    protected void onPostExecute (HttpResponse response) {
        
        /* We only need to consider if the response is null (if the POST failed for some reason. */
        if (response != null) {
            //Log.v("SenseTecnicPoster", "POST response: " + response.getStatusLine().getReasonPhrase());
        	//Toast.makeText(c, "POST response: " + response.getStatusLine().getReasonPhrase(), Toast.LENGTH_LONG).show();
        } else {
        	Toast.makeText(c, "Response is null!", Toast.LENGTH_LONG).show();
            Log.e("STPoster", "Error sending data to Sense Tecnic.");
        }
        
    }
     
}
