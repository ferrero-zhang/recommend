package com.ifeng.iRecommend.featureEngineering.DetectExtrmSim;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class StageExcutor {
	private static final Logger log= Logger.getLogger(StageExcutor.class);
	private static ExecutorService threadPool = Executors.newFixedThreadPool(20);
	public static Future<StageRt> sumbmitTask(Callable<StageRt> r){
		return threadPool.submit(r);
	}
	public static void close(){
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(5000L, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(),e);
		}
	}
}
