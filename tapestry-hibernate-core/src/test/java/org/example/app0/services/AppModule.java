package org.example.app0.services;

import org.apache.tapestry5.hibernate.HibernateConfigurer;
import org.apache.tapestry5.hibernate.HibernateCoreModule;
import org.apache.tapestry5.hibernate.HibernateSessionSource;
import org.apache.tapestry5.hibernate.HibernateSymbols;
import org.apache.tapestry5.hibernate.annotations.DefaultFactory;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.hibernate.cfg.Configuration;

import java.lang.annotation.Annotation;
import java.util.Properties;

@SubModule(HibernateCoreModule.class)
public class AppModule
{
    public void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(HibernateSymbols.DEFAULT_CONFIGURATION, "false");
    }

    @Contribute(HibernateSessionSource.class)
    public void configureHibernate(OrderedConfiguration<HibernateConfigurer> configurer)
    {
        configurer.add("default", new HibernateConfigurer()
        {
            public void configure(Configuration configuration)
            {
                Properties prop = new Properties();
                prop.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
                prop.put("hibernate.connection.url", "jdbc:hsqldb:mem:test");
                prop.put("hibernate.connection.username", "sa");
                prop.put("hibernate.connection.password", "");
                prop.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
                prop.put("hibernate.show_sql", "true");
                prop.put("hibernate.format_sql", "true");
                prop.put("hibernate.hbm2ddl.auto", "update");
                prop.put("hibernate.generate_statistics", "true");
                configuration.setProperties(prop);
            }

            public Class<? extends Annotation> getMarker()
            {
                return DefaultFactory.class;
            }

            public String[] getPackageNames()
            {
                return new String[]{
                        "org.example.app0.entities"
                };
            }
        });
    }
}
