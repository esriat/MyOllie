package com.example.myollie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orbotix.ConvenienceRobot;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.Ollie;
import com.orbotix.Sphero;
import com.orbotix.command.RGBLEDOutputCommand;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotLE;
import com.orbotix.common.RobotChangedStateListener;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RobotChangedStateListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 42;
    private static final float VIVACITE_OLLIE = 1f;

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;

    private ConvenienceRobot ollie;

    private TextView textMyo;
    private TextView textOllie;

    private boolean isDriving = false;

    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            textMyo.setText("Myo " + myo.getName() + " connecté.");
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            textMyo.setText("Myo " + myo.getName() + " déconnecté.");
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            textMyo.setText(myo.getArm() == Arm.LEFT ? "Bras gauche" : "Bras droit");
            myo.unlock(Myo.UnlockType.HOLD);
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            textMyo.setText("Myo désynchronisé");
            myo.lock();
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {

        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {

        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
//            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
//            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
//            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
//            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));
//
//            // Adjust roll and pitch for the orientation of the Myo on the arm.
//            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
//                roll *= -1;
//                pitch *= -1;
//            }
//
//            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
//            mTextView.setRotation(roll);
//            mTextView.setRotationX(pitch);
//            mTextView.setRotationY(yaw);
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    Log.d("MYOPOSE", "Unknown");
                    break;
                case REST:
                    Log.d("MYOPOSE", "Rest");
                    if(ollie != null) ollie.stop();
                    break;
                case DOUBLE_TAP:
                    Log.d("MYOPOSE", "Double tap");
                    break;
                case FIST:
                    if(ollie != null) {
                        Log.d("TestDrive", "On passe");
                        ollie.drive(0f, VIVACITE_OLLIE);
                    }
//                        ollie.sendCommand(new RGBLEDOutputCommand(0.5f, 0.5f, 0.5f));
                    Log.d("MYOPOSE", "Fist");
                    break;
                case WAVE_IN:
                    Log.d("MYOPOSE", "Wave in");
                    ollie.drive(90f, VIVACITE_OLLIE);
                    break;
                case WAVE_OUT:
                    Log.d("MYOPOSE", "Wave out");
                    ollie.drive(270f, VIVACITE_OLLIE);
                    break;
                case FINGERS_SPREAD:
                    ollie.drive(180f, VIVACITE_OLLIE);
                    Log.d("MYOPOSE", "Fingers spread");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textMyo = (TextView) findViewById(R.id.textMyo);
        textMyo.setText("Pas de Myo");

        textOllie = (TextView) findViewById(R.id.textOllie);
        textOllie.setText("Pas de Ollie");

        /*button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);*/

        DualStackDiscoveryAgent.getInstance().addRobotStateListener(this);

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int hasLocationPermission = checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION );
            if( hasLocationPermission != PackageManager.PERMISSION_GRANTED ) {
                Log.e( "Sphero", "Location permission has not already been granted" );
                List<String> permissions = new ArrayList<String>();
                permissions.add( Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()] ), REQUEST_CODE_LOCATION_PERMISSION );
            } else {
                Log.d( "Sphero", "Location permission already granted" );
            }
        }

        Hub hub = Hub.getInstance();

        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        hub.addListener(mListener);
    }

    private void onScanMyoSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch ( requestCode ) {
            case REQUEST_CODE_LOCATION_PERMISSION: {
                for( int i = 0; i < permissions.length; i++ ) {
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        //startDiscovery();
                        Log.d( "Permissions", "Permission Granted: " + permissions[i] );
                    } else if( grantResults[i] == PackageManager.PERMISSION_DENIED ) {
                        Log.d( "Permissions", "Permission Denied: " + permissions[i] );
                    }
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void startDiscovery() {
        //If the DiscoveryAgent is not already looking for robots, start discovery.
        Log.d("DISCOVERY", "Start Discovery");
        if( !DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            try {
                DualStackDiscoveryAgent.getInstance().startDiscovery( this );
                this.textOllie.setText("En cours");
            } catch (DiscoveryException e) {
                Log.e("Sphero", "DiscoveryException: " + e.getMessage());
                this.textOllie.setText("pas de Ollie");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*try {
            DualStackDiscoveryAgent.getInstance().startDiscovery(this);
        } catch (DiscoveryException e) {
            //handle
        }*/
    }

    @Override
    protected void onStop(){
        if( DualStackDiscoveryAgent.getInstance().isDiscovering() ) {
            DualStackDiscoveryAgent.getInstance().stopDiscovery();
        }
        super.onStop();
    }

    protected void onDestroy(){
        if(ollie != null) ollie.disconnect();
        super.onDestroy();
        DualStackDiscoveryAgent.getInstance().addRobotStateListener(null);
    }

    @Override
    public void onClick(View v) {
        //if(ollie == null) return;
        switch (v.getId()){
            case R.id.button1:
                onScanMyoSelected();
                break;
            case R.id.button2:
                Log.d("ButtonOllie", "Button2");
                if(ollie != null) ollie.sendCommand(new RGBLEDOutputCommand(0f, 0f, 1f));
                break;
            case R.id.button5:
                Log.d("Button", "Button5");
                if(ollie!=null) ollie.sendCommand( new RGBLEDOutputCommand( 0f, 1f, 0f ) );
//                ollie.drive(180.0f, VIVACITE_OLLIE);
                break;
            case R.id.button4:
                Log.d("Button", "Button4");
                if(ollie!=null) ollie.sendCommand(new RGBLEDOutputCommand(1f, 0f, 0f));
//                ollie.drive(270.0f, VIVACITE_OLLIE);
                break;
            case R.id.button3:
                Log.d("ButtonOllie", "Button3");
                startDiscovery();
                break;
        }
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {
        switch (robotChangedStateNotificationType){
            case Online:
                ollie = new Ollie(robot);
        }
    }
}
