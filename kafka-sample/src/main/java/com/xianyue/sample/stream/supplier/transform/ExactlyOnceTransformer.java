package com.xianyue.sample.stream.supplier.transform;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;

@Slf4j
public class ExactlyOnceTransformer implements Transformer<Windowed<String>, Long, KeyValue<Windowed<String>, Long>> {

    //    TreeMap<Windowed<String>, Long> datas = new TreeMap<>(Comparator.comparingLong(o -> o.window().start())); #
    // 线程不安全
    ConcurrentSkipListMap<Windowed<String>, Long> datas = new ConcurrentSkipListMap<>(
        Comparator.comparingLong(o -> o.window().start()));

    private Long hasSendTime = 0L;

    @Override
    public void init(ProcessorContext context) {
        context.schedule(20000, PunctuationType.WALL_CLOCK_TIME, new Punctuator() {
            @Override
            public void punctuate(long timestamp) {
                if (datas.isEmpty()) {
                    return;
                }

                Long now = System.currentTimeMillis();
                Iterator<Map.Entry<Windowed<String>, Long>> iterator = datas.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Windowed<String>, Long> entry = iterator.next();
                    if (entry.getKey().window().end() < now) {
                        log.info("send info --------- key:{}, value:{}", entry.getKey(), entry.getValue());
                        context.forward(entry.getKey(), entry.getValue());
                        hasSendTime = entry.getKey().window().end();
                        iterator.remove();
                    } else {
                        //保存的数据还不到发送时间
                        break;
                    }
                }
            }
        });
    }

    @Override
    public KeyValue<Windowed<String>, Long> transform(Windowed<String> key, Long value) {
        if (key.window().end() >= hasSendTime) {
            datas.put(key, value);
        } else {
            log.warn("Invaild data! windowEnd={}, hasSend:{}", key, hasSendTime);
        }
        return null;
    }

    @Override
    public KeyValue<Windowed<String>, Long> punctuate(long timestamp) {
        return null;
    }

    @Override
    public void close() {

    }

}
