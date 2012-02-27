// Copyright 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.hibernate.integration;

import org.apache.tapestry5.internal.hibernate.PersistedEntity;
import org.apache.tapestry5.test.AbstractIntegrationTestSuite;
import org.example.app0.entities.User;
import org.testng.annotations.Test;

@Test(sequential = true, groups = "integration")
public class TapestryHibernateIntegrationTests extends AbstractIntegrationTestSuite
{
    public TapestryHibernateIntegrationTests()
    {
        super("src/test/webapp");
    }

    public void valueencode_all_entity_types() throws Exception
    {
        open("/encodeentities");

        assertEquals(getText("//span[@id='name']").length(), 0);

        // need to create an entity in order to link with one
        clickAndWait("link=create an entity");
        assertEquals(getText("//span[@id='name']"), "name");

        // should return null for missing objects
        open("/encodeentities/9999");
        assertEquals(getText("//span[@id='name']").length(), 0);
    }

    public void persist_entities()
    {
        open("/persistentity");
        assertEquals(getText("//span[@id='name']").length(), 0);

        clickAndWait("link=create entity");
        assertText("//span[@id='name']", "name");

        // shouldn't save the change to the name because it's reloaded every time
        clickAndWait("link=change the name");
        assertText("//span[@id='name']", "name");

        // can set back to null
        clickAndWait("link=set to null");
        assertEquals(getText("//span[@id='name']").length(), 0);

        // deleting an entity that is still persisted. just remove the entity from the session if it's not found.
        clickAndWait("link=create entity");
        assertText("//span[@id='name']", "name");
        clickAndWait("link=delete");
        assertEquals(getText("//span[@id='name']").length(), 0);

        // transient objects cannot be persisted
        clickAndWait("link=set to transient");
        assertTextPresent("Error persisting");
    }

    public void sso_entities()
    {
    	open("/ssoentity");
        assertEquals(getText("//span[@id='name']").length(), 0);
        assertText("//span[@id='persistedEntityClassName']", User.class.getName());

        clickAndWait("link=persist entity");
        assertText("//span[@id='name']", "name");
        assertText("//span[@id='persistedEntityClassName']", PersistedEntity.class.getName());

        // can set back to null
        clickAndWait("link=set to null");
        assertEquals(getText("//span[@id='name']").length(), 0);
        assertText("//span[@id='persistedEntityClassName']", User.class.getName());

        clickAndWait("link=persist entity");
        assertText("//span[@id='name']", "name");
        assertText("//span[@id='persistedEntityClassName']", PersistedEntity.class.getName());
        clickAndWait("link=delete");
        assertEquals(getText("//span[@id='name']").length(), 0);
        assertText("//span[@id='persistedEntityClassName']", User.class.getName());

        clickAndWait("link=persist entity");
        assertText("//span[@id='name']", "name");
        assertText("//span[@id='persistedEntityClassName']", PersistedEntity.class.getName());
        clickAndWait("link=set to transient");
        assertText("//span[@id='persistedEntityClassName']", User.class.getName());
    }

    /**
     * TAPESTRY-2244
     */
    public void using_cached_with_form()
    {
        start("Cached Form", "setup");
        assertTextSeries("name_%d", 0);

        type("name", "name1");
        clickAndWait(SUBMIT);
        assertTextSeries("name_%d", 0, "name1");

        type("name", "name2");
        clickAndWait(SUBMIT);
        assertTextSeries("name_%d", 0, "name1", "name2");
    }

    public void commit_after_on_component_methods()
    {
        start("CommitAfter Demo");

        assertText("name", "Diane");

        clickAndWait("link=change name");

        assertText("name", "Frank");

        clickAndWait("link=runtime exception");

        assertText("name", "Frank");

        clickAndWait("link=checked exception");

        assertText("name", "Troy");

    }

    public void grid()
    {
        start("Grid Demo", "setup");

        clickAndWait("link=First Name");

        assertText("//td[@class='firstName t-sort-column-ascending']", "Joe_1");

        clickAndWait("link=First Name");

        assertText("//td[@class='firstName t-sort-column-descending']", "Joe_9");
    }

    public void hibernate_statistics()
    {
    	open(BASE_URL + "hibernate/Statistics");

    	assertTextPresent("Hibernate Statistics");

    	assertTextPresent("Entities Statistics");

    	assertTextPresent(User.class.getName());
    }


}
