package com.wmbest.sabrowser;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import org.apache.http.impl.client.DefaultHttpClient;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.HttpResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import android.util.Log;

public class SABrowserActivity extends Activity
{

	private static final String TAG = "SABrowserActivity";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		Log.d(TAG, "Entered onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		String websiteData = null;

		try {

			DefaultHttpClient client = new DefaultHttpClient();
			URI uri = new URI("http://www.google.com/");
			HttpGet method = new HttpGet(uri);
			HttpResponse res = client.execute(method);
			Log.d(TAG, "Created Objects, Now Creating Stream");
			InputStream data = res.getEntity().getContent();
			websiteData = generateString(data);

			Log.d(TAG, "Generated String: " + websiteData);

			TextView tx = (TextView)findViewById(R.id.output);
			tx.setText(websiteData);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    }

	public String generateString(InputStream stream) {
		Log.d(TAG, "generateString");
		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader buffer = new BufferedReader(reader);
		StringBuilder sb = new StringBuilder();

		try {
			String cur;
			while ((cur = buffer.readLine()) != null) {
				sb.append(cur + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

}
