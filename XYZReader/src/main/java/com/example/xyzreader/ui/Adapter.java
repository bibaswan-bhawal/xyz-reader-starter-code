package com.example.xyzreader.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

class Adapter extends RecyclerView.Adapter<ArticleListActivity.ViewHolder> {
    private final ArticleListActivity articleListActivity;
    private Cursor mCursor;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);
    private static final String TAG = ArticleListActivity.class.toString();

    public Adapter(ArticleListActivity articleListActivity, Cursor cursor) {
        this.articleListActivity = articleListActivity;
        mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleListActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = articleListActivity.getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
        final ArticleListActivity.ViewHolder vh = new ArticleListActivity.ViewHolder(view);
        final ImageView thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) view.getContext(), thumbnailView, thumbnailView.getTransitionName()).toBundle();
                articleListActivity.startActivity(new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))), bundle);
            }
        });
        return vh;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(ArticleListActivity.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {

            holder.subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            holder.subtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                    + "<br/>" + " by "
                    + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        }

        Picasso.get().load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                .resize(600,400)
                .centerCrop()
                .into(holder.thumbnailView);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
