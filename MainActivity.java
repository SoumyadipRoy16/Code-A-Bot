import java.io.IOException;
import java.util.TimerTask;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.launch;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Button startButton;
    private TextView statusTextView;

    private SensorManager sensorManager;
    private Sensor motionSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.start_button);
        statusTextView = findViewById(R.id.status_text_view);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            motionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMonitoring();
            }
        });
    }

    private void startMonitoring() {
        if (motionSensor == null) {
            Toast.makeText(this, "Motion sensor not available", Toast.LENGTH_SHORT).show();
            return;
        }

        sensorManager.registerListener(this, motionSensor, SensorManager.SENSOR_DELAY_NORMAL);
        statusTextView.setText("Monitoring motion...");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double magnitude = Math.sqrt(x * x + y * y + z * z);

        double threshold = 10.0;

        if (magnitude > threshold) {
            statusTextView.setText("Motion detected!");
            sendAlertToServer();
        }
    }

    private void simulateMotionEvents() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                float simulatedMagnitude = generateSimulatedMagnitude();
                processMotionEvent(simulatedMagnitude);
            }
        }, 0, 10000); 
    }

    private float generateSimulatedMagnitude() {
        return (float) (Math.random() * 20); 
    }

    private void processMotionEvent(float magnitude) {
        double threshold = 10.0; 
        if (magnitude > threshold) {
            statusTextView.setText("Motion detected! (Simulated)");
            sendAlertToServer(); 
        }
    }

    private void simulateCamera() {
        imageView.setImageResource(R.drawable.camera_image);
    }

    private void integrateWithSimulationPlatform() {
        val simulatedMagnitude = generateSimulatedMagnitude();

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulate sending motion event data to the simulation platform
                val motionEventDataResponse = apiService.sendMotionEventData(simulatedMagnitude);
                if (motionEventDataResponse.isSuccessful()) {
                    showToast("Motion event data sent successfully to the simulation platform");
                } else {
                    showToast("Failed to send motion event data to the simulation platform");
                }

                // Simulate sending camera data to the simulation platform
                val cameraDataResponse = apiService.sendCameraData("ImageFilePath");
                if (cameraDataResponse.isSuccessful()) {
                    showToast("Camera data sent successfully to the simulation platform");
                } else {
                    showToast("Failed to send camera data to the simulation platform");
                }
            } catch (e: Exception) {
                e.printStackTrace();
                showToast("Error occurred while sending data to the simulation platform");
            }
        }
    }

    private void sendDataToSimulationPlatform(String eventType, String eventData) {
        String simulationPlatformUrl = "http://simulation-platform.com/api/data";
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, "{"
                + "\"eventType\":\"" + eventType + "\","
                + "\"eventData\":\"" + eventData + "\""
                + "}");

        Request request = new Request.Builder()
                .url(simulationPlatformUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showToast("Failed to send data to the simulation platform");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    showToast("Data sent successfully to the simulation platform");
                } else {
                    showToast("Failed to send data to the simulation platform");
                }
            }
        });
    }

    // Helper method to show toast message
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }


    private void sendAlertToServer() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://your-server-url.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Void> call = apiService.sendAlert();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Alert sent successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to send alert", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to send alert: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    public interface ApiService {
        @POST("sendAlert")
        Call<Void> sendAlert();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                Toast.makeText(this, "Accelerometer sensor accuracy is unreliable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }
}