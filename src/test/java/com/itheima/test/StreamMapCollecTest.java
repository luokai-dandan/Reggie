package com.itheima.test;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamMapCollecTest {

    @Test
    public void test(){
        List<Integer> num = Arrays.asList(1,2,3,4,5);
        List<Integer> collect1 = num.stream().map((item)->{
            item = item*2;
            return item;
        }).collect(Collectors.toList());
        System.out.println(collect1); //[2, 4, 6, 8, 10]
    }
}
