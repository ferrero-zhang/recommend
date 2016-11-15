package com.ifeng.iRecommend.featureEngineering.KnowledgeGraphSearch;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.ifeng.commen.Utils.LoadConfig;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

public class OrientGraphInstance {

	
	static OrientGraphFactory factory = null;
	
	public static OrientGraphFactory getInstance(){
		synchronized (OrientGraphInstance.class) {
			if(null == factory){
				factory = new OrientGraphFactory(
						LoadConfig.lookUpValueByKey("KnowledgeGraphUrl"),"root","root");
				
				
				
				/**
				 * <entry name="client.channel.minPool" value="50"/>
		           <entry name="client.channel.maxPool" value="300"/>
				 */
				
				//factory.setupPool(10, 3000);
				
				Map<String, Object> configMap = new HashMap<String, Object>();
				
				configMap.put("client.channel.minPool", 50);
				configMap.put("client.channel.maxPool", 3000);
                configMap.put("profiler.enabled", false);
                configMap.put("cache.local.enabled", false);
                
				OGlobalConfiguration.setConfiguration(configMap);
				
				/**
				 *     <entry value="60000" name="network.socketTimeout"/>           
                    <entry value="45000" name="network.lockTimeout"/>
                    
                    cache.level1.enabled
                    
				 * 
				 * It's strongly suggested you enlarge your timeout only after 
				 * tried to enlarge the Network Connection Pool. The timeout parameters to tune are:
                  network.lockTimeout, the timeout in ms to acquire a lock against a channel. The 
                  default is 15 seconds.
                   network.socketTimeout, the TCP/IP Socket timeout in ms. The default is 10 seconds.
				 */
				
				OGlobalConfiguration.SERVER_LOG_DUMP_CLIENT_EXCEPTION_FULLSTACKTRACE.setValue(Boolean.TRUE);
				
				OGlobalConfiguration.SERVER_LOG_DUMP_CLIENT_EXCEPTION_LEVEL.setValue(Level.INFO.getName());
				
				OGlobalConfiguration.NETWORK_SOCKET_TIMEOUT.setValue(60000);

		        OGlobalConfiguration.NETWORK_LOCK_TIMEOUT.setValue(45000);
				
				System.err.println("==========");
			}
		}
		return factory;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
            
	}

}
