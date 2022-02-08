package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.LinkedList;

public class BlackListThread extends Thread{
    private static final int BLACK_LIST_ALARM_COUNT=5;
    private int start,end,maxcount;
    private String host;
    HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
    LinkedList<Integer> blackListOcurrences=new LinkedList<>();
    private int checkedListsCount = 0;
    private int ocurrences = 0;
    private Object lock = new Object();
    public static boolean stop = false;

    public BlackListThread(int start,int end, String host, Object lock,int maxcount){
        this.start = start;
        this.end = end;
        this.host = host;
        this.lock = lock;
        this.maxcount = maxcount;
    }

    public void run(){
        for (int i=start; i<end && ocurrences<BLACK_LIST_ALARM_COUNT;i++){
            if(stop){
                synchronized (lock){
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            checkedListsCount++;
            if (skds.isInBlackListServer(i, host)) {
                blackListOcurrences.add(i);
                ocurrences++;
            }
        }
    }

    public LinkedList<Integer> getBlackListOcurrences() {
        return blackListOcurrences;
    }

    public int getCheckedListsCount() {
        return checkedListsCount;
    }

    public int getOcurrences() {
        return ocurrences;
    }

    public static boolean getStop() {
        return stop;
    }

    public static void setStop(boolean stop) {
        BlackListThread.stop = stop;
    }
}
