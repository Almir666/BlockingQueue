import java.util.concurrent.ArrayBlockingQueue;

public class BlockingQueue<T> {
    private final Object lock = new Object();
    private final T[] queue;
    private int capacity;
    private int size;
    private int head;
    private int tail;

public BlockingQueue(int capacity) {
    if (capacity <= 0) {
        throw new IllegalArgumentException();
    }
    this.capacity = capacity;
    queue = (T[])new Object[capacity];
}

    public void enqueue(T el) throws InterruptedException {
        synchronized (lock) {
            while (size == queue.length) {
                lock.wait();
            }

            queue[tail] = el;
            tail = (tail + 1) % capacity;
            size++;
            lock.notifyAll();
        }
    }

    public T dequeue() throws InterruptedException {
        synchronized (lock) {
            if (size == 0) {
                lock.wait();
            }
            T el = queue[head];
            head = (head + 1) % capacity;
            size--;
            lock.notifyAll();

            return el;
        }
    }

    public int size() {
        synchronized (lock) {
            return size;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue = new BlockingQueue<>(4);

        Runnable taskProduce = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        System.out.println("Producing :" + i);
                        queue.enqueue(i);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Runnable taskConsume = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 9; i++) {
                    try {
                        int el = queue.dequeue();
                        System.out.println("Consuming: " + el);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };

        Thread producer = new Thread(taskProduce);
        Thread consumer = new Thread(taskConsume);

        System.out.println(queue.size);
        producer.start();
        consumer.start();

        producer.join();
        consumer.join();
        System.out.println(queue.size);
    }
}
