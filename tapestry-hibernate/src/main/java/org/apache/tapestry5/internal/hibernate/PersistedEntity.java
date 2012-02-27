// Copyright 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.annotations.ImmutableSessionPersistedObject;
import org.apache.tapestry5.hibernate.HibernateServiceLocator;

import java.io.Serializable;

/**
 * Encapsulates a Hibernate entity name with an entity id.
 */
@ImmutableSessionPersistedObject
public class PersistedEntity implements Serializable
{
    private static final long serialVersionUID = 897120520279686518L;

    private final String entityName;

    private final Serializable id;

    public PersistedEntity(String entityName, Serializable id)
    {
        this.entityName = entityName;
        this.id = id;
    }

    @SuppressWarnings({"unchecked"})
    public Object restore(HibernateServiceLocator locator)
    {
        try
        {
            return locator.getSession(entityName).get(entityName, id);
        } catch (Exception ex)
        {
            throw new RuntimeException(
                    HibernateMessages.sessionPersistedEntityLoadFailure(entityName, id, ex));
        }
    }

    @Override
    public String toString()
    {
        return String.format("<PersistedEntity: %s(%s)>", entityName, id);
    }
}
