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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.hibernate.HibernateServiceLocator;
import org.apache.tapestry5.test.TapestryTestCase;
import org.hibernate.Session;
import org.testng.annotations.Test;

@Test
public class EntityPersistentFieldStrategyTest extends TapestryTestCase
{
    public void not_an_entity()
    {
        String nonEntity = "foo";
        HibernateServiceLocator locator = mockHibernateEntityServiceLocator();

        EntityPersistentFieldStrategy strategy = new EntityPersistentFieldStrategy(locator, null);

        expect(locator.getSession(String.class)).andThrow(new IllegalArgumentException());

        replay();

        try
        {
            strategy.postChange("pageName", "", "fieldName", nonEntity);

            unreachable();
        } catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Failed persisting an entity in the session. Only entities attached to a Hibernate Session can be persisted. entity: foo");
        }

        verify();
    }

    private Session mockHibernateSession()
    {
        return newMock(Session.class);
    }

    private HibernateServiceLocator mockHibernateEntityServiceLocator()
    {
        return newMock(HibernateServiceLocator.class);
    }
}
