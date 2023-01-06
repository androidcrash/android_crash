package com.fh2022.androidcrash;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class LockPair{
    private int taskId;
    private Lock selfLock;
    private Lock requireLock;

    public LockPair(int taskId, Lock selfLock, Lock requireLock) {
        this.taskId = taskId;
        this.selfLock = selfLock;
        this.requireLock = requireLock;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int  taskId) {
        this.taskId = taskId;
    }

    public Lock getSelfLock() {
        return selfLock;
    }

    public void setSelfLock(Lock selfLock) {
        this.selfLock = selfLock;
    }

    public Lock getRequireLock() {
        return requireLock;
    }

    public void setRequireLock(Lock requireLock) {
        this.requireLock = requireLock;
    }
}



class ThreadCustom extends Thread{
    private final int sleepTime=10;
    private int res; 
    private List<LockPair> lockPairs;
   
    public ThreadCustom(@NonNull String name) {
        super(name);
        this.res=-1;
        lockPairs=new ArrayList<>();
    }

    public void setLockPairs(List<LockPair> lockPairs) {
        this.lockPairs = lockPairs;
    }

    public void addLockPair(LockPair l){
        this.lockPairs.add(l);
    }

    public int getRes(){
        return res;
    }

    public List<LockPair> getLockPairs() {
        return lockPairs;
    }

    @Override
    public void run() {
        for(LockPair lp:lockPairs){
            try {
                if(lp.getSelfLock().tryLock(1, TimeUnit.SECONDS)){
                    try {
                        sleep(sleepTime);
                        if(!lp.getRequireLock().tryLock(1, TimeUnit.SECONDS)){
                            this.res=lp.getTaskId();
                        }else {
                            lp.getRequireLock().unlock();
                        }
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    lp.getSelfLock().unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


class TestCompGroup{

    private List<ThreadCustom> threadCustoms;

    public TestCompGroup(List<ThreadCustom> threadCustoms) {
        this.threadCustoms=threadCustoms;
    }

    public boolean test(){
        for(ThreadCustom threadCustom:threadCustoms){
            threadCustom.start();
        }

        for(ThreadCustom threadCustom:threadCustoms){
            try {
                threadCustom.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (ThreadCustom threadCustom:threadCustoms){
            if (threadCustom.getRes()!=-1){
                return true;
            }
        }
        return false;

    }
}


class TestCompApp{

    private int threadNum;
    private List<Set<Integer>> threadName;
    List<ThreadCustom> totalThread;
    Utils utils;

    public TestCompApp(int threadNum, List<Set<Integer>> threadName) {
        this.threadNum = threadNum;
        this.threadName = threadName;
        totalThread=new ArrayList<>();
        utils=new Utils();
        configure();
    }


    private void configure(){
       

        for(int i=0;i<threadNum;i++){
            String threadNamePrex="Test Process";
            ThreadCustom tmp=new ThreadCustom(threadNamePrex+(i+1));
            totalThread.add(tmp);
        }
        
        for (int i=0;i< this.threadName.size();i++){
            Set<Integer> threadPairs=threadName.get(i);
            int threadPairNums=threadPairs.size();
           
            LockPair[] lockPairs=new LockPair[threadPairNums];
            Lock[] locks=new Lock[threadPairNums];
           
            for (int j=0;j<threadPairNums;j++){
                locks[j] = new ReentrantLock();
            }
            for (int j=0;j<threadPairNums;j++){
                lockPairs[j]=new LockPair(i+1,locks[j],locks[(j+1)%threadPairNums]);
            }
            int indexPairsAll=0;
            for(Integer threadId:threadPairs){
                ThreadCustom tmp=totalThread.get(threadId-1);
                tmp.addLockPair(lockPairs[indexPairsAll]);
                totalThread.set(threadId-1,tmp);
                indexPairsAll++;
            }
        }
    }

    
    private void reset(){
        this.totalThread.clear();
        this.configure();
    }

    private  List<List<ThreadCustom>> createThreadByNum(List<List<Integer>> nums){
        List<List<ThreadCustom>>res=new ArrayList<>();
        for(int i=0;i<nums.size();i++){
            List<Integer> pearSet=nums.get(i);
            List<ThreadCustom> tmpThreads=new ArrayList<>();
            for(Integer index:pearSet){
                tmpThreads.add(totalThread.get(index-1));
            }
            res.add(tmpThreads);
        }
        return res;
    }

    private  List<ThreadCustom> createThreadByNumSmall(List<Integer> nums){
        List<ThreadCustom>res=new ArrayList<>();
        for(int i=0;i<nums.size();i++){
            res.add(totalThread.get(nums.get(i)));
        }
        return res;
    }


    
    void testAlg1(Handler handler){
        List<Set<Integer>>sortNums=utils.findallCinrange(threadNum);
        for(Set<Integer> nums:sortNums){
            List<ThreadCustom> selectThread=new ArrayList<>();
            for(Integer id:nums){
                selectThread.add(totalThread.get(id-1));
            }
            boolean res=new TestCompGroup(selectThread).test();
            if(res){
                Message msg = Message.obtain();
                msg.what=1;
                msg.obj="Conflict：:"+nums.toString()+"\n";
                handler.sendMessage(msg);
            }else {
                Message msg = Message.obtain();
                msg.what=1;
                msg.obj="No Conflict:"+nums.toString()+"\n";
                handler.sendMessage(msg);
            }
            reset();
        }
    }
   
    void testAlg2(Handler handler){
        boolean res=new TestCompGroup(totalThread).test();
        if(!res){

            Message msg = Message.obtain();
            msg.what=1;
            msg.obj="No Conflict\n";
            handler.sendMessage(msg);
        }else {
            int step=2;
            List<List<Integer>>queue=new ArrayList<>();
            List<Integer> initNums=new ArrayList<>();
            for(int i=0;i<threadNum;i++){
                initNums.add(i+1);
            }
            queue.add(initNums);

            step2:
            while(true){
          
                reset();
                List<List<ThreadCustom>> queueThread = createThreadByNum(queue);
               
                boolean flag = true;
                for (int i = 0; i < queueThread.size(); i++) {
                    if (queueThread.get(i).size() >= step) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    return;
                }

               
                boolean flag1 = true;
                Iterator<List<ThreadCustom>> ite = queueThread.iterator();
                Iterator<List<Integer>> iteIndex = queue.iterator();
             
                while (ite.hasNext()) {
                    List<ThreadCustom> o = ite.next();
                    List<Integer> indexNums = iteIndex.next();
               
                    if (new TestCompGroup(o).test()) {
                        flag1 = false;
                      
                        List<Set<Integer>> selectThreads = utils.findallCOfNums(indexNums, step);
                        boolean flag2 = true;
                        for (Set<Integer> nums : selectThreads) {
                        
                            List<ThreadCustom> selectThread = new ArrayList<>();
                            reset();
                            for (Integer id : nums) {
                                selectThread.add(totalThread.get(id - 1));
                            }
                            res = new TestCompGroup(selectThread).test();
                            if (res) {
                               
                                Message msg = Message.obtain();
                                msg.what=1;
                                msg.obj="Check Conflict:"+nums.toString()+"\n";
                                handler.sendMessage(msg);

                                List<List<Integer>> splitNums = utils.splitThreadCrash(indexNums, nums);
                                ite.remove();
                                iteIndex.remove();
                                queue.addAll(splitNums);
                                flag2 = false;
                                break;
                            } else {
                                Message msg = Message.obtain();
                                msg.what=1;
                                msg.obj="No Conflict:"+nums.toString()+"\n";
                                handler.sendMessage(msg);
                            }
                        }
                        if (flag2) {
                            step += 1;
                            continue step2;
                        }else {
                            continue step2;
                        }
                    }
                  
                    else {
                        ite.remove();
                        iteIndex.remove();
                        continue step2;
                    }
                }

                if (flag1) {
                    return;
                }

            }



        }





    }

   
    public void test(int alrSelect,Handler handler){
       
        if(alrSelect==0)
            
           testAlg1(handler);
        else if(alrSelect==1)
           
            testAlg2(handler);
    }
}






public class MainActivity extends AppCompatActivity {


    private List<Set<Integer>> crashThread;
    private Utils utils;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(msg.what==1){
                    Bundle data = msg.getData();
                    String name = (String) msg.obj;
                    Log.e("name",name);
                    TextView infoTV=(TextView) findViewById(R.id.tv_info);
                    infoTV.append(name);
                    ScrollView sv = (ScrollView)findViewById(R.id.sv);
                    sv.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }
        };

       
        utils=new Utils();

        Button runBtn=(Button) findViewById(R.id.btn_run);
        runBtn.setOnClickListener(new RunClick());


    }




    private class RunClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            Button btncur=(Button) findViewById(R.id.btn_run);
            btncur.setEnabled(false);
            TextView infoTV=(TextView) findViewById(R.id.tv_info);
           
            EditText threadNumET=(EditText) findViewById(R.id.eTN_threadNum);
            String threadInputStr=threadNumET.getText().toString();
            if(threadInputStr.isEmpty()){
                Toast.makeText(getApplicationContext(), "Input Number of Process:", Toast.LENGTH_LONG).show();
                btncur.setEnabled(true);
                return;
            }
            int threadnum=Integer.parseInt(threadInputStr);
            if (threadnum<2){
                Toast.makeText(getApplicationContext(), "Input A Number(>2)", Toast.LENGTH_LONG).show();
                btncur.setEnabled(true);
                return;
            }

           
            EditText threadCrashET=(EditText) findViewById(R.id.et_threadName);
            String threadCrashInputStr=threadCrashET.getText().toString();
            if(threadCrashInputStr.isEmpty()){
                Toast.makeText(getApplicationContext(), "Conflict Process List", Toast.LENGTH_LONG).show();
                btncur.setEnabled(true);
                return;
            }

         
            RadioGroup rgAl=(RadioGroup) findViewById(R.id.rg_algorithm);
            int radioButtonId=rgAl.getCheckedRadioButtonId();
            RadioButton rb=(RadioButton) findViewById(radioButtonId);
            if(rb==null){
                Toast.makeText(getApplicationContext(), "Choose One Algorithm", Toast.LENGTH_LONG).show();
                btncur.setEnabled(true);
                return;
            }
            String alStr=rb.getText().toString();



            List<Set<Integer>>threadName=utils.createThreadNameFromStr(threadCrashInputStr);
           
            for(Set<Integer> tmp:threadName){
                for(Integer num:tmp){
                    if(num<1 || num>threadnum){
                        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();
                        btncur.setEnabled(true);
                        return;
                    }
                }
            }

            TestCompApp testCompApp=new TestCompApp(threadnum,threadName);




            new Thread(new Runnable() {
                @Override
                public void run() {
                    long startTime = System.currentTimeMillis();   
                    if(alStr.equals("Full Testing")){
                        testCompApp.test(0,mHandler);
                    }else
                    if(alStr.equals(" Heuristic Testing Algorithm")){
                        testCompApp.test(1,mHandler);
                    }
                    long endTime = System.currentTimeMillis();    
                    Message msg = Message.obtain();
                    msg.what=1;
                    msg.obj="Running Time:"+(endTime-startTime)+"ms\n";
                    mHandler.sendMessage(msg);
                }
            }).start();

            btncur.setEnabled(true);
        }
    }
}