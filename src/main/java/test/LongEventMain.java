package test;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import event.LongEvent;
import factory.LongEventFactory;
import handler.LongEventHandler;
import producer.LongEventProducer;
import producer.LongEventProducerWithTranslator;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Author:shenqin
 * version: V1.0
 * Date: 2017/7/12
 * Time: 14:03
 * Description:
 */
public class LongEventMain {

    private static final int THREAD_NUM = 16;

    public static void main(String[] args) throws InterruptedException {
        // 触发 Consumer 的事件处理
        Executor executor = Executors.newCachedThreadPool();
        // The factory for the event
        EventFactory<LongEvent> factory = new LongEventFactory();
        // RingBuffer 大小，必须是 2 的 N 次方
        int bufferSize = 1024;
        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(factory, bufferSize, executor, ProducerType.SINGLE,
                new YieldingWaitStrategy());
//        disruptor.handleEventsWith(new LongEventHandler());
        //多个消费者处理相同的event
//        disruptor.handleEventsWith(new LongEventHandler(),new LongEventHandler(),new LongEventHandler());

        //多个消费者处理不同的event
        LongEventHandler[] eventDisruptorConsumers = new LongEventHandler[THREAD_NUM];
        for (int i = 0; i < THREAD_NUM; i++) {
            eventDisruptorConsumers[i] = new LongEventHandler();
        }
        disruptor.handleEventsWithWorkerPool(eventDisruptorConsumers);

        disruptor.start();
        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();


//        LongEventProducer producer = new LongEventProducer(ringBuffer);

        LongEventProducerWithTranslator producer=new LongEventProducerWithTranslator(ringBuffer);

        for (long i = 0; i<16; i++) {
            System.out.println("事件 "+i);
            producer.onData(i+"");
//            Thread.sleep(3000);
        }
//        disruptor.shutdown();
    }
}
