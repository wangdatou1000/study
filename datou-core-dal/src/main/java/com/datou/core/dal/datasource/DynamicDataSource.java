package com.datou.core.dal.datasource;


import com.yoho.core.dal.datasource.router.MultiDataSourceRouter;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态选择数据源，sqlsessionfactory获取连接时回掉
 * @author jipeng
 * 
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
	
	@Override
	protected Object determineCurrentLookupKey() {
		return MultiDataSourceRouter.getCurrentDataSourceKey();
	}

}