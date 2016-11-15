package com.ifeng.iRecommend.lidm.userLog;

/**
 * <PRE>
 * 作用 : 
 *   读取本地log文件、解析、合并等基本操作。
 *   
 * 使用 : 
 *   
 * 示例 :
 *   
 * 注意 :
 * 	 
 * 历史 :
 * -----------------------------------------------------------------------------
 *        VERSION          DATE           BY       CHANGE/COMMENT
 * -----------------------------------------------------------------------------
 *          1.0          2014-01-21       lidm          change
 * -----------------------------------------------------------------------------
 * </PRE>
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ifeng.commen.Utils.LoadConfig;
import com.ifeng.commen.Utils.commenFuncs;
import com.ifeng.iRecommend.fieldDicts.fieldDicts;

public class LogProcessor {
	private static final Log LOG = LogFactory.getLog("log_to_hbase");

	/**
	 * 日志文件的类型 PCLOG:pc端日志 APPLOG:客户端日志
	 * 
	 */
	public enum LogType {
		PCLOG, APPLOG,UNDEFINED
	};

	private String tableName;// hbase中的数据表的名称，log解析后的数据会被写入到hbase中此名称对应的数据表
	private LogDate startLogDate;// 解析此日期之后的log源文件，如20140121，表示解析20140121之后的log
	private LogDate currentLogDate;// 当前正在解析的log源文件所在目录
	private LogDate endLogDate;// log文件为此日期时终止解析，解析区间为[startLogDate,endLogDate)
	private LogType logType;// log源文件的类别，如pc端的log、客户端的log

	private int logFrequency;// log源文件的产生频率;如1表示每一分钟生成一个log文件。
	private int maxLogFileIndex;// 根据log文件的产生频率，在一天即24小时内，生成的log文件的最大数目

	// constructor
	public LogProcessor(LogType logType) {
		this.logType = logType;

		if (logType == LogType.PCLOG)
			tableName = fieldDicts.pcUserLogTableNameInHbase;
		else if (logType == LogType.APPLOG)
			tableName = fieldDicts.appUserLogTableNameInHbase;
		else 
			tableName = null;

		logFrequency = 1;
		maxLogFileIndex = 24 * 60 / logFrequency - 1;

		startLogDate = null;
		currentLogDate = null;
		endLogDate = null;
	}

	/**
	 * 根据log文件的类型及是否压缩，进行文件拷贝或解压，并返回准备好的log文件的路径
	 * 
	 * @param compressed
	 *            log文件是否是压缩格式，如gz格式
	 * @return
	 */
	public LogFileStatus PrepareToReadLogFile(boolean compressed) {
		String logPath;// 源log文件所在目录
		String source_logfile_name = null;// 源log文件路径及名称
		String dest_logfile_name = null;// copy与解压后log文件的路径及名称

		// 存放日志文件的临时目录，因为源log文件所在目录只允许读，不允许写或解压等操作
		// 需要将其copy到其它目录进行解压、读取等操作
		String logPath_temp = LoadConfig.lookUpValueByKey("temp_log_path");

		// 复制log文件到temp目录失败后的尝试次数
		int reCopyTime = 10;

		logPath = LoadConfig.lookUpValueByKey("log_path")
				+ currentLogDate.getDate() + "/";

		try {
			// 源log文件目录，如/weblog/2013-10-25/1201。attention:未加后缀名
			String logFilePath = getLogFilePath(logPath,
					currentLogDate.getIndex(), logFrequency);

			// 日志源文件名称，attention:未加后缀名，如1201
			String logFileName = logFilePath.substring(logFilePath
					.lastIndexOf(getFileSeparator()) + 1);

			// pc端log进行了压缩，后缀为.sta.gz,客户端日志没有进行压缩，后缀为.sta
			if (compressed)
				source_logfile_name = logFilePath + ".sta.gz";
			else
				source_logfile_name = logFilePath + ".sta";

			LOG.info("start process log file:" + source_logfile_name);

			File source_logfile = new File(source_logfile_name);
			if (!source_logfile.exists()) {
				return new LogFileStatus(source_logfile_name,
						FileStatus.SOURCENOTEXIST);
			}

			// 如果不需要copy源log文件到临时目录，并且不需要解压操作，可以直接读取源log文件
			if (!shouldCopyFile() && !compressed)
				return new LogFileStatus(source_logfile_name,
						FileStatus.SUCCESSED);

			dest_logfile_name = logPath_temp + logFileName + ".sta";

			if (compressed) {
				// 临时copy压缩格式的日志到临时目录用于读取操作
				if (!copyLogFileAndUnzip(source_logfile_name, dest_logfile_name
						+ ".gz", reCopyTime)) {
					LOG.error("copy and uzip logfile error:"
							+ source_logfile_name);
					currentLogDate.updateLogDate();

					return new LogFileStatus(dest_logfile_name,
							FileStatus.COPYORUNZIPERROR);
				}
			} else {
				if (!copyLogFileAndNoUnzip(source_logfile_name,
						dest_logfile_name, reCopyTime)) {
					LOG.error("copy logfile error:" + source_logfile_name);
					currentLogDate.updateLogDate();
					return new LogFileStatus(dest_logfile_name,
							FileStatus.COPYERROR);
				}
			}
		} catch (Exception e) {
			LOG.error("copy or unzip log file:" + source_logfile_name
					+ " error", e);

			return new LogFileStatus(dest_logfile_name,
					FileStatus.COPYORUNZIPERROR);
		}
		File dest_log_file = new File(dest_logfile_name);
		if (!dest_log_file.exists())
			return new LogFileStatus(dest_logfile_name, FileStatus.DESTNOTEXIST);

		return new LogFileStatus(dest_logfile_name, FileStatus.SUCCESSED);

	}

	/**
	 * 是否需要copy源log文件到临时目录 客户端log可以直接读取，因为没有压缩 pc端日志需要copy到临时目录，因为它是gz压缩格式
	 */
	public boolean shouldCopyFile() {
		if (logType == LogType.APPLOG)
			return false;
		else if (logType == LogType.PCLOG)
			return true;
		return true;
	}

	/**
	 * 根据logFileStatus决定是否需要进行异常处理并更新currentLogDate或正常进行解析
	 * @param logFileStatus
	 * @return
	 */
	public boolean checkLogFileStatus(LogFileStatus logFileStatus) {
		boolean logFileReady = false;
		if (logFileStatus.getFileStatus() != FileStatus.SUCCESSED)
			if (logFileStatus.getFileStatus() == FileStatus.SOURCENOTEXIST) {
				LOG.info("wait for log file:" + logFileStatus.getLogFilePath()
						+ ",wait 30 seconds.");
				try {
					//源log文件不存在，可能是暂时未同步过来，等待30秒
					Thread.sleep(30 * 1000);
				} catch (Exception e) {
					LOG.error("thread sleep error", e);
				}
			} else if (logFileStatus.getFileStatus() == FileStatus.COPYERROR//复制文件时出错
					|| logFileStatus.getFileStatus() == FileStatus.UNZIPERROR//解析文件时出错
					|| logFileStatus.getFileStatus() == FileStatus.COPYORUNZIPERROR//复制或解压时出错
					|| logFileStatus.getFileStatus() == FileStatus.DESTNOTEXIST/*目标文件不存在*/) {
				LOG.error("prepare log file error,maybe copy or unzip failed");
				//跳过出错的log文件，解析下一个文件
				currentLogDate.updateLogDate();
			} else {
				LOG.error("prepare log file error,unknown error");
				currentLogDate.updateLogDate();
			}
		else
			logFileReady = true;
		return logFileReady;
	}
	
	/**
	 * 验证log解析后得到的dataHashMap是否合法
	 * @param dataHashMap
	 * @return
	 */
	public boolean checkDataHashMapValid(HashMap<String, String> dataHashMap){
		if (dataHashMap == null || dataHashMap.size() == 0) {
			LOG.error("the dataHashMap is empty,please check copy/unzip/extract process");
			//跳过出错的log文件，解析下一个文件
			currentLogDate.updateLogDate();
			return false;
		}
		return true;
	}

	/**
	 * copy/unzip log源文件、抽取、写入相应数据库
	 */
	public void StartProcessLog() {
		if (startLogDate == null) {
			LOG.error("invalid start_log_date,init start log date first!");
			return;
		}

		boolean compressedLog = isCompressedLog();
		String dest_logfile_name = null;
		int lisener = 0;

		LogFileStatus logFileStatus;
		while (true) {
			long start = System.currentTimeMillis();
			// 是否已处理完[startLogDate,endLogDate)区间内的log
			if (endLogDate != null && checkProcessLogFinished())
				break;

			logFileStatus = PrepareToReadLogFile(compressedLog);
			if(!checkLogFileStatus(logFileStatus)){
				lisener++;
				if(lisener == 10){
					currentLogDate.updateLogDate();
					lisener = 0;
				}
				continue;
			}
				
			lisener = 0;
			dest_logfile_name = logFileStatus.getLogFilePath();
			File dest_logfile_sta = new File(dest_logfile_name);

			// 抽取pc端日志文件中的信息并存入hash
			HashMap<String, String> dataHashMap = extractLog(dest_logfile_name);
			if(!checkDataHashMapValid(dataHashMap))
				continue;

			// 加入log的日期和时间，日期如：20131024，时间如：1201，表示12点01分的日志
			dataHashMap.put("log_time", getCurrentLogFileName());
			dataHashMap.put("log_date", currentLogDate.getSimpleDate());

			// 删除log文件解压后的log文件
			if (logType != LogType.APPLOG && dest_logfile_sta.exists())
				dest_logfile_sta.delete();

			// 将dataHashMap中存储的数据写入数据库hbase/cf中
			pushLogDataHashMap(dataHashMap);

			// update currentLogDate
			currentLogDate.updateLogDate();

			long end = System.currentTimeMillis();
			LOG.info("duration of process log file:" + (end - start));
			continue;
		}
	}

	/**
	 * log源文件是否为压缩格式 目前客户端log为正常格式，即后缀名为.sta pc端log为压缩格式，即后缀名为.sta.gz 默认为非压缩格式
	 */
	public boolean isCompressedLog() {
		boolean compressedLog = false;
		switch (logType) {
		case PCLOG:
			compressedLog = true;
			break;
		case APPLOG:
			compressedLog = false;
			break;
		default:
			compressedLog = false;
			break;
		}
		return compressedLog;
	}

	/**
	 * 将dataHashMap中存储的数据写入数据库hbase/cf中
	 * 
	 * @param dataHashMap
	 */
	public void pushLogDataHashMap(HashMap<String, String> dataHashMap) {
		if (dataHashMap == null || dataHashMap.size() == 0) {
			LOG.error("the dataHashMap is empty");
			return;
		}

		if (logType == LogType.PCLOG) {
			CountDownLatch pclogToHbaseCDT = new CountDownLatch(2);

			// pc端日志 写入hbase
			LogToDB logToHbase = LogToDBFactory.createLogToHbase(tableName,
					dataHashMap);
			LogPushThread logToHbaseThread = new LogPushThread(
					"PCLog-HB-Thread", pclogToHbaseCDT, logToHbase);
			logToHbaseThread.start();

			// pc端日志 写入cf
			LogToDB logToCF = LogToDBFactory.createLogToCF(dataHashMap);
			LogPushThread logToCFThread = new LogPushThread("PCLog-CF-Thread",
					pclogToHbaseCDT, logToCF);
			logToCFThread.start();
			try {
				pclogToHbaseCDT.await();
			} catch (InterruptedException e) {
				LOG.error("pclogToHbaseCDT error", e);
			}
		} else if (logType == LogType.APPLOG) {
			// 客户端日志 写入hbase
			try {
				/*LogToDB logToHbase = LogToDBFactory.createLogToHbase(tableName,
						dataHashMap);
				logToHbase.PushLogToDB();*/
				LogToSolr logToSolr = new LogToSolr();
				logToSolr.pushToSolr(dataHashMap);
			} catch (Exception e) {
				LOG.error("failed push app log data to hbase", e);
			}
		}
	}

	/**
	 * 抽取日志文件中的信息 支持的日志类型有pc端日志、客户端日志
	 * 
	 * @param logfile_name
	 * @return
	 */
	public HashMap<String, String> extractLog(String logfile_name) {
		if (logType == LogType.PCLOG)
			return ExtractLog.ExtractPCLog(logfile_name);
		else if (logType == LogType.APPLOG)
			return ExtractLog.ExtractAppLog(logfile_name);
		else {
			LOG.error("not supported log type");
			return null;
		}
	}

	/**
	 * 是否已处理完[startLogDate,endLogDate)区间内的log true:已处理完 false:未处理完
	 * 
	 * @return
	 */
	public boolean checkProcessLogFinished() {
		if (endLogDate != null)
			if (currentLogDate.getDate().equals(endLogDate.getDate())
					&& currentLogDate.getIndex() == endLogDate.getIndex()) {
				LOG.info("all logs are processed:" + currentLogDate + "-"
						+ currentLogDate + "~" + endLogDate + "-" + endLogDate);
				return true;
			}
		return false;
	}

	/**
	 * 根据currentLogDate获取正在解析的log的文件名,attention:不带后缀
	 */
	public String getCurrentLogFileName() {
		return getLogFileName(currentLogDate.getIndex(), logFrequency);
	}

	/**
	 * 根据输入的日期参数，确定需要解析的log的开始文件与结束文件。 log源文件存放于以日期命名的目录内。
	 * 
	 * @param dateProps
	 * @return
	 */
	public boolean initLogDate(String[] dateProps) {
		String start_log_date = null;// like 2013-10-28
		String end_log_date = null;// like 2013-10-28
		int start_log_index = 0;
		int end_log_index = 0;

		if (dateProps.length == 0) {
			// 默认取当前日期
			Date date = new Date();
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
			start_log_date = dateformat.format(date);
		} else if (dateProps.length == 1) {
			start_log_date = dateProps[0];
		} else if (dateProps.length == 2) {
			start_log_date = dateProps[0];
			start_log_index = Integer.valueOf(dateProps[1]);
		} else if (dateProps.length == 4) {
			start_log_date = dateProps[0];
			start_log_index = Integer.valueOf(dateProps[1]);
			end_log_date = dateProps[2];
			end_log_index = Integer.valueOf(dateProps[3]);
		}

		if (start_log_date == null || start_log_date.equals("")) {
			LOG.error("log_date error");
			return false;
		}

		// startLogDate
		int start_index = (60 * (start_log_index / 100) + start_log_index % 100)
				/ logFrequency;
		startLogDate = new LogDate(start_index, start_log_date);
		currentLogDate = startLogDate;

		// endLogDate
		if (end_log_date != null && end_log_date.length() > 0) {
			int end_index = (60 * (end_log_index / 100) + end_log_index % 100)
					/ logFrequency;

			endLogDate = new LogDate(end_index, end_log_date);
		}

		return true;
	}

	/**
	 * 根据log的产生频率及index获得log文件名称,attention:不带后缀名
	 * 
	 * @param index
	 *            index
	 * @param logFrequency
	 *            log产生频率，1表示每1分钟生成一个log文件
	 * @return
	 */
	public String getLogFileName(int index, int logFrequency) {
		int time = (index * logFrequency / 60) * 100
				+ ((index * logFrequency) % 60);
		String logFileName = null;
		if (time < 10)
			logFileName = "000" + time;
		else if (time >= 10 && time < 100)
			logFileName = "00" + time;
		else if (time >= 100 && time < 1000)
			logFileName = "0" + time;
		else
			logFileName = "" + time;

		return logFileName;
	}

	/**
	 * 复制log文件到临时目录并解压。 原挂载的日志目录如/weblog为只读模式， 需要将日志文件copy到其它有可写权限的目录进行解压与读取。
	 * 
	 * @param sourceLogFile
	 * @param destLogFile
	 * @param reCopyTime
	 * @return
	 */
	public boolean copyLogFileAndUnzip(String sourceLogFile,
			String destLogFile, int reCopyTime) {
		long start = System.currentTimeMillis();
		boolean copySucceed = copyFile(sourceLogFile, destLogFile);
		File tempLogFile_gz = new File(destLogFile);

		while (!copySucceed && (reCopyTime--) > 0) {
			LOG.error("copy failed[" + reCopyTime + "]:" + sourceLogFile);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				LOG.error("thread sleep error", e);
			}
			copySucceed = copyFile(sourceLogFile, destLogFile);
			if (copySucceed && tempLogFile_gz.exists())
				break;
		}

		if (!copySucceed && reCopyTime <= 0) {
			LOG.error("recopy failed:" + sourceLogFile);
			return false;
		}

		boolean unCompSucceed = uncompressFile(destLogFile);
		File tempLogFile = new File(destLogFile.replace(".gz", ""));
		if (!unCompSucceed || !tempLogFile.exists()) {
			LOG.error("unCompress file failed:" + sourceLogFile);
			return false;
		}
		// 删除临时文件
		File destFile_gz = new File(destLogFile);
		if (destFile_gz.exists())
			destFile_gz.delete();
		long end = System.currentTimeMillis();
		LOG.info("duration of copy and unzip log file:" + (end - start));
		return true;
	}

	/**
	 * 复制log文件到临时目录。
	 * 
	 * @param sourceLogFile
	 * @param destLogFile
	 * @param reCopyTime
	 * @return
	 */
	public boolean copyLogFileAndNoUnzip(String sourceLogFile,
			String destLogFile, int reCopyTime) {
		long start = System.currentTimeMillis();
		boolean copySucceed = copyFile(sourceLogFile, destLogFile);
		File tempLogFile = new File(destLogFile);

		while (!copySucceed && (reCopyTime--) > 0) {
			LOG.error("copy failed[" + reCopyTime + "]:" + sourceLogFile);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				LOG.error("thread sleep error", e);
			}
			copySucceed = copyFile(sourceLogFile, destLogFile);
			if (copySucceed && tempLogFile.exists())
				break;
		}

		if (!copySucceed && reCopyTime <= 0) {
			LOG.error("recopy failed:" + sourceLogFile);
			return false;
		}

		long end = System.currentTimeMillis();
		LOG.info("duration of copy log file:" + (end - start));
		return true;
	}

	/**
	 * 解压日志文件 pc端的日志源文件进行了压缩，如1121.sta.gz，需要进行解压以便随后的读取操作。
	 * 
	 * @param inFileName
	 * @return
	 */
	public boolean uncompressFile(String inFileName) {
		boolean rv = true;
		GZIPInputStream in = null;
		FileOutputStream out = null;
		try {
			if (!getExtension(inFileName).equalsIgnoreCase("gz")) {
				LOG.error("file name must have extension of \".gz\"");
			}

			try {
				in = new GZIPInputStream(new FileInputStream(inFileName));
			} catch (Exception e) {
				LOG.error("read file error:" + e);
				return false;
			}

			String outFileName = getFileName(inFileName);
			try {
				out = new FileOutputStream(outFileName);
			} catch (FileNotFoundException e) {
				LOG.error("not found file error:" + e);
				return false;
			}
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (Exception e) {
			rv = false;
			LOG.error("un-compress file error:" + e);
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				LOG.error("unCompress file error:" + e);
			}
		}
		return rv;
	}

	/**
	 * 获取文件后缀名
	 * 
	 * @param f
	 * @return
	 */
	public String getExtension(String f) {
		String ext = "";
		int i = f.lastIndexOf('.');

		if (i > 0 && i < f.length() - 1) {
			ext = f.substring(i + 1);
		}
		return ext;
	}

	/**
	 * 获取文件名称
	 * 
	 * @param f
	 * @return
	 */
	public String getFileName(String f) {
		String file_name = "";
		int i = f.lastIndexOf('.');
		if (i > 0 && i < f.length() - 1) {
			file_name = f.substring(0, i);
		}
		return file_name;
	}

	/**
	 * 将日志文件从源目录copy到另一目录
	 * 
	 * @param sourceFilePath
	 *            日志文件源目录
	 * @param destFilePath
	 *            日志文件目标目录
	 * @return
	 */
	public boolean copyFile(String sourceFilePath, String destFilePath) {
		boolean rv = true;
		File srcFile = new File(sourceFilePath);
		File destFile = new File(destFilePath);
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(srcFile);
			os = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} catch (Exception e) {
			LOG.error("copy file error:" + e);
			rv = false;
		} finally {
			try {
				if (is != null)
					is.close();
				if (os != null)
					os.close();
			} catch (IOException e) {
				LOG.error("close file io error:" + e);
				rv = false;
			}
		}
		return rv;
	}

	/**
	 * 获得目录分隔符，"\" for windows,"/" for linux
	 * 
	 * @return
	 */
	public String getFileSeparator() {
		Properties props = System.getProperties();
		if (props.getProperty("os.name").toLowerCase().contains("windows"))
			return "\\";
		else
			return "/";
	}

	/**
	 * 获得日志文件的具体路径及名称
	 * 
	 * @param dirpath
	 *            日志目录
	 * @param i
	 *            index
	 * @param unit
	 *            log产生频率，1表示每1分钟生成一个log文件
	 * @return
	 */
	public String getLogFilePath(String dirpath, int i, int unit) {
		int currentTime = (i * unit / 60) * 100 + ((i * unit) % 60);
		StringBuffer logfilenamebf = new StringBuffer();
		if (currentTime < 10)
			logfilenamebf.append(dirpath).append("000").append(currentTime);
		else if (currentTime >= 10 && currentTime < 100)
			logfilenamebf.append(dirpath).append("00").append(currentTime);
		else if (currentTime >= 100 && currentTime < 1000)
			logfilenamebf.append(dirpath).append("0").append(currentTime);
		else
			logfilenamebf.append(dirpath).append(currentTime);

		return logfilenamebf.toString();
	}

	public LogType getLogType() {
		return logType;
	}

	/**
	 * 记录日志文件的文件名称 日志文件以分和秒命名，放在以date命名的目录内，
	 * 如2014-01-17/1120.sta,表示是日期为20140117时11分20秒的日志文件
	 * 
	 * @author lidm
	 * 
	 */
	public class LogDate {
		private int index;
		private String date;// 格式为yyyy-MM-dd

		public LogDate(int index, String date) {
			this.index = index;
			this.date = date;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public String getDate() {
			return date;
		}

		public String getSimpleDate() {
			return date.replace("-", "");
		}

		public void setLog_date(String date) {
			this.date = date;
		}

		public void updateLogDate() {
			if (index < maxLogFileIndex) {
				index++;
			} else {
				index = 0;
				date = commenFuncs.getSpecifiedDayAfter(date);
			}
		}
	}

	public enum FileStatus {
		UNDEFINED, SUCCESSED, SOURCENOTEXIST, DESTNOTEXIST, COPYERROR, UNZIPERROR, COPYORUNZIPERROR
	};

	public class LogFileStatus {
		private String log_file_path;
		private FileStatus fileStatus;

		public LogFileStatus(String log_file_path, FileStatus fileStatus) {
			this.log_file_path = log_file_path;
			this.fileStatus = fileStatus;
		}

		public FileStatus getFileStatus() {
			return fileStatus;
		}

		public String getLogFilePath() {
			return log_file_path;
		}
	}
}
