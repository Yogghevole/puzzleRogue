package view.util;

import javafx.scene.Node;

/**
 * Utility class for JavaFX Node operations.
 * Provides helper methods for traversing the scene graph, such as checking ancestry.
 */
public final class NodeUtils {
    private NodeUtils() {}

    public static boolean isDescendantOf(Node node, Node ancestor) {
        if (node == null || ancestor == null) return false;
        Node cur = node;
        while (cur != null) {
            if (cur == ancestor) return true;
            cur = cur.getParent();
        }
        return false;
    }
}