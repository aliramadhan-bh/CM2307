import java.util.*;
import java.util.logging.Level;

public class RouteManager {

    private final LoggingManager logging;
    private final NetworkDeviceManager deviceManager;

    private final Map<String, Set<String>> adjacencyList = new HashMap<>();

    public RouteManager(LoggingManager logging, NetworkDeviceManager deviceManager) {

        this.logging = logging;
        this.deviceManager = deviceManager;
    }

    public void addConnection(String id1, String id2) {

        if (deviceManager.getDeviceById(id1) == null || deviceManager.getDeviceById(id2) == null) {

            logging.logEvent(Level.WARNING, "Cannot connect " + id1 + " and " + id2 + ": one or both devices not found.");

            return;
        }
        adjacencyList.putIfAbsent(id1, new HashSet<>());
        adjacencyList.putIfAbsent(id2, new HashSet<>());
        adjacencyList.get(id1).add(id2);
        adjacencyList.get(id2).add(id1);

        logging.logEvent(Level.INFO, "Connection created: " + id1 + " <-> " + id2);
    }

    public void removeConnection(String id1, String id2) {

        var s1 = adjacencyList.get(id1);
        var s2 = adjacencyList.get(id2);

        if (s1 != null) s1.remove(id2);
        if (s2 != null) s2.remove(id1);

        logging.logEvent(Level.INFO, "Connection removed: " + id1 + " <-> " + id2);
    }


    public void addRoute(NMS.NetworkDevice src, NMS.NetworkDevice dst, int weight) {

        addConnection(src.getId(), dst.getId());
    }

    public List<NMS.NetworkDevice> getOptimalRoute(NMS.NetworkDevice source, NMS.NetworkDevice destination) {

        List<NMS.NetworkDevice> result = new ArrayList<>();

        if (source == null || destination == null) return result;

        String srcId = source.getId();
        String dstId = destination.getId();

        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(srcId);
        visited.add(srcId);

        boolean found = false;

        while (!queue.isEmpty()) {

            String current = queue.poll();

            if (current.equals(dstId)) {

                found = true;
                break;
            }
            Set<String> neighbors = adjacencyList.getOrDefault(current, Collections.emptySet());

            for (String nbr : neighbors) {

                if (!visited.contains(nbr)) {

                    visited.add(nbr);
                    parent.put(nbr, current);
                    queue.add(nbr);
                }
            }
        }

        if (!found) {

            logging.logEvent(Level.INFO, "No route found from " + srcId + " to " + dstId);
            return result;
        }


        List<String> pathIds = new ArrayList<>();

        for (String cur = dstId; cur != null; cur = parent.get(cur)) {

            pathIds.add(cur);
        }
        Collections.reverse(pathIds);

        for (String id : pathIds) {

            result.add(deviceManager.getDeviceById(id));
        }

        logging.logEvent(Level.INFO, "Route found: " + pathIds);

        return result;
    }
}
