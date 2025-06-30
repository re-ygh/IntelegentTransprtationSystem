import java.util.*;

public class MSTCalculator {
    static List<UniPaths> computeMST(List<Universities> nodes, List<UniPaths> allEdges) {
        List<UniPaths> mst = new ArrayList<>();
        Map<String, String> parent = new HashMap<>();

        for (Universities u : nodes) {
            parent.put(u.getUniversityName(), u.getUniversityName());
        }

        allEdges.sort(Comparator.comparingInt(UniPaths::getCost));

        for (UniPaths edge : allEdges) {
            String root1 = find(parent, edge.getStartLocation());
            String root2 = find(parent, edge.getEndLocation());
            if (!root1.equals(root2)) {
                edge.setInMST(true);
                mst.add(edge);
                parent.put(root1, root2);
            }
        }
        return mst;
    }

    private static String find(Map<String, String> parent, String node) {
        if (!parent.get(node).equals(node)) {
            parent.put(node, find(parent, parent.get(node)));
        }
        return parent.get(node);
    }
}