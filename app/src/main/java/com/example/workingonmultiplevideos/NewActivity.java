package com.example.workingonmultiplevideos;

import android.content.Context;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NewActivity extends AppCompatActivity implements View.OnClickListener
{
    private static final int NUMBER_OF_VIDEOS_IN_QUEUE = 3;

    private Context context;
    private String package_name;

    private VideoView video_view;
    private int video_one_resource,video_two_resource,video_three_resource;
    private String[] videos_path;


    private MediaPlayer mediaPlayer;

    private SeekBar video_seek_bar;
    private int video_seek_bar_max;

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            for (VideoModel videoModel : video_model_list)
            {
                if (videoModel.is_video_playing)
                {
                    if(video_view.getCurrentPosition() > 0)
                    {
                        video_seek_bar.setProgress(videoModel.start_duration + video_view.getCurrentPosition());
                    }
                }
            }

            if (imageViewModels!=null && !imageViewModels.isEmpty())
            {
                for (ImageViewModel imageViewModel : imageViewModels)
                {
                    int progress = video_seek_bar.getProgress();
                    if (progress >=imageViewModel.start_duration && progress < imageViewModel.end_duration)
                    {
                        imageViewModel.imageView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        imageViewModel.imageView.setVisibility(View.GONE);
                    }
                }
            }

            handler.postDelayed(runnable,0);
        }
    };

    private List<VideoModel> video_model_list;

    private boolean isFrameChange;
    private int progress_value;

    private TextView start_time,end_time;


    private HorizontalScrollView horizontal_scroll_view;
    private LinearLayout linear_layout;
    private MediaMetadataRetriever mediaMetadataRetriever;
    private List<Bitmap> videos_frame_list;
    private List<List<Bitmap>> video_frames_respectively;


    private RelativeLayout layer;
    private Button add_button;
    private Random random;
    private Bitmap bitmap;
    List<ImageViewModel> imageViewModels;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        package_name = context.getPackageName();
        video_view = (VideoView) findViewById(R.id.video_view_id);
        video_seek_bar = (SeekBar) findViewById(R.id.video_seek_bar_id);
        start_time = (TextView) findViewById(R.id.start_time);
        end_time = (TextView) findViewById(R.id.end_time);
        horizontal_scroll_view = (HorizontalScrollView) findViewById(R.id.horizontal_scroll_view);
        linear_layout = (LinearLayout) findViewById(R.id.linear_layout);

        layer = (RelativeLayout) findViewById(R.id.layer);
        layer.setOnClickListener(this);
        add_button = (Button) findViewById(R.id.add);
        add_button.setOnClickListener(this);
        random = new Random();
        bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        bitmap = Bitmap.createScaledBitmap(bitmap,50,50,true);
        imageViewModels = new ArrayList<>();

        video_one_resource = R.raw.video_one;
        video_two_resource = R.raw.video_two;
        video_three_resource = R.raw.video_three;

        videos_path = new String[NUMBER_OF_VIDEOS_IN_QUEUE];
        videos_path[0] = "android.resource://"+package_name+"/"+video_one_resource;
        videos_path[1] = "android.resource://"+package_name+"/"+video_two_resource;
        videos_path[2] = "android.resource://"+package_name+"/"+video_three_resource;

        video_model_list = new ArrayList<>();

        for (int i=0;i<NUMBER_OF_VIDEOS_IN_QUEUE;i++)
        {
            VideoModel video_model = new VideoModel();
            video_model.video_uri = Uri.parse(videos_path[i]);

            mediaPlayer = null;
            mediaPlayer = MediaPlayer.create(this,video_model.video_uri);
            video_model.video_duration = mediaPlayer.getDuration();
            mediaPlayer.release();

            video_model.start_duration = video_seek_bar_max;
            video_seek_bar_max += video_model.video_duration;
            video_model.end_duration = video_seek_bar_max;

            video_model.is_video_playing = false;

            video_model_list.add(video_model);
        }

        video_seek_bar.setMax(video_seek_bar_max);

//        int track_number = 0;
//        startVideo(track_number);

        isFrameChange = false;

        String time = String.format("%d : %d",
                TimeUnit.MILLISECONDS.toMinutes(video_seek_bar_max),
                TimeUnit.MILLISECONDS.toSeconds(video_seek_bar_max) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(video_seek_bar_max))
        );
        end_time.setText(time);

        /*MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute(video_model_list);*/

        handler.postDelayed(runnable,0);

        video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                for (VideoModel videoModel : video_model_list)
                {
                    int index = video_model_list.indexOf(videoModel);
                    video_model_list.get(index).is_video_playing = false;
                    if (progress_value >= videoModel.start_duration && progress_value < videoModel.end_duration)
                    {
                        video_view.setVideoURI(videoModel.video_uri);
                        video_view.requestFocus();
                        video_view.seekTo(progress_value-videoModel.start_duration);
                        video_model_list.get(index).is_video_playing = true;
                        video_view.start();
                    }
                }

                if (progress_value == video_seek_bar_max)
                {
                    video_seek_bar.setProgress(0);
                    video_view.stopPlayback();
                }

            }
        });

        video_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                String time = String.format("%d : %d",
                        TimeUnit.MILLISECONDS.toMinutes(progress),
                        TimeUnit.MILLISECONDS.toSeconds(progress) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))
                );
                start_time.setText(time);

                for (VideoModel videoModel : video_model_list)
                {
                    if (progress >=videoModel.start_duration && progress < videoModel.end_duration)
                    {
                        if (progress > 0)
                        {
                            progress_value = videoModel.end_duration;
                        }
                    }
                }

                if(isFrameChange)
                {
                    for (VideoModel videoModel : video_model_list)
                    {
                        if (videoModel.is_video_playing)
                        {
                            int index = video_model_list.indexOf(videoModel);
                            video_view.seekTo(video_seek_bar.getProgress()-videoModel.start_duration);

                            if(video_seek_bar.getProgress()>videoModel.end_duration)
                            {
                                if ((index+1) < video_model_list.size())
                                {
                                    video_view.setVideoURI(video_model_list.get(index + 1).video_uri);
                                    video_view.requestFocus();
                                    video_view.start();
                                    videoModel.is_video_playing = false;
                                    video_model_list.get(index + 1).is_video_playing = true;
                                }
                            }

                            if(video_seek_bar.getProgress()<videoModel.start_duration)
                            {
                                if((index-1) != -1)
                                {
                                    video_view.setVideoURI(video_model_list.get(index - 1).video_uri);
                                    video_view.requestFocus();
                                    video_view.start();
                                    videoModel.is_video_playing = false;
                                    video_model_list.get(index - 1).is_video_playing = true;
                                }

                            }
                        }
                    }

                    if (imageViewModels!=null && !imageViewModels.isEmpty())
                    {
                        for (ImageViewModel imageViewModel : imageViewModels)
                        {
                            //int progress = video_seek_bar.getProgress();
                            if (progress >=imageViewModel.start_duration && progress < imageViewModel.end_duration)
                            {
                                imageViewModel.imageView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                imageViewModel.imageView.setVisibility(View.GONE);
                            }
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

                for (VideoModel videoModel : video_model_list)
                {
                    int index = video_model_list.indexOf(videoModel);
                    video_model_list.get(index).is_video_playing = false;
                    if (progress >=videoModel.start_duration && progress < videoModel.end_duration)
                    {
                        video_view.setVideoURI(videoModel.video_uri);
                        video_view.requestFocus();
                        video_view.seekTo(video_seek_bar.getProgress()-videoModel.start_duration);
                        video_model_list.get(index).is_video_playing = true;
                    }
                }
                video_view.start();
                handler.postDelayed(runnable,0);
            }
        });
    }

    private void startVideo(int track_number)
    {
        for (int i=0;i<video_model_list.size();i++)
        {
            if ( i == track_number)
            {
                video_model_list.get(i).is_video_playing = true;
                video_view.setVideoURI(video_model_list.get(i).video_uri);
                video_view.requestFocus();
                video_view.start();
            }
            else
            {
                video_model_list.get(i).is_video_playing = false;
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (video_view!=null)
        {
            video_view.pause();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (video_view!=null && video_seek_bar != null)
        {
            video_seek_bar.setProgress(0);
            int track_number = 0;
            startVideo(track_number);
        }
    }

    public Bitmap optimizedBitmap(final byte[] image,int requiredWidth, int requiredHeight )
    {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(image,0,image.length,options);
//        BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, requiredWidth, requiredHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(image,0,image.length,options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int requiredWidth, int requiredHeight )
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > requiredHeight || width > requiredWidth)
        {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > requiredHeight && (halfWidth / inSampleSize) > requiredWidth)
            {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.layer:
                if (video_view!=null)
                {
                    video_view.start();
                }
                handler.postDelayed(runnable,0);
                break;
            case R.id.add:
                if (video_view!=null)
                {
                    video_view.pause();
                }
                handler.removeCallbacks(runnable);
                addImageViewOperation();
                break;
        }
    }

    private void addImageViewOperation()
    {
        ImageView imageView = new ImageView(context);
        int sample_width = ((context.getResources().getDisplayMetrics().widthPixels)*80)/100;
        int sample_height = Math.round(150.0f * getResources().getDisplayMetrics().density);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        int left = random.nextInt(sample_width);
        int top = random.nextInt(sample_height);
        int right = random.nextInt(sample_width);
        int bottom = random.nextInt(sample_height);
        params.setMargins(left,top,right,bottom);
        imageView.setLayoutParams(params);
        imageView.setImageBitmap(bitmap);
        layer.addView(imageView);

        int additional_duration = 3000;
        ImageViewModel imageViewModel = new ImageViewModel();
        imageViewModel.imageView = imageView;
        imageViewModel.start_duration = video_seek_bar.getProgress();
        imageViewModel.end_duration = imageViewModel.start_duration + additional_duration;

        imageViewModels.add(imageViewModel);

    }

    public class MyAsyncTask extends AsyncTask<List<VideoModel> ,Void,List<List<Bitmap>>>
    {

        @Override
        protected void onPreExecute()
        {
            long millis = System.currentTimeMillis();
            Date date = new Date(millis);
// formattter
            SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss");
// Pass date object
            String formatted = formatter.format(date );
            start_time.setText(formatted);
        }

        @Override
        protected List<List<Bitmap>> doInBackground(List<VideoModel>... params)
        {
            video_frames_respectively = new ArrayList<>();
            for (VideoModel videoModel : params[0])
            {
                videos_frame_list = new ArrayList<>();
                for (int i=0;i<videoModel.end_duration/1000;i++)
                {
                    if (i%3 == 0)
                    {
                        mediaMetadataRetriever = null;
                        mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(context, videoModel.video_uri);
//BitmapFactory.d
                        Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime((i*1000*1000), MediaMetadataRetriever.OPTION_CLOSEST);
                        /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        final Bitmap optimized_bitmap = optimizedBitmap(byteArray, 10, 10);*/
                        videos_frame_list.add(bitmap);
                        mediaMetadataRetriever.release();
                    }
                }
                video_frames_respectively.add(videos_frame_list);
            }
            return video_frames_respectively;
        }

        @Override
        protected void onPostExecute(List<List<Bitmap>> video_frames_respectively)
        {
            long millis = System.currentTimeMillis();
            Date date = new Date(millis);
// formattter
            SimpleDateFormat formatter= new SimpleDateFormat("HH:mm:ss");
// Pass date object
            String formatted = formatter.format(date);
            end_time.setText(formatted);

            for (List<Bitmap> bitmap_list : video_frames_respectively)
            {
                Log.i("Frames",""+bitmap_list.size());
                for (Bitmap bitmap : bitmap_list)
                {
                    ImageView imageView = new ImageView(context);
                    imageView.setImageBitmap(bitmap);
                    linear_layout.addView(imageView);
                }
            }
        }
    }
}
