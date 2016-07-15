package jpinn.io.augmentedreality;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by pujan on 7/15/2016.
 */
public class CameraComponent extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "CameraComponent";

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private Camera.Size prevSize;
    private int[] pixels;
    private Camera.Parameters parameters;

    public CameraComponent(Context context) {
        super(context);

        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        if(motionEvent.getAction() ==MotionEvent.ACTION_DOWN){

        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
            parameters = mCamera.getParameters();
            mCamera.getParameters().setPreviewFormat(ImageFormat.NV21);
            prevSize = parameters.getPreviewSize();

            pixels = new int[prevSize.width*prevSize.height];
        } catch (IOException e) {
            mCamera.release();
            mCamera=null;

            e.printStackTrace();
            Log.d(TAG, "Error setting camera preview");
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder mHolder, int format, int width, int height) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                parameters.setPreviewSize(width, height);
                mCamera.setParameters(parameters);
                mCamera.startPreview();

            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        getCameraInstance().stopPreview();
        getCameraInstance().release();
    }

    /** Check if this device has a camera */
    public boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object.    */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //transforms NV21 pixel data into RGB pixels
        decodeYUV420SP(pixels, data, prevSize.width,  prevSize.height);
        int x = 0;
        int y =0;
        Bitmap bmp = Bitmap.createBitmap(pixels, prevSize.width, prevSize.height, Bitmap.Config.ARGB_8888);
        int pixel = bmp.getPixel( x,y );
        int redValue = Color.red(pixel);
        int blueValue = Color.blue(pixel);
        int greenValue = Color.green(pixel);
        int thiscolor = Color.rgb(redValue, greenValue, blueValue);
        //Outuput the value of the top left pixel in the preview to LogCat
        Log.i("Pixels", "The top right pixel has the following RGB (hexadecimal) values:"
                +Integer.toHexString(pixels[0]));
    }

    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
}
