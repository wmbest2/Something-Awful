package com.wmbest.sabrowser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Context;
import android.widget.BaseAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;

public class ThreadActivity extends Activity
{

	private static final String TAG = "ThreadActivity";
    private static final int THREAD_LIST_RETURNED = 1;
    private static final int ERROR = -1;

    private ArrayList<String> mThreadTitleList;
    private Handler mHandler;
    private ListView mForumList;
    private ProgressDialog mLoadingDialog;
	private String url;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		Log.d(TAG, "Entered onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


		url = getIntent().getStringExtra("url");

        mForumList = (ListView) findViewById(R.id.forum_list);
        mThreadTitleList = new ArrayList<String>();

        mHandler = new Handler() {
            public void handleMessage(Message aMessage) {
                mLoadingDialog.dismiss();

                switch(aMessage.what) {
                    case THREAD_LIST_RETURNED:
                        mForumList.setAdapter(new ThreadListAdapter(ThreadActivity.this, mThreadTitleList));
                        break;
                    case ERROR:
                        Log.e(TAG, "ERRORRRRR");
                        break;
                }
            }
        };

        mLoadingDialog = ProgressDialog.show(ThreadActivity.this, "Loading", "Fetching Threads...", true);
        new PreloadThread(mHandler).start();
    }

    private class ThreadListAdapter extends BaseAdapter {
        private ArrayList<String> mItems;
        private LayoutInflater mInflater;

        public ThreadListAdapter(Context aContext, ArrayList<String> aItems) {
            mItems = aItems;
            mInflater = LayoutInflater.from(aContext);
        }

        public View getView(int aPosition, View aConvertView, ViewGroup aParent) {
            View forumItem;

            // Recycle old View's if the list is long
            if (aConvertView == null) {
                forumItem = mInflater.inflate(R.layout.thread_list_item, null);
            } else {
                forumItem = aConvertView;
            }

            TextView title = (TextView) forumItem.findViewById(R.id.thread_name);
            title.setText(mItems.get(aPosition));

            return forumItem;
        }

        public int getCount() {
            return mItems.size();
        }

        public Object getItem(int aPosition) {
            return mItems.get(aPosition);
        }

        public long getItemId(int aPosition) {
            return aPosition;
        }
    }

    private class PreloadThread extends Thread {

        Handler mHandler;

        PreloadThread(Handler aHandler) {

            mHandler = aHandler;
        }

        public void run() {

            Message msgResponse = Message.obtain();
            msgResponse.setTarget(mHandler);

            String websiteData = null;

            try {
                DefaultHttpClient client = new DefaultHttpClient();
				Log.d(TAG, url);
                URI uri = new URI("http://forums.somethingawful.com/" + url);
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

                for(int i = 0; i < nl.getLength(); ++i)
                {
                    Element a = (Element)nl.item(i);
                    //Log.d(TAG, "Item: " + a.getFirstChild().getNodeName() );

                    if(a.getAttribute("class").equals("thread_title")) {
                        mThreadTitleList.add(((Text)a.getFirstChild()).getData());
                    }
                }

                msgResponse.what = THREAD_LIST_RETURNED;

            } catch (DOMException e) {
                e.printStackTrace();
                msgResponse.what = ERROR;
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                msgResponse.what = ERROR;
            } catch (IOException e) {
                e.printStackTrace();
                msgResponse.what = ERROR;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                msgResponse.what = ERROR;
            } catch (Exception e) {
                Log.i(TAG, e.toString());
                msgResponse.what = ERROR;
            }

            msgResponse.sendToTarget();
        }
    }
}
