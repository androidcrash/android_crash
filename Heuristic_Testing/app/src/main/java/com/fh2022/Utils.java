package com.fh2022.androidcrash;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class Utils {


    public boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

   
    public List<Set<Integer>> createThreadNameFromStr(String data){
        List<Set<Integer>>res=new ArrayList<>();

        String[] datas=data.trim().split(",");
        for(String singleItem:datas){
            String[] numStrs=singleItem.trim().split(" ");
            Set<Integer> tmpset=new HashSet<>();
            for (String numStr:numStrs) {
                numStr=numStr.trim();
                if (!numStr.isEmpty() && isInteger(numStr)) {

                    tmpset.add(Integer.parseInt(numStr));
                }
            }
            res.add(tmpset);
        }
        return res;
    }

    private List<Set<Integer>> combineNumAndSet(int num,List<Set<Integer>> data){
        List<Set<Integer>> res=new ArrayList<>();
        for(Set<Integer> item : data){
            Set<Integer> newitem = new HashSet<>(item);
            newitem.add(num);
            res.add(newitem);
        }
        return res;
    }

   
    public List<Set<Integer>> findallCinrange(int num){
        List<Set<Integer>>res=new ArrayList<>();
        if(num<1) return res;
        Set<Integer>base=new HashSet<>();
        base.add(1);
        res.add(base);
        if (num==1){
            return res;
        }
        for(int step=2;step<=num;step+=1){
            List<Set<Integer>>tmp=combineNumAndSet(step,res);
            res.addAll(tmp);
            Set<Integer>single=new HashSet<Integer>();
            single.add(step);
            res.add(single);
        }
       

        while (true){
            boolean flag=true;
            for(int i=1;i<res.size();i++){
                int lenCur=res.get(i).size();
                int lenPre=res.get(i-1).size();
                if(lenCur<lenPre){
                    Set<Integer> tmp=new HashSet<>(res.get(i));
                    Set<Integer> last=new HashSet<>(res.get(i-1));
                    res.set(i,last);
                    res.set(i-1,tmp);
                    flag=false;
                }
            }
            if(flag){
                break;
            }
        }
        for(int i=0;i<num;i++)
            res.remove(0);
        return res;
    }

   
    public List<Set<Integer>> findallCOfNums(List<Integer> ids,int num){
        List<Set<Integer>>res=new ArrayList<>();
        if(num<1) return res;
        if(num>ids.size()) return res;
        List<Set<Integer>>indexC= findallCinrange(ids.size());
        for(Set<Integer>indexs:indexC){
            if (indexs.size()==num){
                Set<Integer>tmpres=new HashSet<>();
                for(Integer i:indexs){
                    tmpres.add(ids.get(i - 1));
                }
                res.add(tmpres);
            }
        }
        return res;
    }

   
    public List<List<Integer>> splitThreadCrash(List<Integer> data,Set<Integer> deldata){
        List<List<Integer>>res=new ArrayList<>();

        for(Integer delNum:deldata){
            List<Integer> addList=new ArrayList<>();
            for(Integer addNum:data){
                if (!Objects.equals(addNum, delNum)){
                    addList.add(addNum);
                }
            }
            res.add(addList);
        }
        return res;
    }

}
