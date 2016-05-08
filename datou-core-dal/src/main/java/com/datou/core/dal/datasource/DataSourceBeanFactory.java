package com.datou.core.dal.datasource;

import com.yoho.core.common.utils.AES;
import com.yoho.core.dal.datasource.router.MultiDataSourceRouter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.*;

/**
 * Created by jipeng on 2015/12/2.
 */
public class DataSourceBeanFactory implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceBeanFactory.class);


    /**
     * 默认的用户名和密码
     */
    private String defaultDBUser;
    private String defaultDBPassword;

    /**
     * 数据库配置信息
     *
     * <pre>
     *
     *     datasources:
     yh_resource:
     servers:192.168.50.69:9980,192.168.102.219:3306
     username: yh_test
     password: yh_test

     yh_shops:
     servers:192.168.50.69:9980,192.168.102.219:3306
     username: yh_test
     password: yh_test
     daos:
     - com.yoho.yhbresources.dal.IBrandDao
     - com.yoho.yhbresources.dal.IBrandDao

     readOnlyInSlave: true

     </pre>
     *
     */
    private static final String text = "1qaz2wsx3edc4rfv";
    /**
     * 动态数据路由对象，用于支持master和slave时，写操作只路由到master、读操作同时在master和slave中
     */
    MultiDataSourceRouter multiDataSourceRouter = new MultiDataSourceRouter();
    /**
     * database.yml 中的配置内容，会映射到 databasesMap 中， yml的配置映射到程序中为map或list
     */
    private Map<String, Object> databasesMap;
    /**
     * 当前的类通过spring注入，获取当前类型spring上下文，从而向spring的bean工厂注册datasource、sqlsessionfactory等bean，从而建立数据库连接池
     */
    private ConfigurableApplicationContext context;
    /**
     * BeanDefinitionBuilder为spring框架提供的创建bean的工具，等同于在xml配置中注入bean
     */
    private BeanDefinitionBuilder dynamicDataSourceBean;
    /**
     * 保存所有DataSource的bean对象，注入到？
     */
    private Map<String, Object> targetDataSources = new LinkedHashMap<String, Object>();

    /**
     * spring注入数据库配置databasesMap后，解析配置
     * bean的init-method，由spring注入当前类实例时，调用
     */
    public void init() throws Exception {

        //解析数据库连接配置
        decodeDatabases();

        //解析配置，“是否只在从节点读取，不在master读取”
        decodeReadOnlyInSlave();

    }

    /**
     * 解析数据库连接配置，并注入bean、创建数据库连接
     */
    private void decodeDatabases() throws Exception {
        //数据库列表 yh_resource，yh_shops
        Map<String, Object> databases = (Map<String, Object>) databasesMap.get("datasources");
        List<DataSource> dataSources = new ArrayList<>();

        /**
         * 解析数据库配置
         */
        initDataSourceConfig(dataSources, databases);

        /**
         *注入所有数据源的bean
         */
        buildDynDataSourceBean(dataSources);

        /**
         *注入sqlsessionfactory
         */
        buildSqlSessionFactoryBean();
    }

    /**
     * 解析配置，“是否只在从节点读取，不在master读取”
     */
    private void decodeReadOnlyInSlave() {
        try {
            boolean readOnlyInSlave = (boolean) databasesMap.get("readOnlyInSlave");
            logger.info("find readOnlyInSlave in database.yml, use value:{} ", readOnlyInSlave);

            multiDataSourceRouter.setReadOnlyInSlave(readOnlyInSlave);
        } catch (Exception e) {
            //do nothing， use default value：false 即 master和slave都读取
            //如果读取不到 “readOnlyInSlave”，会抛出异常
            logger.info("can not find readOnlyInSlave in database.yml, use default value:false ");
        }
    }

    /**
     * 读取database.yml中配置
     *
     * @param dataSources
     * @param databases
     */
    private void initDataSourceConfig(List<DataSource> dataSources, Map<String, Object> databases) throws Exception {

        Map<String, String> dbClusterSet = new LinkedHashMap<>();
        Map<String, String> daoDbClusterMap = new LinkedHashMap<>();
        multiDataSourceRouter.setDbClusterSet(dbClusterSet);
        multiDataSourceRouter.setDaoDbClusterMap(daoDbClusterMap);

        /**
         * 遍历所有的数据库集群，比如 yh_resources、yh_shops
         *     datasources:
         yh_resource:---- 数据库集群
         servers:192.168.50.69:9980,192.168.102.219:3306   --- 数据库集群中的每个数据源
         yh_shops:  ---数据库集群
         servers:192.168.50.69:9980,192.168.102.219:3306    --- 数据库集群中的每个数据源
         */
        int databaseIndex = 0;
        for (Map.Entry<String, Object> dbentry : databases.entrySet()) {

            String schema = dbentry.getKey();
            Map<String, Object> dbinfo = (Map<String, Object>) dbentry.getValue();
            ArrayList serverArr = (ArrayList) dbinfo.get("servers");


            /** 处理用户名和密码，如果没有填，或者密码解密失败，则使用默认的密码 **/
            String username = (String) dbinfo.get("username");
            String password = (String) dbinfo.get("password");
            //处理用户名和密码, 如果乜有传，则获取一个默认的
            if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
                username = this.defaultDBUser;
                password = this.defaultDBPassword;
            }

            username = StringUtils.trim(username);
            password = StringUtils.trim(password);
            //对密码进行解密
            try{

                String decrypedPassword = AES.decrypt(text, password);
                password = decrypedPassword;

            }catch (Exception e){
                //密码解密失败, 使用默认的密码
                password = AES.decrypt(text, this.defaultDBPassword);
            }





            /**
             * 遍历数据库集群中的每个服务器   yh_resources 下面的 servers
             * yh_resource:
             servers:192.168.50.69:9980,192.168.102.219:3306
             */
            StringBuffer dataSourceBeanList = new StringBuffer();
            for (int serverIndex = 0; serverIndex < serverArr.size(); serverIndex++) {

                DataSource ds = new DataSource();

                ds.schema = StringUtils.trim(schema);

                ds.username = username;
                ds.password = password;

                ds.host = StringUtils.trim((String) serverArr.get(serverIndex));
                ds.beanId = ds.schema + "@" + ds.host;

                /**
                 * 保存所有的数据源，用于后续注入datasourceBean
                 */
                dataSources.add(ds);

                /**
                 * 拼装DbCluster中数据源，比如 yh_resources 数据库为一个集群，每个集群中有master和slave多个数据源
                 * <entry key="resourcesDbCluster" value="resourcesMasterDataSource,resourcesSlaveDataSource"/>
                 */
                if (serverIndex != 0) dataSourceBeanList.append(",");
                dataSourceBeanList.append(ds.beanId);

            }

            /**
             * 保存每个数据库集群对应的数据源列表，数据库访问时，选择路由时使用，第一个默认为master，可写
             */
            dbClusterSet.put(schema, dataSourceBeanList.toString());

            /**
             * dao指定访问的数据库集群，即，dao访问的表，所存在的数据库
             * 默认情况下，dao访问数据库时，使用默认的数据库集群
             */
            List<String> daoList = (List<String>) dbinfo.get("daos");
            if (daoList != null) {
                for (String dao : daoList) {
                    daoDbClusterMap.put(dao, schema);
                }
            }

            /**
             * 默认情况下，yml配置中，第一个数据库集群为默认
             * 即：dao不指定数据库时，使用默认的数据库
             */
            if (databaseIndex == 0) {
                multiDataSourceRouter.setDefaultDBCluster(schema);
            }


            databaseIndex++;
        }
    }

    /**
     * @param databaseIndex
     * @param serverIndex
     * @param ds
     */
    private void setDefaultDataSource(int databaseIndex, int serverIndex, DataSource ds) {
        if (databaseIndex == 0 && serverIndex == 0) {
            ds.isDefault = true;
        }
    }

    /**
     * @param dataSources
     */
    private void buildDynDataSourceBean(List<DataSource> dataSources) {
        /**
         *
         */
        dynamicDataSourceBean = BeanDefinitionBuilder.genericBeanDefinition("com.yoho.core.dal.datasource.DynamicDataSource");
        String beanId = "dynamicDataSource";
        dynamicDataSourceBean.getBeanDefinition().setAttribute("id", beanId);

        /**
         * 创建数据源bean，创建数据库连接池
         */
        for (DataSource ds : dataSources) {
            buildDataSourceBean(ds);
        }

        /**
         * 向spring的AbstractRoutingDataSource注入所有的数据源
         */
        dynamicDataSourceBean.addPropertyValue("targetDataSources", targetDataSources);

        /**
         * 向spring工厂注册dynamicDataSource，可以被其他bean使用
         */
        this.registerBean("dynamicDataSource", dynamicDataSourceBean.getBeanDefinition());

    }

    /**
     * 创建sqlsessionFactory，并把dynamicDataSource和mapper文件路径
     */
    private void buildSqlSessionFactoryBean() {
        /**
         *代替spring中以下配置
         * <pre>
         <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
         <property name="dataSource" ref="dynamicDataSource"/>
         <property name="mapperLocations" value="classpath*:META-INF/mybatis/*.xml"></property>
         </bean>
         </pre>
         */
        BeanDefinitionBuilder sqlSessionFactoryBean = BeanDefinitionBuilder.rootBeanDefinition("org.mybatis.spring.SqlSessionFactoryBean");
        String beanId = "sqlSessionFactory";
        sqlSessionFactoryBean.getBeanDefinition().setAttribute("id", beanId);
        sqlSessionFactoryBean.addPropertyReference("dataSource", "dynamicDataSource");
        sqlSessionFactoryBean.addPropertyValue("mapperLocations", "classpath*:META-INF/mybatis/**/*.xml");
        sqlSessionFactoryBean.addPropertyValue("configLocation",  "classpath:META-INF/spring/mybatis-configuration.xml");
        this.registerBean(beanId, sqlSessionFactoryBean.getBeanDefinition());
    }

    /**
     * 创建mapperScannerConfigurer，mybatis扫描dao定义，创建mapper proxy作为dao的实现类
     * 注入dao路径和sqlsessionfacory
     * 【重要】目前通过配置文件的方式注入，此方法注入时，dao注入失败
     * <pre>
     *   代替xml方式注入sqlSessionFactory
     * <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
     * <property name="basePackage" value="com.yoho.*.dal"/>
     * <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"></property>
     * </bean>
     * </pre>
     */
    private void buildMapperScannerConfigurer() {
        /**
         暂时用不到，通过配置文件的方式注入
         通过此方法注入存在问题
         */
        BeanDefinitionBuilder mapperScannerConfigurerBean = BeanDefinitionBuilder.rootBeanDefinition("org.mybatis.spring.mapper.MapperScannerConfigurer");
        String mapperBeanId = "mapperScannerConfigurer";
        mapperScannerConfigurerBean.getBeanDefinition().setAttribute("id", mapperBeanId);
        mapperScannerConfigurerBean.addPropertyValue("basePackage", "com.yoho.*.dal");
        mapperScannerConfigurerBean.addPropertyValue("sqlSessionFactoryBeanName", "sqlSessionFactory");
        this.registerBean(mapperBeanId, mapperScannerConfigurerBean.getBeanDefinition());
    }

    /**
     * 创建数据源以及数据库连接
     * <p/>
     * <pre>
     *   代替spring配置文件中，通过配置注入的方式
     * <bean id="shopsSlaveDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
     * <property name="driverClassName" value="${jdbc.mysql.driver}"/>
     * <property name="url" value="${jdbc.mysql.shops.slave.url}"/>
     * <property name="username" value="${jdbc.mysql.shops.username}"/>
     * <property name="password" value="${jdbc.mysql.shops.password}"/>
     * <property name="initialSize" value="${jdbc.mysql.initialSize}"></property>
     * <property name="maxActive" value="${jdbc.mysql.maxActive}"></property>
     * <property name="maxIdle" value="${jdbc.mysql.maxIdle}"></property>
     * <property name="minIdle" value="${jdbc.mysql.minIdle}"></property>
     * <property name="maxWait" value="${jdbc.mysql.maxWait}"></property>
     * <property name="testWhileIdle" value="${jdbc.mysql.testWhileIdle}"></property>
     * <property name="timeBetweenEvictionRunsMillis" value="${jdbc.mysql.timeBetweenEvictionRunsMillis}"></property>
     * <property name="validationQuery" value="${jdbc.mysql.validationQuery}"></property>
     * <property name="testOnBorrow" value="${jdbc.mysql.testOnBorrow}"></property>
     * <property name="testOnReturn" value="${jdbc.mysql.testOnReturn}"></property>
     * </bean>
     *  </pre>
     *
     * @param ds
     */
    private void buildDataSourceBean(DataSource ds) {

        /**
         *动态注入BasicDataSource
         */
        BeanDefinitionBuilder basicDataSourceBean = BeanDefinitionBuilder.rootBeanDefinition("org.apache.commons.dbcp2.BasicDataSource");
        basicDataSourceBean.getBeanDefinition().setAttribute("id", ds.beanId);

        /**
         * 数据库连接信息
         */
        basicDataSourceBean.addPropertyValue("driverClassName", ds.driverClassName);
        basicDataSourceBean.addPropertyValue("url", "jdbc:mysql://" + ds.host + "/" + ds.schema);
        basicDataSourceBean.addPropertyValue("username", ds.username);
        basicDataSourceBean.addPropertyValue("password", ds.password);

        /**
         * 连接池大小
         */
        basicDataSourceBean.addPropertyValue("initialSize", ds.initialSize);
        basicDataSourceBean.addPropertyValue("maxTotal", ds.maxTotal);
        basicDataSourceBean.addPropertyValue("maxIdle", ds.maxIdle);
        basicDataSourceBean.addPropertyValue("minIdle", ds.minIdle);
        //basicDataSourceBean.addPropertyValue("maxWait",ds.maxWait);

        /**
         * 数据库连接检查
         */
        basicDataSourceBean.addPropertyValue("testWhileIdle", ds.testWhileIdle);
        basicDataSourceBean.addPropertyValue("testOnBorrow", ds.testOnBorrow);
        basicDataSourceBean.addPropertyValue("testOnReturn", ds.testOnReturn);
        basicDataSourceBean.addPropertyValue("validationQuery", ds.validationQuery);
        basicDataSourceBean.addPropertyValue("timeBetweenEvictionRunsMillis", ds.timeBetweenEvictionRunsMillis);
        basicDataSourceBean.addPropertyValue("defaultQueryTimeout", ds.defaultQueryTimeout);

        /**
         * 向spring注册datasource bean
         */
        this.registerBean(ds.beanId, basicDataSourceBean.getBeanDefinition());

        //org.apache.commons.dbcp.BasicDataSource dstest = (org.apache.commons.dbcp.BasicDataSource)context.getBean(ds.beanId) ;
        /**
         *AbstractRoutingDataSource
         */
        targetDataSources.put(ds.beanId, context.getBean(ds.beanId));

        /**
         * AbstractRoutingDataSource注入默认数据源
         */
        if (ds.isDefault) {
            dynamicDataSourceBean.addPropertyValue("defaultTargetDataSource", context.getBean(ds.beanId));
        }

    }

    /**
     * 实现ApplicationContextAware接口，设置spring上下文，用于向spring继续注入bean
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * 公共函数，向spring注入bean
     *
     * @param beanId
     * @param beanDefinition
     */
    private void registerBean(String beanId, BeanDefinition beanDefinition) {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) context;
        DefaultListableBeanFactory beanDefinitonRegistry = (DefaultListableBeanFactory) configurableApplicationContext.getAutowireCapableBeanFactory();
        beanDefinitonRegistry.registerBeanDefinition(beanId, beanDefinition);
    }

    public Map<String, Object> getDatabasesMap() {
        return databasesMap;
    }

    public void setDatabasesMap(Map<String, Object> databasesMap) {
        this.databasesMap = databasesMap;
    }

    /**
     * 数据源配置
     */
    private final class DataSource {

        boolean isDefault = false;
        String driverClassName = "com.mysql.jdbc.Driver";
        String beanId;

        /**
         * 数据库连接信息
         */
        String schema;
        String host;
        String username;
        String password;

        /**
         * 连接池配置
         */
        int initialSize = 5;  //初始化连接
        int maxTotal = 80;   //最大活动连接
        int maxIdle = 60;  //最大空闲连接
        int minIdle = 5;   //最小空闲连接
        int defaultQueryTimeout = 500;  //查询超时时间

        /**
         * 定期检测连接是否有效
         */
        boolean testWhileIdle = true;
        boolean testOnBorrow = false;
        boolean testOnReturn = false;
        String validationQuery = "select 1";
        int timeBetweenEvictionRunsMillis = 2000;

    }



    public void setDefaultDBUser(String defaultDBUser) {
        this.defaultDBUser = defaultDBUser;
    }

    public void setDefaultDBPassword(String defaultDBPassword) {
        this.defaultDBPassword = defaultDBPassword;
    }
}
