package com.fh2022.androidcrash;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtilsTest extends TestCase {

    public void testFindallCinrange() {

        List<Integer> datas=new ArrayList<>();
        for(int i=1;i<20;i++){
            datas.add(i);
        }
        List<Set<Integer>>res= new Utils().findallCOfNums(datas,3);
        System.out.println(res.size());
        System.out.println(res);
    }

    public void testSplitThreadCrash() {

        List<Integer> oral=new ArrayList<>();
        for(int i=1;i<=6;i++){
            oral.add(i);
        }

        Set<Integer> del=new HashSet<>();
        for(int i=3;i<=5;i++){
            del.add(i);
        }

        System.out.println( new Utils().splitThreadCrash(oral,del));
    }
}