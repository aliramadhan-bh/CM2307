import java.util.*;
import java.util.logging.Level;

public class NetworkDeviceManager {

    private final LoggingManager logging;
    private final Map<String, NMS.NetworkDevice> devices = new HashMap<>();

    public NetworkDeviceManager(LoggingManager logging) {
        this.logging = logging;
    }

    public void addDevice(NMS.NetworkDevice device) {

        if (device == null) return;

        devices.put(device.getId(), device);
        logging.logEvent(Level.INFO, "Device added: " + device.getId());

    }

    public void removeDevice(String deviceId) {

        NMS.NetworkDevice removed = devices.remove(deviceId);

        if (removed != null) {

            logging.logEvent(Level.INFO, "Device removed: " + deviceId);

        }
    }

    public void configureDevice(String deviceId, NMS.DeviceConfiguration config) {

        NMS.NetworkDevice nd = devices.get(deviceId);

        if (nd != null) {

            nd.setConfig(config);

            logging.logEvent(Level.INFO, "Device " + deviceId + " reconfigured.");
        }
    }

    public List<NMS.NetworkDevice> getDevices() {

        return new ArrayList<>(devices.values());

    }
    public NMS.NetworkDevice getDeviceById(String deviceId) {

        return devices.get(deviceId);

    }
}
