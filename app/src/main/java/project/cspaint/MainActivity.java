package project.cspaint;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import petrov.kristiyan.colorpicker.ColorPicker;

public class MainActivity extends AppCompatActivity {

    private static String myImage;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Paintings");

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting view references by ids
        DrawView paint = findViewById(R.id.drawView);
        RangeSlider rangeSliderBrush = findViewById(R.id.rangeBarBrush);
        ImageButton undo = findViewById(R.id.undo);
        ImageButton color = findViewById(R.id.color);
        ImageButton brush = findViewById(R.id.brush);
        ImageButton eraser = findViewById(R.id.eraser);

        // ask for storage access
        askPermission();

        // Set up filename format for saving images
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());
        myImage = path + "/" + date + ".png";
        if (!path.exists())
            path.mkdirs();

        // onClick listeners for buttons

        // UNDO - remove most recent stroke from canvas
        undo.setOnClickListener(view -> paint.undo());

        // COLOR - allows user to select brush color
        color.setOnClickListener(view -> {
            final ColorPicker colorpicker = new ColorPicker(MainActivity.this);
            colorpicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                @Override
                public void setOnFastChooseColorListener(int position, int color) {
                    // get int value of color from selector box and set as stroke color
                    paint.setColor(color);
                }
                @Override
                public void onCancel() {
                    colorpicker.dismissDialog();
                }
            })
                    // dialog options
                    .setColumns(5)
                    .setDefaultColorButton(Color.parseColor("#000000"))
                    .show();
        });

        // BRUSH - toggle rangeSliderBrush for brush size
        brush.setOnClickListener(view -> {
            if (rangeSliderBrush.getVisibility() == View.VISIBLE)
                rangeSliderBrush.setVisibility(View.GONE);
            else
                rangeSliderBrush.setVisibility(View.VISIBLE);
        });

        // ERASER - toggle rangeSliderEraser for eraser size
        eraser.setOnClickListener(view -> paint.clear());

        // configure rangeSlider
        rangeSliderBrush.setValueFrom(0.0f);
        rangeSliderBrush.setValueTo(100.0f);

        // add onChange listener to apply slider values
        rangeSliderBrush.addOnChangeListener((slider, value, fromUser) -> paint.setStrokeWidth((int) value));

        // pass height and width of custom view to DrawView init method
        ViewTreeObserver vto = paint.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                paint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = paint.getMeasuredWidth();
                int height = paint.getMeasuredHeight();
                paint.init(height, width);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        DrawView paint = findViewById(R.id.drawView);
        Bitmap bmp = paint.save();
        switch (item.getItemId()) {
            case R.id.menu_save:
                // SAVE - output current canvas bitmap as png
                    if (bmp == null)
                        Toast.makeText(MainActivity.this, "No content to save", Toast.LENGTH_SHORT).show();
                    else {
                        try {
                            saveImage(bmp);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Save failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // save image to device storage
    private void saveImage(Bitmap bitmap) throws IOException {
        File image = new File(myImage);

        // create byte array from bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bitmapData = baos.toByteArray();

        // output array as file
        FileOutputStream fos = new FileOutputStream(image);
        fos.write(bitmapData);
        // tidy up and close
        fos.flush();
        fos.close();

        Toast.makeText(this, "Your painting is saved", Toast.LENGTH_SHORT).show();
    }

    // ask permission to access storage
    private void askPermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        Toast.makeText(MainActivity.this, "File Access Approved", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
}