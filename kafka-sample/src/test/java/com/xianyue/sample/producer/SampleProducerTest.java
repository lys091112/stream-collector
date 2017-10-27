package com.xianyue.sample.producer;

import org.apache.kafka.streams.kstream.TimeWindows;
import org.junit.Test;

public class SampleProducerTest {

    @Test
    public void window() throws Exception {

        TimeWindows timeWindow = TimeWindows.of(10);

    }

}
