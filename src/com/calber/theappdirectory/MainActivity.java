package com.calber.theappdirectory;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlo Alberto Negri
 */

public class MainActivity extends Activity {

    private static String TAG = "theApp";
    PhotoAdapter pad;
    ImageLoader imageLoader;
    RequestQueue queue;
    LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        pad = new PhotoAdapter();
        ((ListView) findViewById(R.id.listView)).setAdapter(pad);

        queue = Volley.newRequestQueue(this);
        imageLoader = new ImageLoader(queue, new BitmapCache(40));

        Uri.Builder uri = new Uri.Builder();
        uri.scheme("http");
        uri.authority("theappbusiness.com");
        uri.path("our-team");
        String url = uri.build().toString();

        StringRequest request = new StringRequest(Request.Method.GET, url, createSuccessListener(), createErrorListener());

        queue.add(request);

    }

    private Response.ErrorListener createErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Response: " + error.getMessage());
            }
        };
    }

    private Response.Listener<String> createSuccessListener() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                List<AppPeople> plist = new ArrayList<AppPeople>();

                Document doc = Jsoup.parse(response);
                Elements peoples = doc.select("div.col2");

                String a, b, c, d;
                for (Element people : peoples) {
                    try {
                        a = people.select("div.title").select("img").first().attr("src");
                        b = people.select("h3").first().text();
                        c = people.select("p").get(0).text();
                        d = people.select("p").get(1).text();

                        plist.add(new AppPeople(people.select("div.title").select("img").first().attr("src"),
                                people.select("h3").first().text(), people.select("p").get(0).text(), people.select("p").get(1).text()));
                    } catch (Exception e) {
                        Log.e(TAG, "error", e);
                    }
                }

                pad.setList(plist);
                Log.d(TAG, "Error Response code: " + toString());
            }
        };
    }

    class PhotoAdapter extends BaseAdapter {
        private List<AppPeople> plist;

        PhotoAdapter() {
            plist = new ArrayList<AppPeople>();
        }

        public void setList(List<AppPeople> plist) {
            this.plist = plist;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return plist.size();
        }

        @Override
        public Object getItem(int i) {
            return plist.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null) {
                view = inflater.inflate(R.layout.peoplerow, viewGroup, false);
            }

            ((TextView) view.findViewById(R.id.name)).setText(this.plist.get(i).getName());
            ((TextView) view.findViewById(R.id.job)).setText(this.plist.get(i).getJobtitle());
            ((TextView) view.findViewById(R.id.bio)).setText(this.plist.get(i).getBio());

            ((NetworkImageView) view.findViewById(R.id.image)).setImageUrl(this.plist.get(i).getImgurl(), imageLoader);

            return view;
        }
    }

    class AppPeople {
        String imgurl, name, jobtitle, bio;

        AppPeople(String imgurl, String name, String jobtitle, String bio) {
            this.imgurl = imgurl;
            this.name = name;
            this.jobtitle = jobtitle;
            this.bio = bio;
        }

        String getImgurl() {
            return imgurl;
        }

        String getName() {
            return name;
        }

        String getJobtitle() {
            return jobtitle;
        }

        String getBio() {
            return bio;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public class BitmapCache extends LruCache implements ImageLoader.ImageCache {
        public BitmapCache(int maxSize) {
            super(maxSize);
        }

        @Override
        public Bitmap getBitmap(String url) {
            Log.d(TAG, "Get: " + url);
            return (Bitmap) get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            Log.d(TAG, "Put: " + url);
            put(url, ImageHelper.getRoundedShape(bitmap));
        }
    }
}
