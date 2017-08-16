package com.example.workingonmultiplevideos;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{
    private Context context;
    private String package_name;

    private VideoView video_view;
    private int video_one_resource,video_two_resource;
    private String video_one_path,video_two_path;
    private Uri video_one_uri,video_two_uri;
    private boolean video_one_playing;
//    private MediaMetadataRetriever mediaMetadataRetriever;
    private MediaPlayer mediaPlayer;
    private int video_one_duration,video_two_duration;
    private boolean isFrameChange;

    private SeekBar video_seek_bar;

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            if(video_one_playing)
            {
                if(video_view.getCurrentPosition() > 0)
                {
                    video_seek_bar.setProgress(video_view.getCurrentPosition());
                }
            }
            else
            {
                if(video_view.getCurrentPosition() > 0)
                {
                    video_seek_bar.setProgress(video_one_duration + video_view.getCurrentPosition());
                }
            }


            handler.postDelayed(runnable,0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        package_name = context.getPackageName();
        video_view = (VideoView) findViewById(R.id.video_view_id);
        video_seek_bar = (SeekBar) findViewById(R.id.video_seek_bar_id);

        video_one_resource = R.raw.video_one;
        video_two_resource = R.raw.video_two;

        video_one_path = "android.resource://"+package_name+"/"+video_one_resource;
        video_two_path = "android.resource://"+package_name+"/"+video_two_resource;

        video_one_uri = Uri.parse(video_one_path);
        video_two_uri = Uri.parse(video_two_path);

        video_view.setVideoURI(video_one_uri);
        video_view.requestFocus();
        video_view.start();

        mediaPlayer = MediaPlayer.create(this,video_one_uri);
        video_one_duration = mediaPlayer.getDuration();
        mediaPlayer.release();

        mediaPlayer = null;
        mediaPlayer = MediaPlayer.create(this,video_two_uri);
        video_two_duration = mediaPlayer.getDuration();
        mediaPlayer.release();

        /*mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(context,video_one_uri);
        video_one_duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toString());
        mediaMetadataRetriever.release();

        mediaMetadataRetriever = null;
        mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(context,video_two_uri);
        video_two_duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toString());
        mediaMetadataRetriever.release();*/


        video_seek_bar.setMax(video_one_duration+video_two_duration);

        video_one_playing = true;

        isFrameChange = false;

        handler.postDelayed(runnable,0);


        video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                if (video_one_playing)
                {
                    video_view.setVideoURI(video_two_uri);
                    video_view.requestFocus();
                    video_view.start();

                    video_one_playing = false;
                }
                else
                {
                    video_view.setVideoURI(video_one_uri);
                    video_view.requestFocus();
                    video_view.start();

                    video_one_playing = true;
                }

            }
        });

        video_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if(isFrameChange)
                {
                    if(video_one_playing)
                    {
                        video_view.seekTo(video_seek_bar.getProgress());
                        if(video_seek_bar.getProgress()>video_one_duration)
                        {
                            video_view.stopPlayback();
                            video_view.setVideoURI(video_two_uri);
                            video_view.requestFocus();
                            video_view.start();

                            video_one_playing = false;
                        }
                    }
                    else
                    {
                        video_view.seekTo(video_seek_bar.getProgress()-video_one_duration);
                        if(video_seek_bar.getProgress()<=video_one_duration)
                        {
                            video_view.stopPlayback();
                            video_view.setVideoURI(video_one_uri);
                            video_view.requestFocus();
                            video_view.start();

                            video_one_playing = true;
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                handler.removeCallbacks(runnable);
                isFrameChange = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                handler.removeCallbacks(runnable);
                isFrameChange = false;
                int progress = seekBar.getProgress();

                if (progress >= 0 && progress <= video_one_duration)
                {
                    video_view.stopPlayback();
                    video_view.setVideoURI(video_one_uri);
                    video_view.requestFocus();

                    video_one_playing = true;
                }
                else if (progress > video_one_duration && progress <= (video_one_duration+video_two_duration))
                {
                    video_view.stopPlayback();
                    video_view.setVideoURI(video_two_uri);
                    video_view.requestFocus();

                    video_one_playing = false;
                }

                if(video_one_playing)
                {
                    video_view.seekTo(video_seek_bar.getProgress());
                }
                else
                {
                    video_view.seekTo(video_seek_bar.getProgress()-video_one_duration);
                }

                video_view.start();
                handler.postDelayed(runnable,0);


            }
        });

    }
}
