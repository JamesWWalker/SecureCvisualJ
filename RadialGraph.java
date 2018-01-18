import java.util.*;
import javafx.scene.canvas.*;
import javafx.scene.layout.*;

// Represents a graph node
class RGNode {
  public String name;
  public double x, y;
  public List<Integer> callees = new ArrayList<>();
  public List<Integer> callers = new ArrayList<>();
  public int depth;
  public double angle;
  public boolean collapsed;
  public boolean visible;
  public boolean selfRecursive;
  public long pid; // ID of process that called the function
}


/**
 * This class represents a function call by holding the index of the function
 * that did the calling and the index of the function that was called.
 */
class FunctionCall {
  public int caller, callee;
  public boolean ignore;
  public boolean isReturn;
}


/**
 * This graph data structure has the behavior of a radial graph. The first
 * node, which represents the first function of the run, lies in the center
 * of the graph. Function calls grow outward from the center in a quasi-
 * circular pattern. This class also contains the order of function calls
 * and logic for animating the sequence of function calls as well as panning
 * and zooming the graph and collapsing branches of the graph.
 */
public class RadialGraph {
  public List<RGNode> nodes = new ArrayList<>();
  public List<Double> scaledXs = new ArrayList<>();
  public List<Double> scaledYs = new ArrayList<>();
  public List<FunctionCall> callOrder = new ArrayList<>();
  public String currentNode;
  public int selectedNode;
  public double NODE_DIAMETER;
  
  private double SEPARATOR_LENGTH;
  private List<Double> originalXs = new ArrayList<>();
  private List<Double> originalYs = new ArrayList<>();
  private String firstFunctionName;
  
  
  public RadialGraph() {
    SEPARATOR_LENGTH = 100;
    NODE_DIAMETER = 10;
    currentNode = "<none>";
    selectedNode = -1;
  }
  
  
 /**
  * Copies all of the positions in the node data structures into the "scaled"
  * data structures, which contain positional data that accounts for the user's
  * panning and zooming.
  */
  public void allocatePositions() {
    scaledXs.clear();
    scaledYs.clear();
    for (RGNode node : nodes) {
      scaledXs.add(node.x);
      scaledYs.add(node.y);
    }
  }
  
  
 /**
  * Copies all of the current node positions into the "originals" data
  * structures, so that the view can be restored to these positions with
  * the resetView() function.
  */
  public void allocateOriginals() {
    originalXs.clear();
    originalYs.clear();
    for (RGNode node : nodes) {
      originalXs.add(node.x);
      originalYs.add(node.y);
    }
  }
  
  
 /**
  * Copies all of the current node positions into the "originals" data
  * structures, so that the view can be restored to these positions with
  * the resetView() function.
  */
  public void resetView() {
    for (int n = 0; n < nodes.size(); ++n) {
      scaledXs.set(n, originalXs.get(n));
      scaledYs.set(n, originalYs.get(n));
    }
  }
  
  
 /**
  * Calculates the positions of all the graph nodes. As this is a radial graph,
  * the nodes grow outward from the center in a series of
  * expanding concentric rings, with their angles calculated so that they are
  * about evenly spaced.
  */
  public void buildGraph(Canvas canvas) {
    determineNodeDepths();
    
    // populate rings
    List<Integer> rings = new ArrayList<>();
    int maxDepth = 0;
    for (RGNode node : nodes) {
      if (node.depth > maxDepth) maxDepth = node.depth;
    }
    for (int n = 0; n <= maxDepth; ++n) rings.add(0);
    for (int n = 0; n < nodes.size(); ++n) {
      int idx = nodes.get(n).depth;
      rings.set(idx, rings.get(idx) + 1);
    }
    
    List<Double> rangeLows = new ArrayList<>();
    List<Double> rangeHighs = new ArrayList<>();
    List<Boolean> visited = new ArrayList<>();
    
    for (int n = 0; n < nodes.size(); ++n) {
      rangeLows.add(0.0);
      rangeHighs.add(0.0);
      visited.add(false);
    }
    
    // s is our last start point index. We assume that the first function
    // in the nodes data structure is our starting point, so we set
    // s to 0.
    int s = 0;

    LinkedList<Integer> queue = new LinkedList<>();
    visited.set(s, true);
    queue.add(s);
    
    rangeLows.set(s, 0.0);
    rangeHighs.set(s, Math.PI * 2);
    
    while (queue.isEmpty()) {
      s = queue.getFirst();
      queue.pop();
      double range = rangeHighs.get(s) - rangeLows.get(s);
      int children = 0;
      for (int n = 0; n < nodes.get(s).callees.size(); ++n) {
        int idx = nodes.get(s).callees.get(n);
        if (!visited.get(idx) && nodes.get(idx).depth > nodes.get(s).depth) ++children;
      }
      double gap = range / (double)(children+1);
      double angle = rangeLows.get(s);
      
      for (int n = 0; n < nodes.get(s).callees.size(); ++n) {
        int idx = nodes.get(s).callees.get(n);
        if (!visited.get(idx)) {
          queue.add(idx);
          visited.set(idx, true);
          if (nodes.get(idx).depth > nodes.get(s).depth) {
            rangeLows.set(idx, angle);
            nodes.get(idx).angle = angle;
            rangeHighs.set(idx, angle + gap);
            angle += gap;
          }
        }
      }
    }
    
    // now figure out coordinates
    for (RGNode node : nodes) {
      node.x = SEPARATOR_LENGTH * node.depth * Math.cos(node.angle);
      node.y = SEPARATOR_LENGTH * node.depth * Math.sin(node.angle);
    }
    
    for (RGNode node : nodes) node.collapsed = false;
    allocatePositions();
    recenter(0, 0, canvas);
    allocateOriginals();

  }
  
  
 /**
  * Gets the "depth" of each node in the graph; i.e. their distance from
  * the center node.
  */
  private void determineNodeDepths() {
    for (int n = 0; n < nodes.size(); ++n) {
      nodes.get(n).depth = distanceBetweenNodes(n, firstFunctionName);
    }
  }
  
  
 /**
  * Performs a breadth-first search to determine the (shortest) distance
  * between two nodes in the graph.
  *
  * \param start The starting point is identified by its index
  * \param end   The ending point is identified by the name of the
  *              function represented by that node
  * \return      The (shortest) distance between the two nodes
  */
  private int distanceBetweenNodes(int start, String end) {
  
    if (nodes.get(start).name.equals(end)) return 0;
    
    int s = start;
    List<Boolean> visited = new ArrayList<>();
    for (RGNode node : nodes) visited.add(false);
    LinkedList<Integer> queue = new LinkedList<>();
    visited.set(s, true);
    queue.add(s);
    
    int distance = 0;
    
    while (!queue.isEmpty()) {
      ++distance;
      s = queue.getFirst();
      queue.pop();
      for (int n = 0; n < nodes.get(s).callers.size(); ++n) {
        int idx = nodes.get(s).callers.get(n);
        if (idx != -1 && !visited.get(idx)) {
          if (nodes.get(idx).name.equals(end)) return distance;
          visited.set(idx, true);
          queue.add(idx);
        }
      }
    }
    
    return -1; // error indication: destination not reachable from start
    
  } // distanceBetweenNodes()
  
  
 /**
  * Recenters the graph on the provided X and Y positions.
  *
  * \param x      Horizontal pixel position in the program window to recenter
  *               the graph on
  * \param y      Vertical pixel position in the program window to recenter
  *               the graph on
  * \param canvas The draw area, which we need to get its width and height
  *               for the recentering calculation
  */
  public void recenter(int x, int y, Canvas canvas) {
    double offsetX = canvas.getWidth() / 2.0 - x;
    double offsetY = canvas.getHeight() / 2.0 - y;
    for (int n = 0; n < nodes.size(); ++n) {
      scaledXs.set(n, scaledXs.get(n) + offsetX);
      scaledYs.set(n, scaledYs.get(n) + offsetY);
    }
  }
  
  
 /**
  * Zooms in on the graph. Zooming in and out is actually 'simulated' by
  * changing the positions of the graph nodes rather than tracking zoom
  * level separately.
  *
  * \param canvas The drawing area, which we need to get its width and height
  *               for the zooming calculation
  */
  public void zoomIn(Canvas canvas) {
    // Get every node's offset from the center of the window and
    // multiply it by 1.25 which effectively simulates zooming in
    for (int n = 0; n < nodes.size(); ++n) {
      double xOffset = scaledXs.get(n) - canvas.getWidth() / 2.0;
      double yOffset = scaledYs.get(n) - canvas.getHeight() / 2.0;
      scaledXs.set(n, canvas.getWidth() / 2.0 + (xOffset * 1.25));
      scaledYs.set(n, canvas.getHeight() / 2.0 + (yOffset * 1.25));
    }

  }


 /**
  * Zooms out from the graph. Zooming in and out is actually 'simulated' by
  * changing the positions of the graph nodes rather than tracking zoom
  * level separately.
  *
  * \param canvas The drawing area, which we need to get its width and height
  *               for the zooming calculation
  */
  public void zoomOut(Canvas canvas) {
    // Get every node's offset from the center of the window and
    // multiply it by 0.8 which effectively simulates zooming out
    for (int n = 0; n < nodes.size(); ++n) {
      double xOffset = scaledXs.get(n) - canvas.getWidth() / 2.0;
      double yOffset = scaledYs.get(n) - canvas.getHeight() / 2.0;
      scaledXs.set(n, canvas.getWidth() / 2.0 + (xOffset * 0.8));
      scaledYs.set(n, canvas.getHeight() / 2.0 + (yOffset * 0.8));
    }
  }
  
  
 /**
  * Gets the node closest to a given X and Y position, and optionally selects
  * that node. Returns the actual data structure of the closest node.
  *
  * \param x          X position
  * \param y          Y position
  * \reassignSelected Indicates whether to select the closest node or not
  * \return           The node closest to the provided X and Y positions
  */
  public RGNode getClosestTo(int x, int y, boolean reassignSelected) {
    double lowestDist = Double.MAX_VALUE;
    int idx = -1;
    for (int n = 0; n < nodes.size(); ++n) {
      if (!nodes.get(n).collapsed) {
        double distance = Math.hypot(scaledXs.get(n)-x, scaledYs.get(n)-y);
        if (distance < NODE_DIAMETER && distance < lowestDist) {
          lowestDist = distance;
          idx = n;
        }
      }
    }
    if (idx == -1) {
      RGNode none = new RGNode();
      none.name = "<none>";
      return none;
    }
    if (reassignSelected) selectedNode = idx;
    return nodes.get(idx);
  }
  
  
 /**
  * Gets the node closest to a given X and Y position, and optionally selects
  * that node. Returns the index of the closest node.
  *
  * \param x          X position
  * \param y          Y position
  * \reassignSelected Indicates whether to select the closest node or not
  * \return           The node index closest to the provided X and Y positions
  */
  public int getClosestToIdx(int x, int y, boolean reassignSelected) {
    double lowestDist = Double.MAX_VALUE;
    int idx = -1;
    for (int n = 0; n < nodes.size(); ++n) {
      if (!nodes.get(n).collapsed) {
        double distance = Math.hypot(scaledXs.get(n)-x, scaledYs.get(n)-y);
        if (distance < NODE_DIAMETER && distance < lowestDist) {
          lowestDist = distance;
          idx = n;
        }
      }
    }
    if (idx == -1) return idx;
    if (reassignSelected) selectedNode = idx;
    return idx;
  }
  
  
 /**
  * Given the provided node, this function either collapses or uncollapses the
  * branch of the graph originating from that node.
  *
  * \param node Starting point of the branch to collapse or uncollapse.
  */
  public void toggleCollapse(int node) {
    // First, determine whether to collapse or uncollapse the branch.
    boolean collapse = collapseBranch(node);
    
    int s = node;
    List<Boolean> visited = new ArrayList<>();
    for (int n = 0; n < nodes.size(); ++n) visited.add(false);
    LinkedList<Integer> queue = new LinkedList<>();
    visited.set(s, true);
    queue.add(s);
    
    while (!queue.isEmpty()) {
      s = queue.getFirst();
      queue.pop();
      for (int n = 0; n < nodes.get(s).callees.size(); ++n) {
        int idx = nodes.get(s).callees.get(n);
        if (!visited.get(idx) && nodes.get(idx).name.equals(firstFunctionName)) {
          visited.set(idx, true);
          queue.add(idx);
          nodes.get(idx).collapsed = collapse;
        }
      }
    }
  }
  
  
  
 /**
  * Determines whether a branch originating at the given node should be
  * collapsed or uncollapsed. If any of the nodes on the branch are uncollapsed,
  * we assume the desired behavior is to collapse everything. If all the nodes
  * on the branch are already collapsed, we want to uncollapse them.
  *
  * \param node Starting point of the branch to check
  * \return     True if the branch should be collapsed, false if it should be
  *             uncollapsed
  */
  private boolean collapseBranch(int node) {
    int s = node;
    List<Boolean> visited = new ArrayList<>();
    for (int n = 0; n < nodes.size(); ++n) visited.add(false);
    LinkedList<Integer> queue = new LinkedList<>();
    visited.set(s, true);
    queue.add(s);
    
    while (!queue.isEmpty()) {
      s = queue.getFirst();
      queue.pop();
      for (int n = 0; n < nodes.get(s).callees.size(); ++n) {
        int idx = nodes.get(s).callees.get(n);
        if (!visited.get(idx) && nodes.get(idx).name.equals(firstFunctionName)) {
          visited.set(idx, true);
          queue.add(idx);
          if (!nodes.get(idx).collapsed) return true;
        }
      }
    }
    
    return false;
  }
  
  
  public void resetGraph() {
    nodes.clear();
    callOrder.clear();
  }
  
  
  public void populateFunctions(List<String> functions) {
    if (functions.size() == 0) return;
    firstFunctionName = functions.get(0);
    for (String s : functions) {
      RGNode node = new RGNode();
      node.name = s;
      node.selfRecursive = false;
      nodes.add(node);
    }
  }
  
  
  // Format:
  // *I* = ignore
  // *R* = return
  // otherwise assume it's a function name
  public void populateCallOrder(List<String> events, Canvas canvas) {
  
    boolean firstCall = true;
    LinkedList<Integer> functionIndices = new LinkedList<>();
    int previousFunctionIdx = -1;
    String previousFunctionName = "<noneyet>";
  
    for (String event : events) {
      if (event.equals("*I*")) {
        FunctionCall call = new FunctionCall();
        call.ignore = true;
        callOrder.add(call);
        continue;
      }
      else if (event.equals("*R*")) {
        FunctionCall call = new FunctionCall();
        call.ignore = false;
        call.isReturn = true;
        call.caller = functionIndices.pop();
        call.callee = functionIndices.peek();
        callOrder.add(call);
        
        previousFunctionIdx = functionIndices.peek();
        continue;
      }
      else {
        String functionName = event;
        int functionIdx = getIndexOfFunction(functionName);
        functionIndices.add(functionIdx);
        
        if (firstCall) {
          firstCall = false;
          FunctionCall call = new FunctionCall();
          call.ignore = false;
          call.caller = -1;
          previousFunctionIdx = call.callee = functionIdx;
          callOrder.add(call);
          previousFunctionName = functionName;
          continue;
        }
        
        if (functionName.equals(previousFunctionName)) nodes.get(functionIdx).selfRecursive = true;
        
        FunctionCall call = new FunctionCall();
        call.ignore = false;
        call.isReturn = false;
        call.caller = previousFunctionIdx;
        call.callee = functionIdx;
        callOrder.add(call);
        
        if (!functionContainsCallee(previousFunctionIdx, functionIdx))
          nodes.get(previousFunctionIdx).callees.add(functionIdx);
        if (!functionContainsCaller(functionIdx, previousFunctionIdx))
          nodes.get(functionIdx).callers.add(previousFunctionIdx);
          
        previousFunctionIdx = functionIdx;
        previousFunctionName = functionName;
      }
    } // End loop through events
    
    currentNode = nodes.get(nodes.size()-1).name;
    
//    buildGraph(canvas);
    
  } // populateCallOrder()
  
  
  
  private int getIndexOfFunction(String name) {
    for (int n = 0; n < nodes.size(); ++n) {
      if (nodes.get(n).name.equals(name)) return n;
    }
    return -1;
  }
  
  
  private boolean functionContainsCallee(int functionIdx, int calleeIdx) {
    RGNode node = nodes.get(functionIdx);
    for (int n = 0; n < node.callees.size(); ++n) {
      if (node.callees.get(n) == calleeIdx) return true;
    }
    return false;
  }
  
  
  private boolean functionContainsCaller(int functionIdx, int callerIdx) {
    RGNode node = nodes.get(functionIdx);
    for (int n = 0; n < node.callers.size(); ++n) {
      if (node.callers.get(n) == callerIdx) return true;
    }
    return false;
  }


  
}
