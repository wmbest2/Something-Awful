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
import android.util.Log;
import org.w3c.dom.*;


import java.util.ArrayList;

public class ThreadActivity extends Activity
{

	private static final String TAG = "ThreadActivity";
    private static final int THREAD_LIST_RETURNED = 1;
    private static final int ERROR = -1;

    private ArrayList<SAThread> mThreadTitleList;
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


		url = "http://forums.somethingawful.com/" + getIntent().getStringExtra("url");

        mForumList = (ListView) findViewById(R.id.forum_list);
        mThreadTitleList = new ArrayList<SAThread>();

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
        private ArrayList<SAThread> mItems;
        private LayoutInflater mInflater;

        public ThreadListAdapter(Context aContext, ArrayList<SAThread> aItems) {
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
            title.setText(mItems.get(aPosition).title);

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
                SAHttpRequest http = new SAHttpRequest();
                Document dom = http.httpGet(url);
                Log.d(TAG, "DOM Parsed printing results");

                NodeList nl = dom.getElementsByTagName("a");

                for(int i = 0; i < nl.getLength(); ++i)
                {
                    Element a = (Element)nl.item(i);

                    if(a.getAttribute("class").equals("thread_title")) {
	                    Log.d(TAG, "Item: " + ((Text)a.getFirstChild()).getData() );
                        mThreadTitleList.add(new SAThread(((Text)a.getFirstChild()).getData(), a.getAttribute("href")));
                    }
                }
				Log.d(TAG, "Somethings not working fucker");
                msgResponse.what = THREAD_LIST_RETURNED;

            } catch (DOMException e) {
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
