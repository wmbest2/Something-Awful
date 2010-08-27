package com.wmbest.sabrowser;

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
import org.w3c.dom.*;
import org.htmlcleaner.*;
import android.util.Log; 

public class SAHttpRequest {
	private static final String TAG = "SAHttpRequest";

	public class RequestException extends Exception {
		public RequestException() {
		}
	};

	public SAHttpRequest() {

	}

	public Document httpGet(String url) throws RequestException {
		try{
			DefaultHttpClient client = new DefaultHttpClient();
			URI uri = new URI(url);
			HttpGet method = new HttpGet(uri);
			HttpResponse res = client.execute(method);
			
			Log.d(TAG, "Created Objects, Now Creating Stream");
			InputStream data = res.getEntity().getContent();
			
			Log.d(TAG, "Create HTML Cleaner");
			HtmlCleaner hc = new HtmlCleaner();
			CleanerProperties props = hc.getProperties();
			DomSerializer ds = new DomSerializer(props, true);	

			Log.d(TAG, "Parse DOM");
			return ds.createDOM(hc.clean(data));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
			throw new RequestException();
        } catch (IOException e) {
            e.printStackTrace();
			throw new RequestException();
		} catch (URISyntaxException e) {
            e.printStackTrace();
			throw new RequestException();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RequestException();
		}	
	}
}
