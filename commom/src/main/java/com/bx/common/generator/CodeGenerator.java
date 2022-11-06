package com.bx.common.generator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.ArrayList;
import java.util.List;


public class CodeGenerator {
    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        //生成的代码输出路径，自己根据需要修改
        String projectPath = "d:\\work\\project\\code";
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor("blue");
        gc.setOpen(false);
        gc.setFileOverride(true);
        gc.setActiveRecord(true);
        gc.setBaseColumnList(true);
        gc.setBaseResultMap(true);
        gc.setIdType(IdType.AUTO);
        gc.setDateType(DateType.ONLY_DATE);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://localhost:3306/box-im?useUnicode=true&characterEncoding=utf-8");
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("root");
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName("");
        pc.setParent("com.bx");
        mpg.setPackageInfo(pc);

        // 如果模板引擎是 velocity
        String templatePath = "/templates/mapper.xml.vm";

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/"
                        + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        // 下划线转驼峰
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityTableFieldAnnotationEnable(true);
        strategy.setVersionFieldName("version");
        //逻辑删除的字段
        strategy.setLogicDeleteFieldName("deleted");
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);


        //多张表的时候直接在代码中写表名
        strategy.setInclude("friends");
        strategy.setTablePrefix("");
        mpg.setStrategy(strategy);

        mpg.setTemplateEngine(new VelocityTemplateEngine());
        mpg.execute();
    }
}
