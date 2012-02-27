// Copyright 2007, 2008, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.example.app0.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.hibernate.HibernateConfigurer;
import org.apache.tapestry5.hibernate.HibernateCoreModule;
import org.apache.tapestry5.hibernate.HibernateModule;
import org.apache.tapestry5.hibernate.HibernateSessionSource;
import org.apache.tapestry5.hibernate.HibernateSymbols;
import org.apache.tapestry5.hibernate.HibernateTransactionAdvisor;
import org.apache.tapestry5.hibernate.annotations.DefaultFactory;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;
import org.hibernate.cfg.Configuration;

import java.lang.annotation.Annotation;
import java.util.Properties;

// @SubModule just needed for developers running these tests within the IDE
@SubModule({HibernateModule.class, HibernateCoreModule.class})
public class AppModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(UserDAO.class, UserDAOImpl.class);
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(SymbolConstants.PRODUCTION_MODE, "false");
        configuration.add(HibernateSymbols.ENTITY_SESSION_STATE_PERSISTENCE_STRATEGY_ENABLED, "true");
        configuration.add(HibernateSymbols.DEFAULT_CONFIGURATION, "false");
    }

    @Match("*DAO")
    public static void adviseTransactions(HibernateTransactionAdvisor adviser,
                                          MethodAdviceReceiver receiver)
    {
        adviser.addTransactionCommitAdvice(receiver);
    }

    @Contribute(HibernateSessionSource.class)
    public static void configureHibernate(OrderedConfiguration<HibernateConfigurer> configurer){
        System.err.println("Configure Hibernate");
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

    @Contribute(ClientWhitelist.class)
    public static void provideWhitelistAnalyzer(OrderedConfiguration<WhitelistAnalyzer> configuration)
    {
        configuration.add("TestAnalyzer", new WhitelistAnalyzer()
        {

            public boolean isRequestOnWhitelist(Request request)
            {
                return true;
            }
        }, "before:*");
    }
}
