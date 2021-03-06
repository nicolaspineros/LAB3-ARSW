/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    public Object lock = new Object();
    private AtomicInteger checkedListsCount = new AtomicInteger(0);
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int n){
        
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        int ocurrencesCount=0;
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        ArrayList<BlackListThread> th = new ArrayList<BlackListThread>();
        int part = skds.getRegisteredServersCount()/n;
        int res = skds.getRegisteredServersCount()%n;


        for(int i = 0; i < n; i++){
            if(i == n-1){
                th.add(new BlackListThread((i*part),(i*part)+part+res,ipaddress,lock,BLACK_LIST_ALARM_COUNT));
            }else{
                th.add(new BlackListThread((i*part),(i*part)+part,ipaddress,lock,BLACK_LIST_ALARM_COUNT));
            }
            th.get(i).start();
        }

        for (BlackListThread thread : th) {
            while(thread.isAlive()){
                continue;
            }
            checkedListsCount.addAndGet(1);
            ocurrencesCount += thread.getOcurrences();
            blackListOcurrences.addAll(thread.getBlackListOcurrences());
            if(ocurrencesCount >= BLACK_LIST_ALARM_COUNT){
                thread.setStop(true);
                break;
            }
        }
        
        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }
    
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    
}
