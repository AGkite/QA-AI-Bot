package com.newone.ai.robot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SpringBootTest
class AiRobotSpringbootApplicationTests {

    @Test
    void contextLoads() {
        String[] array = new String[] {"ab", "2"};
//        System.out.println(joinStr(array, "&&"));

//        System.out.println(Arrays.toString(splitStr("ab&&2", "&&")));

//        int[] nums = new int[1000];
//        for (int i = 0; i <= 998 ; i++) {
//            nums[i] = i;
//        }
//        nums[999] = 1;
//        System.out.println(findNum(nums));
//        System.out.println(findNum2(nums));
//        System.out.println(getList());
        getList();
    }

    public static String joinStr(String[] array) {
        List<String> list = Arrays.asList(array);
        return String.join("&&", list);
    }

//    public static String joinStr(String[] str, String separ) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < str.length; i++) {
//            if (i > 0) {
//                sb.append(separ);
//            }
//            sb.append(str[i]);
//        }
//        return sb.toString();
//    }

    public static String joinStr(String[] array, String separ) {
        return addStr(array, separ, 0);
    }
    public static String addStr(String[] array, String separ, int index) {
        if (index == array.length - 1) {
            return array[index];
        }
        return array[index] + separ + addStr(array, separ, index + 1);
    }

    public static String[] splitStr(String str) {
        return str != null ? str.split("&&") : new String[] {};
    }

    public static String[] splitStr(String str, String separ) {
        List<String> list = new ArrayList<>();
        int start = 0, end = 0;
        while((end = str.indexOf(separ, start)) != -1) {
            list.add(str.substring(start, end));
            start = end + separ.length();
        }
        list.add(str.substring(start));
        return list.toArray(new String[0]);
    }

    public static int findNum(int[] nums) {
        int index = 0;
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            if (set.contains(nums[i])) {
                index = i;
                break;
            } else {
                set.add(nums[i]);
            }
        }
        return nums[index];
    }

    public static int findNum2(int[] nums) {
        int n = nums.length;
        int expectSum = nums[0] * n + n * (n - 1) / 2;

        int sum = 0;
        for(int num : nums) {
            sum += num;
        }

        System.out.println(sum);
        System.out.println(expectSum);
        return Math.abs(sum - expectSum);
    }

    public static List<String> getList() {
        List<String> list = new ArrayList<>();
        for (char i = 'a'; i <= 'z'; i++) {
            String str = "";
            for (char j = i; j <= 'z'; j++) {
                str += j;
//                list.add(str);
                System.out.println(str);
            }
        }
        return list;
    }

    public static void main(String[] args) throws InterruptedException{

        AtomicInteger count = new AtomicInteger();
        final Object obj = new Object();

        Thread t1 = new Thread(() -> {
            synchronized(obj) {
                try {
                    obj.wait();
                    while (count.get() <= 200) {
                        System.out.println(count.get());
                        count.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    obj.notifyAll();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized(obj) {
                try {
                    obj.wait();
                    while (count.get() <= 200) {
                        System.out.println(count.get());
                        count.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    obj.notifyAll();
                }
            }
        });

        t1.start();
        t2.start();

        Thread.sleep(100);
        synchronized (obj) {
            obj.notify();
        }


        t1.join();
        t2.join();

    }
}
