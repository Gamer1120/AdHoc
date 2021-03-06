package com.procoder.routing.protocol;

import com.procoder.routing.client.*;
import com.procoder.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RIPRoutingProtocol implements IRoutingProtocol {
    public static final Logger LOGGER = LoggerFactory.getLogger(RIPRoutingProtocol.class);


    private LinkLayer linkLayer;
    private ConcurrentHashMap<Inet4Address, BasicRoute> forwardingTable = new ConcurrentHashMap<>();
    private boolean changed = false;
    private Set<Integer> localLinks = new HashSet<>();

    private boolean running = true;

    public void init(LinkLayer linkLayer) {
        LOGGER.debug("Init node:" + linkLayer.getOwnAddress());
        this.linkLayer = linkLayer;

        forwardingTable.put(this.linkLayer.getOwnAddress(), new BasicRoute(this.linkLayer.getOwnAddress(), this.linkLayer.getOwnAddress(), (byte) 0, (byte) 0, new Inet4Address[0]));

        // First, send a broadcast packet (to address 0), with distance vector
        Packet discoveryBroadcastPacket = new Packet(this.linkLayer.getOwnAddress(), NetworkUtils.getBroadcastAddress(), getDistanceVectorTable());
        this.linkLayer.transmit(discoveryBroadcastPacket);
    }

    public void run() {
        int cycles = 0;
        try {
            while (running) {
                // Try to receive a packet
                Packet packet = this.linkLayer.receive();
                if (packet != null) {
                    LOGGER.debug("Received packet!");
                    // Add source to forwardingtable, if distance less than current
                    Inet4Address sender = packet.getSourceAddress();

                    processDV(sender, packet.getData());

                }

                if (cycles % 15 == 0) {
                    // Checks all directly connected nodes and update forwardingtable if necessary
                    for (ConcurrentHashMap.Entry<Inet4Address, BasicRoute> entry : forwardingTable.entrySet()) {
                        byte newCostToNext = linkLayer.getLinkCost(entry.getValue().nextHop);
                        newCostToNext = newCostToNext == -1 ? 127 : newCostToNext;
                        byte oldCostToNext = entry.getValue().costToNext;
                        if (linkLayer.getOwnAddress() != entry.getKey() && newCostToNext != oldCostToNext) {
                            changed = true;
                            int tempNewCost = entry.getValue().distance - oldCostToNext + newCostToNext;
                            byte newCost = (byte) tempNewCost;
                            if (tempNewCost > Byte.MAX_VALUE) {
                                newCost = Byte.MAX_VALUE;
                            } else if (tempNewCost < 0) {
                                newCost = 0;
                            }
                            BasicRoute newRoute = entry.getValue();
                            newRoute.distance = newCost;
                            newRoute.costToNext = newCostToNext;
                            forwardingTable.put(entry.getKey(), newRoute);

                            LOGGER.debug("Updating table: link cost to " + entry.getValue().nextHop + " changed, old: " + oldCostToNext + " new:" + newCostToNext);

                        }
                    }

                    // TODO Uitzoeken of detectie van hosts hier nodig is
                }

                if (cycles % 30 == 0) {
                    if (changed) {
                        Packet discoveryBroadcastPacket = new Packet(this.linkLayer.getOwnAddress(), NetworkUtils.getBroadcastAddress(), getDistanceVectorTable());
                        this.linkLayer.transmit(discoveryBroadcastPacket);
                    }
                    changed = false;
                }

                if(cycles % 450 == 0) {
                    Packet discoveryBroadcastPacket = new Packet(this.linkLayer.getOwnAddress(), NetworkUtils.getBroadcastAddress(), getDistanceVectorTable());
                    this.linkLayer.transmit(discoveryBroadcastPacket);

                    LOGGER.debug("Huidige forwardingtable {}", forwardingTable);
                }

                Thread.sleep(10);
                cycles++;
            }
        } catch (InterruptedException e) {
            // We were interrupted, stop execution of the protocol
        }
    }

    public ConcurrentHashMap<Inet4Address, BasicRoute> getForwardingTable() {
        return new ConcurrentHashMap<>(this.forwardingTable);
    }

    @Override
    public void stop() {
        running = false;
    }

    public DVTable getDistanceVectorTable() {

        DVTable dvTable = new DVTable(forwardingTable.values());
        return dvTable;
    }

    public void processDV(Inet4Address sourceAddress, DVTable dV) {
        byte sourceCost = linkLayer.getLinkCost(sourceAddress);

        for(BasicRoute newRoute : dV) {
            LinkedList<Inet4Address> route = new LinkedList<>(Arrays.asList(newRoute.path));
            route.addFirst(sourceAddress);

            byte newDistance = newRoute.distance;
            if (newDistance != Byte.MAX_VALUE) {
                newDistance++;
            }
            Inet4Address dest = newRoute.destination;

            BasicRoute currentRoute = forwardingTable.get(dest);

            if (currentRoute != null && currentRoute.nextHop == sourceAddress && currentRoute.distance != newDistance) {
                forwardingTable.put(dest, new BasicRoute(dest, sourceAddress, newDistance, sourceCost, currentRoute.path));
                LOGGER.debug("Updating cost: dest: " + dest + " cost: " + newDistance + " next hop: " + sourceAddress);
                changed = true;


            }

            if (currentRoute == null || (newDistance < currentRoute.distance && !route.contains(linkLayer.getOwnAddress()))) {
                LOGGER.debug("Adding path: dest: " + dest + " cost: " + newDistance + " next hop: " + sourceAddress);
                forwardingTable.put(dest, new BasicRoute(dest, sourceAddress, newDistance, sourceCost, route.toArray(new Inet4Address[route.size()])));
                changed = true;
            }
        }




    }


}
