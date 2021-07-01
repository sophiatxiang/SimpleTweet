package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetDetailsActivity extends AppCompatActivity {

    public static final String TAG = "TweetDetailsActivity";

    ImageView ivProfileImage;
    TextView tvBody;
    TextView tvScreenName;
    TextView tvDate;
    ImageView ivMedia;
    ImageButton ibReply;
    ImageButton ibRetweet;
    ImageButton ibLike;
    TextView tvName;
    TextView tvFavoriteNum;
    TextView tvRetweetNum;

    Tweet tweet;

    TwitterClient client;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);

        client = TwitterApp.getRestClient(this);

        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
        Log.d("TweetDetailsActivity", String.format("Showing details for '%s'", tweet.body));

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvBody = findViewById(R.id.tvBody);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvDate = findViewById(R.id.tvDate);
        ivMedia = findViewById(R.id.ivMedia);
        ibReply = findViewById(R.id.ibReply);
        ibRetweet = findViewById(R.id.ibRetweet);
        ibLike = findViewById(R.id.ibLike);
        tvName = findViewById(R.id.tvName);
        tvFavoriteNum = findViewById(R.id.tvFavoriteNum);
        tvRetweetNum = findViewById(R.id.tvRetweetNum);

        RequestOptions imgOptions = new RequestOptions();
        imgOptions = imgOptions.transforms(new CenterCrop(), new RoundedCorners(20));
        tvBody.setText(tweet.body);
        tvScreenName.setText("@" + tweet.user.screenName);
        tvName.setText(tweet.user.name);
        tvDate.setText(tweet.relTimeAgo);
        tvFavoriteNum.setText("" + tweet.favoriteCount);
        tvRetweetNum.setText("" + tweet.retweetCount);
        Glide.with(getApplicationContext()).load(tweet.user.profileImageUrl).transform(new CircleCrop()).into(ivProfileImage);
        if (tweet.media_url != null) {
            Glide.with(getApplicationContext()).load(tweet.media_url).apply(imgOptions).into(ivMedia);
            ivMedia.setVisibility(View.VISIBLE);
        } else ivMedia.setVisibility(View.GONE);

        if (tweet.favorited.equals("true")) {
            ibLike.setSelected(true);
        } else ibLike.setSelected(false);

        if (tweet.retweeted.equals("true")) {
            ibRetweet.setSelected(true);
        } else ibRetweet.setSelected(false);
    }

    public void clickedLikeBtn(View view) {
        if (tweet.favorited.equals("false")){
            ibLike.setSelected(true);
            tweet.favorited = "true";
            client.publishLike(tweet.id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.i(TAG, "onSuccess tweet id:" + tweet.id);
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "onFailure to like tweet: ", throwable);
                }
            });
        }
        else {
            ibLike.setSelected(false);
            tweet.favorited = "false";
            client.destroyLike(tweet.id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.i(TAG, "onSuccess tweet id:" + tweet.id);
                }
                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "onFailure to like tweet: ", throwable);
                }
            });
        }
    }

    public void clickedRetweetBtn(View view) {
        if (tweet.retweeted.equals("false")){
            ibRetweet.setSelected(true);
            tweet.retweeted = "true";
            client.publishRetweet(tweet.id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.i(TAG, "onSuccess retweeted tweet id:" + tweet.id);
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "onFailure to retweet tweet: ", throwable);
                }
            });
        }
        else {
            ibRetweet.setSelected(false);
            tweet.retweeted = "false";
            client.destroyRetweet(tweet.id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.i(TAG, "onSuccess unretweet id:" + tweet.id);
                }
                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e(TAG, "onFailure to unretweet tweet: ", throwable);
                }
            });
        }
    }
}