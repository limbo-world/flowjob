package org.limbo.flowjob.tracker.dao.test;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/**
 * 工具类，帮助生成MyBatis的xml配置
 *
 * @author Brozen
 * @since 2021-06-02
 */
public class MyBatisGenerator {

    /**
     * 项目根目录
     */
    private static final String PROJECT_DIR = System.getProperty("user.dir");

    /**
     * 传入相对路径，返回绝对路径。
     * @param relativePath 相对于项目根目录的相对路径。
     * @return 绝对路径
     */
    public static String path(String relativePath) {
        return PROJECT_DIR + (relativePath.startsWith("/") ? relativePath : ("/" + relativePath));
    }

    String[] tables;

    public MyBatisGenerator(String... tables) {
        this.tables = tables;
    }

    public void generate() {

        AutoGenerator generator = new AutoGenerator();
        generator.setGlobalConfig(createGlobalConfig());
        generator.setDataSource(createDataSourceConfig());

        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setController("");// 不生成controller
        templateConfig.setService("");// 不生成service
        templateConfig.setServiceImpl("");
        generator.setTemplate(templateConfig);

        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setEntityLombokModel(true);// 使用Lombok
        strategyConfig.setNaming(NamingStrategy.underline_to_camel);// 表下划线命名转驼峰
        strategyConfig.setEntitySerialVersionUID(true);// 生成序列号
        strategyConfig.setInclude(tables);// 生成的表名
        generator.setStrategy(strategyConfig);

        generator.execute();

    }

    private GlobalConfig createGlobalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOutputDir(path("/job-tracker-dao/src/test/java"));
        globalConfig.setAuthor("Brozen");
        globalConfig.setOpen(false);
        globalConfig.setBaseResultMap(true);// 生成 <resultMap> 标签
        globalConfig.setBaseColumnList(true);// 生成包含所有字段的 <sql>
        return globalConfig;
    }

    private DataSourceConfig createDataSourceConfig() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl("jdbc:mysql://10.219.153.31:3306/flow_job?serverTimezone=GMT%2B8");
        dataSourceConfig.setDriverName("com.mysql.cj.jdbc.Driver");
        dataSourceConfig.setUsername("brozen");
        dataSourceConfig.setPassword("159000");
        return dataSourceConfig;
    }


    public static void main(String[] args) {
        new MyBatisGenerator("worker_statistics").generate();
    }
}
