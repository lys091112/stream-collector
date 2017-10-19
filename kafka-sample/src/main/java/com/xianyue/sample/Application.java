package com.xianyue.sample;

import com.xianyue.sample.config.SampleConfig;
import com.xianyue.sample.producer.SampleProducer;
import com.xianyue.sample.producer.Sender;
import com.xianyue.common.util.YamlConverter;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    public static void main(String[] args) throws IOException {
        SampleConfig config = YamlConverter.config(SampleConfig.class, "application.yml");

        // produecer
        Sender<String, String> producer = new SampleProducer(config.getKafka());
        try {
            for (int i = 0; i < 10; i++) {
                producer.send("hello", "this is a demo message");
            }
            producer.send("hello", "didi");
        } catch (Exception e) {
            log.error("send failed. error:{}", e);
        }finally {
            producer.close();
        }
    }
}
