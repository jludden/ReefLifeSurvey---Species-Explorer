package me.jludden.reeflifesurvey.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by Jason on 11/6/2017.
 *
 * Button widget that has both a background target drawable and a text overlay
 */

public class TextImageButton extends AppCompatButton implements Target {
    public TextImageButton(Context context) {
        super(context);
    }

    public TextImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    /**
     * Callback when an image has been successfully loaded.
     * @param bitmap
     * @param from
     */
    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        setBackgroundDrawable(new BitmapDrawable(bitmap));
    }

    /**
     * Callback indicating the image could not be successfully loaded.
     * @param errorDrawable
     */
    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    /**
     * Callback invoked right before your request is submitted.
     * @param placeHolderDrawable
     */
    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
