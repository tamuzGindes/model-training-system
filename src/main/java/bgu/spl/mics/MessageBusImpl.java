package bgu.spl.mics;

import bgu.spl.mics.application.messages.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
    private AtomicInteger numberOfThreads = new AtomicInteger(0);
    private AtomicInteger numberOfTerminations = new AtomicInteger(0);
    private static MessageBusImpl messageBus = null;
    private static boolean isDone = false;
    private ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>> serviceQueue;
    private ConcurrentHashMap<Class<? extends Message>, LinkedBlockingDeque<MicroService>> eventsServices;
    private ConcurrentHashMap<Class<? extends Message>, LinkedBlockingDeque<MicroService>> broadcastsServices;
    private ConcurrentHashMap<Event, Future> results;

    private MessageBusImpl() {
        serviceQueue = new ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>>();
        eventsServices = new ConcurrentHashMap<Class<? extends Message>, LinkedBlockingDeque<MicroService>>();
        broadcastsServices = new ConcurrentHashMap<Class<? extends Message>, LinkedBlockingDeque<MicroService>>();
        results = new ConcurrentHashMap<Event, Future>();
    }

    public static MessageBusImpl getInstance() {
        if (!isDone) {
            synchronized (MessageBusImpl.class) {
                if (!isDone) {
                    messageBus = new MessageBusImpl();
                    isDone = true;
                }
            }
        }
        return messageBus;
    }


    @Override
    public synchronized <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        if (serviceQueue.containsKey(m)) {
            if (eventsServices.containsKey(type)) {
                eventsServices.get(type).add(m);
            } else {
                LinkedBlockingDeque<MicroService> queue = new LinkedBlockingDeque<MicroService>();
                queue.add(m);
                eventsServices.put(type, queue);
            }
        }
    }

    @Override
    public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        if (serviceQueue.containsKey(m)) {
            if (broadcastsServices.containsKey(type)) {
                broadcastsServices.get(type).add(m);
            } else {
                LinkedBlockingDeque<MicroService> queue = new LinkedBlockingDeque<MicroService>();
                queue.add(m);
                broadcastsServices.put(type, queue);
            }
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        if (results.containsKey(e)) {
            Future<T> fut = results.get(e);
            fut.resolve(result);
            synchronized (fut) {
                fut.notifyAll();
            }
        }

    }

    @Override
    public synchronized void sendBroadcast(Broadcast b) {
        if (broadcastsServices.containsKey(b.getClass())) {
            LinkedBlockingDeque<MicroService> queueOfSubscribed = broadcastsServices.get(b.getClass());
            if (b.getClass().equals(TerminateBroadcast.class)) {
                for (MicroService m : queueOfSubscribed) {
                    serviceQueue.get(m).addFirst(new TerminateBroadcast());
                }
            } else {
                for (MicroService m : queueOfSubscribed) {
                    serviceQueue.get(m).add(b);
                }
            }
        }
    }

    @Override
    public synchronized <T> Future<T> sendEvent(Event<T> e) {
        if (eventsServices.containsKey(e.getClass())) {
            LinkedBlockingDeque<MicroService> queueOfSubscribed = eventsServices.get(e.getClass());
            if (!queueOfSubscribed.isEmpty()) {
                MicroService m = queueOfSubscribed.poll();
                serviceQueue.get(m).add(e);
                queueOfSubscribed.add(m);
                Future<T> future = new Future<T>();
                results.put(e, (future));
                return future;
            }
        }
        return null;
    }

    @Override
    public void register(MicroService m) {
        LinkedBlockingDeque<Message> queue = new LinkedBlockingDeque<Message>();
        if (!serviceQueue.containsKey(m)) serviceQueue.put(m, queue);
    }

    @Override
    public void unregister(MicroService m) {
        if (!serviceQueue.containsKey(m)) serviceQueue.remove(m);

    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        if (serviceQueue.containsKey(m)) {
            return serviceQueue.get(m).take();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isMicroServiceRegistered(MicroService m) {
        return (serviceQueue.containsKey(m));
    }


}
