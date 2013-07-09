package com.dianping.monkeysocks.socket;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A stream buffer in producer&consumer pattern.<br>
 *
 * @author yihua.huang@dianping.com <br>
 * @date: 13-7-9 <br>
 * Time: 下午8:21 <br>
 */
public class StreamBuffer {

    private byte[] buffer;

    private int capacity;

    private int readPointer;

    private int writePointer;

    private ReentrantLock lock;

    private Condition full;

    private Condition empty;

    public static StreamBuffer allocate(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException("capacity must greater than 0");
        return new StreamBuffer(capacity);
    }

    StreamBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new byte[capacity];
        this.lock = new ReentrantLock();
        this.full = lock.newCondition();
        this.empty = lock.newCondition();
    }

    public byte read() {
        try {
            lock.lock();
            full.signal();
            if (readPointer >= writePointer) {
                empty.await();
            }
            //end
        } catch (InterruptedException e) {
        } finally {
            lock.unlock();
        }
        byte b = buffer[readPointer % capacity];
        readPointer++;
        return b;
    }

    public void write(byte b) {
        try {
            lock.lock();
            empty.signal();
            buffer[writePointer % capacity] = b;
            //end
            if (writePointer - readPointer >= capacity) {
                full.await();
            }
            writePointer++;
        } catch (InterruptedException e) {
        } finally {
            lock.unlock();
        }
    }

}
