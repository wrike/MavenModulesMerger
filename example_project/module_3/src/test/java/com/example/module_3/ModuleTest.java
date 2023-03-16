package com.example.module_3;

import org.junit.jupiter.api.Test;

/**
 * @author daniil.shylko on 16.03.2023
 */
class ModuleTest {

    @Test
    void test2Seconds() throws InterruptedException {
        Thread.sleep(2_000);
    }

    @Test
    void test3Seconds() throws InterruptedException {
        Thread.sleep(3_000);
    }

    @Test
    void test4Seconds() throws InterruptedException {
        Thread.sleep(4_000);
    }

}
