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
import org.w3c.tidy.*;
import org.w3c.dom.*;

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
			URI uri = new URI("http://forums.somethingawful.com/");
			HttpGet method = new HttpGet(uri);
			HttpResponse res = client.execute(method);
			Log.d(TAG, "Created Objects, Now Creating Stream");
			InputStream data = res.getEntity().getContent();

			Log.d(TAG, "Create Tidy");
			Tidy tidy = new Tidy();
			Log.d(TAG, "Parse DOM");

			Document dom = tidy.parseDOM(data, null);

			Log.d(TAG, "DOM Parsed printing results");

			NodeList nl = dom.getElementsByTagName("a");
			String out = "";
			for(int i = 0; i < nl.getLength(); ++i)
			{
				Element a = (Element)nl.item(i);
				Log.d(TAG, "Item: " + a.getFirstChild().getNodeName() );

				if(a.getAttribute("class").equals("forum")) {
					out = out + a.getFirstChild().getNodeName() + "\n";
				}
				
			}


			TextView tx = (TextView)findViewById(R.id.output);
			tx.setText(out);
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    }

}
