package com.procoder.routing.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DVTable implements Iterable<BasicRoute> {

    private List<BasicRoute> rows;

    public DVTable() {
        rows = new ArrayList<>();
    }

    public DVTable(Collection col) {
        rows = new ArrayList<>(col);
    }

    public void addRow(BasicRoute row) {
        rows.add(row);
    }

    public BasicRoute getRow(int index) {
        return rows.get(index);
    }

    public byte[] toByteArray() {
        byte[][] byteRows = new byte[rows.size()][];

        int i = 0;
        int size = 0;
        for(BasicRoute route : this) {
            byteRows[i] = route.toByteArray();
            size += byteRows[i].length;
            i++;
        }

        ByteBuffer buf = ByteBuffer.allocate(size);
        for(byte[] row : byteRows) {
            buf.put(row);
        }

        return buf.array();


    }

    public static DVTable parseBytes(byte[] tableBytes) {
        DVTable result = new DVTable();

        ByteBuffer buf = ByteBuffer.wrap(tableBytes);
        while(buf.hasRemaining()) {
            byte nextSize = buf.get();
            byte[] nextRow = new byte[nextSize];
            for(int i=0; i < nextSize ; i++) {
                nextRow[i] = buf.get();
            }

            result.addRow(BasicRoute.parseBytes(nextRow));
        }
        return result;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<BasicRoute> iterator() {
        return rows.iterator();
    }
}
