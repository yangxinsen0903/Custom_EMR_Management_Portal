package com.sunbox.shardingjdbcspringbootstarter.configuration;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunbox.shardingjdbcspringbootstarter.properties.ShardingJDBCProperties;
import com.sunbox.shardingjdbcspringbootstarter.util.DESUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(prefix = "spring.datasource",value = "enable",matchIfMissing = true)
@EnableConfigurationProperties(ShardingJDBCProperties.class)
public class ShardingJDBCAutoConfiguration {

    @Autowired
    private ShardingJDBCProperties druidProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * 默认数据源配置
     *
     * @return
     */
    public DataSource dataSource() {
        HikariDataSource datasource = new HikariDataSource();
        datasource.setJdbcUrl(druidProperties.getUrl());
        datasource.setUsername(DESUtil.decrypt(druidProperties.getUsername()));
        datasource.setPassword(DESUtil.decrypt(druidProperties.getPassword()));
        datasource.setDriverClassName(druidProperties.getDriverClassName());
        //datasource.setInitialSize(druidProperties.getInitialSize());
        datasource.setMinimumIdle(druidProperties.getMinIdle());
        datasource.setMaximumPoolSize(druidProperties.getMaxActive());
        datasource.setPoolName("ds0");
        return datasource;
    }

    /**
     * 获取多数据源配置
     * @return
     */
    public Map<String,DataSource> getDataSources(){
        Map<String,DataSource> allDataSources = new HashMap<>();
        String configs =druidProperties.getMultconfig();
        if(StringUtils.isNotEmpty(configs)){
            JSONArray configjsons= JSONArray.parseArray(configs);
            for(int i=0;i<configjsons.size();i++){

                JSONObject configObject=configjsons.getJSONObject(i);

                HikariDataSource datasource = new HikariDataSource();
                datasource.setJdbcUrl(configObject.getString("url"));
                datasource.setUsername(DESUtil.decrypt(configObject.getString("username")));
                datasource.setPassword(DESUtil.decrypt(configObject.getString("password")));
                datasource.setDriverClassName(druidProperties.getDriverClassName());
                //datasource.setInitialSize(druidProperties.getInitialSize());
                datasource.setMinimumIdle(druidProperties.getMinIdle());
                datasource.setMaximumPoolSize(druidProperties.getMaxActive());
                datasource.setPoolName(configObject.getString("name"));

                allDataSources.put(configObject.getString("name"),datasource);
            }
            return allDataSources;
        }
        return null;
    }


    //分表配置
    @Bean("dataSource")
    public DataSource shardingDataSource(){
        // 配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds0",dataSource());

        if(getDataSources()!=null) {
            dataSourceMap.putAll(getDataSources());
        }

        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        System.out.println("初始化分表配置开始:" + druidProperties.getMultconfig());

        JSONArray tbjsons=JSONArray.parseArray(druidProperties.getTableconfig().toString());

        for (Object item:tbjsons){
            System.out.println(item.toString());
            JSONObject jobj=(JSONObject)item;
            if (jobj.containsKey("IsSplitTable")&&jobj.getString("IsSplitTable").equals("1")) {
                //分片表的配置
                ShardingTableRuleConfiguration trc = new ShardingTableRuleConfiguration(jobj.getString("logicTable"),
                        jobj.getString("actualDataNodes"));
                trc.setTableShardingStrategy(new StandardShardingStrategyConfiguration(jobj.getString("colName"),
                        jobj.getString("logicTable")+"_tableShardingAlgorithm"));

                // 配置分表算法
                Properties tableShardingAlgorithmrProps = new Properties();
                tableShardingAlgorithmrProps.setProperty("algorithm-expression", jobj.getString("actualExpression"));
                shardingRuleConfiguration.getShardingAlgorithms().put(jobj.getString("logicTable")+"_tableShardingAlgorithm",
                        new ShardingSphereAlgorithmConfiguration("INLINE", tableShardingAlgorithmrProps));
                shardingRuleConfiguration.getTables().add(trc);
            }else{
                ShardingTableRuleConfiguration trc = new ShardingTableRuleConfiguration(jobj.getString("logicTable"),
                        jobj.getString("actualDataNodes"));
                shardingRuleConfiguration.getTables().add(trc);
            }
        }
        System.out.println("初始化分表配置结束");
        //默认数据源

        // 没有配置分库，直接返回datasource
        if(shardingRuleConfiguration.getTables().size()==0){
            return dataSource();
        }

        DataSource dataSource1 = null;
        try {
            Properties properties=new Properties();
            properties.setProperty("max.connections.size.per.query","50");
            properties.setProperty("sql.show", druidProperties.getShowsql());

            //System.out.println(JSON.toJSONString(dataSourceMap));
            dataSource1 = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfiguration), properties);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource1;
    }



    /**
     * 事务管理器配置
     */
    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }


    /**
     * configuration 配置
     * @return
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer(){
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                configuration.setMapUnderscoreToCamelCase(true);
                if (null!=druidProperties
                        && null!= druidProperties.getShowsql()
                        &&druidProperties.getShowsql().equals("false")) {
                    configuration.setLogImpl(org.apache.ibatis.logging.nologging.NoLoggingImpl.class);
                }else{
                    configuration.setLogImpl(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
                }
            }
        };
    }


    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // 设置MyBatis全局属性
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true); // 设置mapUnderscoreToCamelCase属性为true
        sessionFactory.setConfiguration(configuration);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        sessionFactory.setMapperLocations(resolver.getResources("classpath*:mapper/*Mapper.xml"));

        return sessionFactory;
    }

}
