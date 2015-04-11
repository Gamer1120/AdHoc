package com.procoder.routing.protocol;

import com.procoder.routing.client.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RIPRoutingProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;
    private ConcurrentHashMap<Integer, BasicRoute> forwardingTable = new ConcurrentHashMap<>();
    private boolean changed = false;
    private Set<Integer> localLinks = new HashSet<>();

    public void init(LinkLayer linkLayer) {
        System.out.println("Init node:" + linkLayer.getOwnAddress());
        this.linkLayer = linkLayer;

        forwardingTable.put(this.linkLayer.getOwnAddress(), new BasicRoute(this.linkLayer.getOwnAddress(), 0, 0, new Integer[0]));

        // First, send a broadcast packet (to address 0), with distance vector
        Packet discoveryBroadcastPacket = new Packet(this.linkLayer.getOwnAddress(), 0, getDistanceVectorTable());
        this.linkLayer.transmit(discoveryBroadcastPacket);
    }

    public void run() {
        int cycles = 0;
        try {
            while (true) {
                // Try to receive a packet
                Packet packet = this.linkLayer.receive();
                if (packet != null) {
                    System.out.println("Received packet!");
                    // Add source to forwardingtable, if distance less than current
                    int sender = packet.getSourceAddress();

                    processDV(sender, packet.getData());

                }

                if (cycles % 15 == 0) {
                    // Checks all directly connected nodes and update forwardingtable if necessary
                    for (ConcurrentHashMap.Entry<Integer, BasicRoute> entry : forwardingTable.entrySet()) {
                        int newCostToNext = linkLayer.getLinkCost(entry.getValue().nextHop);
                        newCostToNext = newCostToNext == -1 ? 100000 : newCostToNext;
                        int oldCostToNext = entry.getValue().costToNext;
                        if (linkLayer.getOwnAddress() != entry.getKey() && newCostToNext != oldCostToNext) {
                            changed = true;
                            int newCost = entry.getValue().distance - oldCostToNext + newCostToNext;
                            BasicRoute newRoute = entry.getValue();
                            newRoute.distance = newCost;
                            newRoute.costToNext = newCostToNext;
                            forwardingTable.put(entry.getKey(), newRoute);

                            System.out.println("Updating table: link cost to " + entry.getValue().nextHop + " changed, old: " + oldCostToNext + " new:" + newCostToNext);

                        }
                    }

                    for (int addr = 1; addr < 7; addr++) {
                        int cost = linkLayer.getLinkCost(addr);
                        if (cost != -1 && !localLinks.contains(addr)) {
                            localLinks.add(addr);
                            changed = true;
                            System.out.println("Link to " + addr + " added");
                        }
                        if (cost == -1) {
                            localLinks.remove(addr);
                        }
                    }
                }

                if (cycles % 30 == 0) {
                    if (changed) {
                        Packet discoveryBroadcastPacket = new Packet(this.linkLayer.getOwnAddress(), 0, getDistanceVectorTable());
                        this.linkLayer.transmit(discoveryBroadcastPacket);
                    }
                    changed = false;
                }

                if(cycles % 450 == 0) {
                    Packet discoveryBroadcastPacket = new Packet(this.linkLayer.getOwnAddress(), 0, getDistanceVectorTable());
                    this.linkLayer.transmit(discoveryBroadcastPacket);
                }

                Thread.sleep(10);
                cycles++;
            }
        } catch (InterruptedException e) {
            // We were interrupted, stop execution of the protocol
        }
    }

    public ConcurrentHashMap<Integer, BasicRoute> getForwardingTable() {
        return this.forwardingTable;
    }

    public DataTable getDistanceVectorTable() {

        DataTable DVTable = new DataTable(20);
        Integer[] empty = new Integer[20];
        Arrays.fill(empty, -1);

        int row = 0;
        for (ConcurrentHashMap.Entry<Integer, BasicRoute> entry : this.forwardingTable.entrySet()) {
            DVTable.setRow(row, empty);
            DVTable.set(row, 0, entry.getKey());
            DVTable.set(row, 1, entry.getValue().distance);
            Integer[] route = entry.getValue().route;

            for (int i = 0; route != null && i < route.length; i++) {
                DVTable.set(row, i + 2, route[i]);
            }
            row++;
        }

        return DVTable;
    }

    public void processDV(int sourceAddress, DataTable dV) {
        int sourceCost = linkLayer.getLinkCost(sourceAddress);

        for (int i = 0; i < dV.getNRows(); i++) {
            Integer[] row = dV.getRow(i);
            int dest = row[0];
            int cost = row[1] + sourceCost;
            LinkedList<Integer> route = new LinkedList<>();
            for (int col = 2; col < 20; col++) {
                int val = dV.get(i, col);
                if (val == -1) {
                    break;
                }
                route.add(val);
            }
            route.addFirst(sourceAddress);

            BasicRoute currentRoute = forwardingTable.get(dest);

            if (currentRoute != null && currentRoute.nextHop == sourceAddress && currentRoute.distance != cost) {
                forwardingTable.put(dest, new BasicRoute(sourceAddress, cost, sourceCost, currentRoute.route));
                System.out.println("Updating cost: dest: " + dest + " cost: " + cost + " next hop: " + sourceAddress);
                changed = true;


            }

            if (currentRoute == null || (cost < currentRoute.distance && !route.contains(linkLayer.getOwnAddress()))) {
                System.out.println("Adding route: dest: " + dest + " cost: " + cost + " next hop: " + sourceAddress);
                forwardingTable.put(dest, new BasicRoute(sourceAddress, cost, sourceCost, route.toArray(new Integer[route.size()])));
                changed = true;
            }
        }


    }


}
