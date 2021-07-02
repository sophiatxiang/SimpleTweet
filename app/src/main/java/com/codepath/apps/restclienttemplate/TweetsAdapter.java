package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.CommentActivity;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;

    TwitterClient client;

    public static final int REQUEST_CODE = 30;

    public static final String TAG = "TweetsAdapter";

    // pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    // for each row, inflate the layout
    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        client = TwitterApp.getRestClient(context.getApplicationContext());
        return new ViewHolder(view);
    }

    // bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // get the data at position
        Tweet tweet = tweets.get(position);
        // bind the tweet with view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }


    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvDate;
        ImageView ivMedia;
        ImageButton ibReply;
        ImageButton ibRetweet;
        ImageButton ibLike;
        TextView tvName;
        TextView tvFavoriteCount;
        TextView tvRetweetCount;
        RelativeLayout relativeLayout;



        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ibReply = itemView.findViewById(R.id.ibReply);
            ibRetweet = itemView.findViewById(R.id.ibRetweet);
            ibLike = itemView.findViewById(R.id.ibLike);
            tvName = itemView.findViewById(R.id.tvName);
            tvFavoriteCount = itemView.findViewById(R.id.tvFavoriteCount);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
            relativeLayout.setOnClickListener(this);
        }

        public void bind(final Tweet tweet) {
            //setting all of the views
            RequestOptions imgOptions = new RequestOptions();
            imgOptions = imgOptions.transforms(new CenterCrop(), new RoundedCorners(20));
            tvBody.setText(tweet.body);
            tvScreenName.setText("@" + tweet.user.screenName);
            tvName.setText(tweet.user.name);
            tvDate.setText(tweet.relTimeAgo);
            tvFavoriteCount.setText("" + tweet.favoriteCount);
            tvRetweetCount.setText("" + tweet.retweetCount);
            Glide.with(context).load(tweet.user.profileImageUrl).transform(new CircleCrop()).into(ivProfileImage);
            if (tweet.media_url != null) {
                Glide.with(context).load(tweet.media_url).apply(imgOptions).into(ivMedia);
                ivMedia.setVisibility(View.VISIBLE);
            } else ivMedia.setVisibility(View.GONE);

            if (tweet.favorited.equals("true")) {
                ibLike.setSelected(true);
            } else ibLike.setSelected(false);

            if (tweet.retweeted.equals("true")) {
                ibRetweet.setSelected(true);
            } else ibRetweet.setSelected(false);


            //create onclick listener for comment button
            ibReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra("tweetUser", tweet.user.screenName);
                    intent.putExtra("tweetId", tweet.id);
                    context.startActivity(intent);
                }
            });


            //create onclick listener for like button
            ibLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if like is already selected, pressing the button unlikes the post
                    if (tweet.favorited.equals("false")){
                        ibLike.setSelected(true);
                        tweet.favorited = "true";
                        tweet.favoriteCount++;
                        tvFavoriteCount.setText("" + tweet.favoriteCount);
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
                    //if like is not selected, pressing the button likes the post
                    else {
                        ibLike.setSelected(false);
                        tweet.favorited = "false";
                        tweet.favoriteCount--;
                        tvFavoriteCount.setText("" + tweet.favoriteCount);
                        client.destroyLike(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "onSuccess unliked tweet id:" + tweet.id);
                            }
                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "onFailure to unlike tweet: ", throwable);
                            }
                        });
                    }
                }
            });


            //create onclick listener for retweet button
            ibRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if retweet is already selected, pressing the button unretweets the post
                    if (tweet.retweeted.equals("false")){
                        ibRetweet.setSelected(true);
                        tweet.retweeted = "true";
                        tweet.retweetCount++;
                        tvRetweetCount.setText("" + tweet.retweetCount);
                        client.publishRetweet(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "onSuccess retweet tweet id:" + tweet.id);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "onFailure to retweet tweet: ", throwable);
                            }
                        });
                    }
                    //if retweet is not selected, pressing the button retweets the post
                    else {
                        ibRetweet.setSelected(false);
                        tweet.retweeted = "false";
                        tweet.retweetCount--;
                        tvRetweetCount.setText("" + tweet.retweetCount);
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
            });
        }


        //if the view holder is clicked from the timeline activity, launch tweet details activity
        @Override
        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the tweet at the position, this won't work if the class is static
                Tweet tweet = tweets.get(position);
                // create new intent for the new activity
                Intent intent = new Intent(context, TweetDetailsActivity.class);
                // serialize the new tweet using parceler, use its short name as a key
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                // show the activity
                context.startActivity(intent);
            }
        }
    }
}
