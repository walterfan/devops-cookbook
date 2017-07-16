package com.github.walterfan.kanban.domain;

/**
 * The Class Node.
 *
 * @author walter
 *
 */
public class Node extends BaseObject {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8361288889254536362L;

    /** The node id. */
    private int nodeID;
    
    /** The name. */
    private String name;
    
    /** The node host. */
    private String nodeHost;

    /**
     * Instantiates a new node.
     */
    public Node() {
        super();
    }

    /**
     * Instantiates a new node.
     *
     * @param nodeID the node id
     * @param name the name
     */
    public Node(int nodeID, String name) {
        super();
        this.nodeID = nodeID;
        this.name = name;
    }

    /**
     * Instantiates a new node.
     *
     * @param name the name
     */
    public Node(String name) {
        super();
        this.name = name;
    }

    /**
     * Gets the node id.
     *
     * @return the node id
     */
    public int getNodeID() {
        return nodeID;
    }

    /**
     * Sets the node id.
     *
     * @param nodeID the new node id
     */
    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    /**
     * Gets the node host.
     *
     * @return the node host
     */
    public String getNodeHost() {
        return nodeHost;
    }

    /**
     * Sets the node host.
     *
     * @param nodeHost the new node host
     */
    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

}
