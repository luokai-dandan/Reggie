package com.itheima.test;

import org.junit.jupiter.api.Test;

public class UploadFileTest {
    @Test
    public void test01(){
        String fileName = "exa.fga.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }
}
