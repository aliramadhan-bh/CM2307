import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class NMS {

    public static void main(String[] args) {

        if (args.length < 4) {

            System.out.println("Usage: java -cp . NMS devices.txt connections.txt <fromDeviceId> <toDeviceId>");

            return;
        }

        String devicesFile = args[0];
        String connectionsFile = args[1];
        String fromId = args[2];
        String toId   = args[3];

        LoggingManager logging = LoggingManager.getInstance();

        NetworkDeviceManager deviceManager = new NetworkDeviceManager(logging);

        RouteManager routeManager = new RouteManager(logging, deviceManager);

        loadDevices(devicesFile, deviceManager);
        loadConnections(connectionsFile, routeManager);

        NetworkDevice fromDev = deviceManager.getDeviceById(fromId);
        NetworkDevice toDev   = deviceManager.getDeviceById(toId);

        if (fromDev == null || toDev == null) {

            System.out.println("Invalid source or destination!");

            return;
        }

        List<NetworkDevice> path = routeManager.getOptimalRoute(fromDev, toDev);

        if (path.isEmpty()) {

            System.out.println("No route found!");

        }

        else {

            System.out.println("Optimal route: " + String.join(" -> ", toIds(path)));
        }
    }

    private static void loadDevices(String file, NetworkDeviceManager dm) {

        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader(file));

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty()) {

                    continue;
                }

                String[] parts = line.split(",", 3);

                if (parts.length < 2) {

                    continue;
                }

                String id      = parts[0].trim();
                String type    = parts[1].trim();
                String cfgPart = (parts.length == 3) ? parts[2].trim() : "";

                DeviceConfiguration config = parseConfig(cfgPart);

                NetworkDevice device = NetworkDevice.createDevice(id, type, config);

                dm.addDevice(device);
            }

        } catch (IOException e) {

            System.err.println("Error loading devices: " + e.getMessage());

        } finally {

            if (br != null) {

                try {

                    br.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private static DeviceConfiguration parseConfig(String line) {

        DeviceConfiguration cfg = new DeviceConfiguration();

        if (line.contains("Config:{") && line.endsWith("}")) {

            int start = line.indexOf('{');
            int end   = line.lastIndexOf('}');

            if (start != -1 && end != -1 && end > start) {

                String inner = line.substring(start + 1, end).trim();
                String[] tokens = inner.split(";");

                for (String token : tokens) {

                    token = token.trim();

                    if (!token.isEmpty()) {

                        String[] kv = token.split("=", 2);

                        if (kv.length == 2) {

                            String key = kv[0].trim();
                            String val = kv[1].trim();

                            if (key.equals("Interface")) {

                                cfg.setInterfaceName(val);

                            } else if (key.equals("MAC")) {

                                cfg.setMacAddress(val);

                            } else if (key.equals("IPV4")) {

                                cfg.setIpAddress(val);

                            } else if (key.equals("Subnet")) {

                                cfg.setSubnet(val);
                            }
                        }
                    }
                }
            }
        }
        return cfg;
    }

    private static void loadConnections(String file, RouteManager rm) {

        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader(file));

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", 2);

                if (parts.length == 2) {

                    String id1 = parts[0].trim();
                    String id2 = parts[1].trim();

                    rm.addConnection(id1, id2);
                }
            }
        } catch (IOException e) {

            System.err.println("Error loading connections: " + e.getMessage());

        } finally {

            if (br != null) {

                try {

                    br.close();

                } catch (IOException ignored) {}
            }
        }
    }

    private static List<String> toIds(List<NetworkDevice> devices) {
        List<String> ids = new ArrayList<String>();
        for (NetworkDevice d : devices) {
            ids.add(d.getId());
        }
        return ids;
    }
    static class NetworkDevice {
        private String id;
        private String type;
        private DeviceConfiguration config;

        public static NetworkDevice createDevice(String id, String type, DeviceConfiguration cfg) {

            return new NetworkDevice(id, type, cfg);
        }


        private NetworkDevice(String id, String type, DeviceConfiguration config) {

            this.id = id;
            this.type = type;
            this.config = (config != null) ? config : new DeviceConfiguration();
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public DeviceConfiguration getConfig() {
            return config;
        }

        public void setConfig(DeviceConfiguration config) {
            this.config = config;
        }
    }

    static class DeviceConfiguration {
        private String interfaceName;
        private String macAddress;
        private String ipAddress;
        private String subnet;

        public void setInterfaceName(String s) {
            this.interfaceName = s;
        }
        public void setMacAddress(String s) {
            this.macAddress = s;
        }
        public void setIpAddress(String s) {
            this.ipAddress = s;
        }
        public void setSubnet(String s) {
            this.subnet = s;
        }
    }
}
