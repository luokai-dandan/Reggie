package com.itheima.test;

import org.junit.jupiter.api.Test;

public class DeleteDishTest {
    @Test
    public void delete(){
//        String ids="1524404755978076161,1524404657802002434";
        String ids="1524404755978076161";
//        if (ids.indexOf(",")==-1) {
//            System.out.println("id");
//        } else {
//
//            System.out.println("ids");
//        }
        String[] id = ids.split(",");
        for (String i:id) {
            System.out.println(i);
        }
        System.out.println(id.length);
    }
}
