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

public class PostListActivity extends Activity
{

	private static final String TAG = "PostListActivity";
    private static final int POST_LIST_RETURNED = 1;
    private static final int ERROR = -1;

    private ArrayList<SAPost> mPostList;
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
        mPostList = new ArrayList<SAPost>();

        mHandler = new Handler() {
            public void handleMessage(Message aMessage) {
                mLoadingDialog.dismiss();

                switch(aMessage.what) {
                    case POST_LIST_RETURNED:
                        mForumList.setAdapter(new PostListAdapter(PostListActivity.this, mPostList));
                        break;
                    case ERROR:
                        Log.e(TAG, "ERRORRRRR");
                        break;
                }
            }
        };

        mLoadingDialog = ProgressDialog.show(PostListActivity.this, "Loading", "Fetching Posts...", true);
        new PreloadThread(mHandler).start();
    }

    private class PostListAdapter extends BaseAdapter {
        private ArrayList<SAPost> mItems;
        private LayoutInflater mInflater;

        public PostListAdapter(Context aContext, ArrayList<SAPost> aItems) {
            mItems = aItems;
            mInflater = LayoutInflater.from(aContext);
        }

        public View getView(int aPosition, View aConvertView, ViewGroup aParent) {
            View forumItem;

            // Recycle old View's if the list is long
            if (aConvertView == null) {
                forumItem = mInflater.inflate(R.layout.post_list_item, null);
            } else {
                forumItem = aConvertView;
            }

            TextView content = (TextView) forumItem.findViewById(R.id.post_content);
            content.setText(mItems.get(aPosition).content);

            TextView poster = (TextView) forumItem.findViewById(R.id.post_poster);
            poster.setText(mItems.get(aPosition).poster);

            TextView date = (TextView) forumItem.findViewById(R.id.post_date);
            date.setText(mItems.get(aPosition).date);

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

                NodeList nl = dom.getElementsByTagName("table");

                for(int i = 0; i < nl.getLength(); ++i) {
                    Element table = (Element)nl.item(i);
					String body = "";
					String poster = "";
                    if(table.getAttribute("class").equals("post")) {
	                   NodeList tdlist = table.getElementsByTagName("td");

					   for(int j = 0; j < tdlist.getLength(); ++j) {
						   Element td = (Element)tdlist.item(j);
						  
						   if(td.getAttribute("class").equals("postbody")) {  		//Get Post Body
								
								NodeList bodylist = td.getChildNodes();
								for(int k = 0; k < bodylist.getLength(); ++k)
								{
									if(bodylist.item(k).getNodeName().equals("#text")) {
										if(!bodylist.item(k).getNodeValue().equals("\n")) {
											Log.d(TAG,  "Line: \"" + bodylist.item(k).getNodeValue() + "\"");
											body = body + ((Text)bodylist.item(k)).getData() + "\n";
										}
									}
								}
						   } else if (td.getAttribute("class").equals("postdate")) { //Get PostDate

						   } else { 												//Get Poster

						   }
					   }
					   mPostList.add(new SAPost(body, "TEST", "TEST"));

					   

                    }
                }
				Log.d(TAG, "Somethings not working fucker");
                msgResponse.what = POST_LIST_RETURNED;

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
