package com.datou.core.dal.datasource.router;

import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.yoho.core.common.database.DatabaseRouting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yoho.core.common.helpers.StackTraceHelper;
import com.yoho.core.dal.datasource.intercepor.*;

public class MultiDataSourceRouter{

	//调试日志
	private static final Logger logger = LoggerFactory.getLogger(DaoInterceptor.class);
	
	//运行日志
	private static final Logger runLogger = LoggerFactory.getLogger("Run");
	
	private static final ThreadLocal<String> dataSourceKey = new InheritableThreadLocal<String>();



	/**
	 * 数据库集群,master,salve读写分离
	 */
	private static Map<String, String> dbClusterSet;
	
	/**
	 * DAO与数据库集群的对应关系
	 */
	private static Map<String, String> daoDbClusterMap;
	
	/**
	 * 默认数据库集群,master,salve读写分离
	 */
	private static String defaultDBCluster ;

	/**
	 * 读操作是否只在slave中
	 */
	private static boolean readOnlyInSlave = false ;

	/**
	 * mybatis配置文件中，database cluster 默认第一个 datasource为master
	 */
	private static final int MASTER_INDEX = 0 ;
	
	/**
	 * dao中数据库 statement命名，约定为 select* insert*  update* 
	 */
	private static final Pattern select = Pattern.compile("^select.*");
	
	/**
	 * private static final Pattern update = Pattern.compile("^update.*");
	 * private static final Pattern insert = Pattern.compile("^insert.*");	
	 */
	
	/**
	 * 默认不应该有delete操作
	 */
	private static final Pattern delete = Pattern.compile("^delete.*");




	/**
	 * 数据源路由选择
	 * @param mapperNamespace  Mapping文件中的DAO名称
	 * @param statementId   数据库操作名称
	 * @return
	 */
	public static String router(String mapperNamespace, String statementId){
		
		//禁止删除操作,并记录错误日志
		if (delete.matcher(statementId).matches()) {
			logger.warn("router databsource warning, delelte operation is forbiden, dao[{}], statement[{}]. ",
					mapperNamespace,statementId);

			//暂时放开
			//return null ;
		}
		
		//获取DAO对应的数据库集群
		String daoDbCluster = MultiDataSourceRouter.getDaoDbClusterMap().get(mapperNamespace);
		if( daoDbCluster == null ){
			daoDbCluster = MultiDataSourceRouter.getDefaultDBCluster();
		}
		
		//选择数据源
		String datasourceSet = MultiDataSourceRouter.getDbClusterSet().get(daoDbCluster);
		String dataSourceArr[] = datasourceSet.split(",");
		
		//默认使用数据库集群中第一个数据源  即master
		String dataSource = dataSourceArr[MASTER_INDEX];
		
		//如果为读操作，随机选择一个数据源
		if (select.matcher(statementId).matches() ) {
			Random random = new Random();
			int rval = random.nextInt(dataSourceArr.length);
			int index = rval % dataSourceArr.length;		

			//读操作是否只在slave上完成
			//因为从北京69数据库读取太慢，所以读操作只在南京的slave
			if( MultiDataSourceRouter.getReadOnlyInSlave() ){
				if( MASTER_INDEX == index && dataSourceArr.length > 1){
					index = (index+1)%dataSourceArr.length;
				}
			}

			//在业务逻辑中, 强制走master
			if( Boolean.TRUE.equals(DatabaseRouting.isForceMaster())  ){
				index = MASTER_INDEX ;
				logger.info("force router datasource to master");
			}

			logger.debug("router datasource in cluster, random[{}], size[{}], datasource index[{}] in datasourceSet[{}]",
					rval,dataSourceArr.length,index, datasourceSet);
			
			dataSource = dataSourceArr[index];
		}		
		
		logger.debug("datasource router result is [{}], choose by dao[{}] & statementid[{}] in cluster[{}]",
				dataSource,mapperNamespace,statementId,daoDbCluster);
		
		return dataSource;
	}


	
	public static void setDataSourceKey(String dataSource) {
		dataSourceKey.set(dataSource);
	}
	
	public static Object getCurrentDataSourceKey() {
		return dataSourceKey.get();
	}

	public static boolean getReadOnlyInSlave() {
		return MultiDataSourceRouter.readOnlyInSlave;
	}

	public static void setReadOnlyInSlave(boolean readOnlyInSlave) {
		MultiDataSourceRouter.readOnlyInSlave = readOnlyInSlave;
	}

	public static void setDbClusterSet(Map<String, String> dbClusterSet) {
		MultiDataSourceRouter.dbClusterSet = dbClusterSet;
	}
	public static Map<String, String> getDbClusterSet() {
		return MultiDataSourceRouter.dbClusterSet;
	}	
	
	public static void setDaoDbClusterMap(Map<String, String> daoDbClusterMap){
		MultiDataSourceRouter.daoDbClusterMap = daoDbClusterMap ;
	}
	public static Map<String, String> getDaoDbClusterMap(){
		return MultiDataSourceRouter.daoDbClusterMap  ;
	}	
	
	public static void setDefaultDBCluster(String defaultDBCluster){
		MultiDataSourceRouter.defaultDBCluster = defaultDBCluster ;
	}
	public static String getDefaultDBCluster(){
		return MultiDataSourceRouter.defaultDBCluster ;
	}



}