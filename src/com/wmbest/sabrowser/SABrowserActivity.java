package com.wmbest.sabrowser;

import android.app.Activity;
import android.os.Bundle;
import org.apache.http.*;

public class SABrowserActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		String websiteData = null;

		try {

			DefaultHttpClient client = new DefaultHttpClient();
			URI u = new URI("http://forums.somethingawful.com");
			HttpGet method = new HttpGet(uri);
			HttpResponse res = client.execute(method);

			InputStream data = res.getEntity().getContent();
			websiteData = generateString(data);

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
