package jpinn.io.augmentedreality;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private CameraComponent mCameraComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraComponent = new CameraComponent(this);
        FrameLayout cameraView = (FrameLayout) findViewById(R.id.camera_layout);
        cameraView.addView(mCameraComponent);
    }
}
