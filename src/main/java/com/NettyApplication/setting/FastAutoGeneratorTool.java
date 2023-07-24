package com.NettyApplication.setting;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class FastAutoGeneratorTool {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                                "jdbc:mysql://180.76.235.69:3306/hardware-ai?serverTimezone=GMT%2B8&allowMultiQueries=true",
                        "root",
                        "aY)I&iBop[F3t")
                .globalConfig(
                        builder -> {
                            builder.author("yb")
                                    //.enableSwagger() // 开启 swagger 模式
                                    .fileOverride() // 覆盖已生成文件
                                    .outputDir("/Users/yubo/Documents/project/hardWareAI/src/main/java"); // 指定输出目录
                        })
                .packageConfig(builder -> {
                    builder.parent("com") // 设置父包名
                            .moduleName("NettyApplication") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "/Users/yubo/Documents/project/hardWareAI/src/main/resources/mapper")); // 设置mapperXml生成路径

                })
                .strategyConfig(builder -> {
                    builder.addInclude("t_hard_ware")
                            // 设置需要生成的表名
                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker 引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}