package pasu.ntracker.viewer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

import pasu.ntracker.DriverMapActivity;
import pasu.ntracker.MainActivity;
import pasu.ntracker.MapsActivity;
import pasu.ntracker.R;
import pasu.ntracker.data.CommonData;
import pasu.ntracker.data.Tracker;
import pasu.ntracker.utils.CommonUtils;
import pasu.ntracker.utils.SessionSave;

/**
 * Created by developer on 26/9/17.
 */


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<Tracker> albumList;
    private int selectedItem;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //        public TextView title, count;
//        public ImageView thumbnail, overflow;
        LinearLayout card_view;
        TextView video_desc, video_date;
        ImageView video_thumb;

        public MyViewHolder(View view) {
            super(view);
//            title = (TextView) view.findViewById(R.id.title);
//            count = (TextView) view.findViewById(R.id.count);
//            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
//            overflow = (ImageView) view.findViewById(R.id.overflow);
            card_view = (LinearLayout) view.findViewById(R.id.card_view);
            video_desc = (TextView) view.findViewById(R.id.video_desc);
            video_thumb = (ImageView) view.findViewById(R.id.video_thumb);
            video_date = (TextView) view.findViewById(R.id.video_date);
            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedItem = getAdapterPosition();
                    //notifyDataSetChanged();
                    Intent i =new Intent(mContext, DriverMapActivity.class);
                    i.putExtra("type",CommonData.RECEIVER_ACTIVITY);
                    i.putExtra(CommonData.TRACK_ID,albumList.get(selectedItem).getTrackID());
                    i.putExtra(CommonData.CURRENT_TRACK_INFO,CommonUtils.toJson(albumList.get(selectedItem)))
;                    mContext.startActivity(i);

                }
            });
        }
    }

    public interface VideoInterface {
        void videoSelected(int pos);
    }

    public VideoAdapter(Context mContext, ArrayList<Tracker> media) {
        this.mContext = mContext;
        this.albumList = media;
        // this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    void setSelection(int pos) {
        selectedItem = pos;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
//        Album album = albumList.get(position);
        if (position < albumList.size()) {
            if (position == selectedItem)
                holder.card_view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            else
                holder.card_view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            System.out.println("[poooooo" + position + "__" + albumList.size());
            holder.video_desc.setText(albumList.get(position).getTrackID());
            holder.video_date.setText(CommonUtils.getDate(albumList.get(position).getTimeStarted()));
            //Picasso.with(mContext).load(albumList[position].getThumbnail()).into(holder.video_thumb);
        }
    }


    @Override
    public int getItemCount() {
        return albumList.size();
    }
}