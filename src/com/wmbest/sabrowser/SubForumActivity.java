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
import android.widget.AdapterView;
import android.widget.AdapterView.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.content.Intent;
import org.w3c.dom.*;


import java.util.ArrayList;

public class SubForumActivity extends Activity
{

	private static final String TAG = "SubForumActivity";
    private static final int SUBFORUM_LIST_RETURNED = 1;
    private static final int ERROR = -1;

    private ArrayList<SAForum> mSubForumTitleList;
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
        mSubForumTitleList = new ArrayList<SAForum>();

        mHandler = new Handler() {
            public void handleMessage(Message aMessage) {
                mLoadingDialog.dismiss();

                switch(aMessage.what) {
                    case SUBFORUM_LIST_RETURNED:
                        mForumList.setAdapter(new SubForumListAdapter(SubForumActivity.this, mSubForumTitleList));
						mForumList.setOnItemClickListener( new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								Intent intent = new Intent(SubForumActivity.this, ThreadActivity.class);
								intent.putExtra("url", ((SAForum)parent.getAdapter().getItem(position)).url);
								SubForumActivity.this.startActivity(intent);
							}
						});
   						mForumList.setOnItemLongClickListener( new OnItemLongClickListener() {
							@Override
							public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
								Intent intent = new Intent(SubForumActivity.this, SubForumActivity.class);
								intent.putExtra("url", ((SAForum)parent.getAdapter().getItem(position)).url);
								Log.d(TAG, intent.getStringExtra("url"));
								SubForumActivity.this.startActivity(intent);
								return true;
							}
						});
                        break;
                    case ERROR:
                        Log.e(TAG, "ERRORRRRR");
                        break;
                }
            }
        };

        mLoadingDialog = ProgressDialog.show(SubForumActivity.this, "Loading", "Fetching SubForums...", true);
        new PreloadThread(mHandler).start();
    }

    private class SubForumListAdapter extends BaseAdapter {
        private ArrayList<SAForum> mItems;
        private LayoutInflater mInflater;

        public SubForumListAdapter(Context aContext, ArrayList<SAForum> aItems) {
            mItems = aItems;
            mInflater = LayoutInflater.from(aContext);
        }

        public View getView(int aPosition, View aConvertView, ViewGroup aParent) {
            View forumItem;

            // Recycle old View's if the list is long
            if (aConvertView == null) {
                forumItem = mInflater.inflate(R.layout.subforum_list_item, null);
            } else {
                forumItem = aConvertView;
            }

            TextView title = (TextView) forumItem.findViewById(R.id.subforum_name);
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

                NodeList nl = dom.getElementsByTagName("tr");

                for(int i = 0; i < nl.getLength(); ++i)
                {
                    Element tr = (Element)nl.item(i);

                    if(tr.getAttribute("class").equals("subforum")) {
	                   NodeList sublist = tr.getElementsByTagName("a");
					   Element a = (Element)sublist.item(0);
		               mSubForumTitleList.add(new SAForum(((Text)a.getFirstChild()).getData(), a.getAttribute("href")));	   
                    }
                }
				Log.d(TAG, "Somethings not working fucker");
                msgResponse.what = SUBFORUM_LIST_RETURNED;

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
