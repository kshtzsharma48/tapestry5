// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.util;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.Orderable;
import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.testng.annotations.Test;

public class OrdererTest extends IOCInternalTestCase
{
    @Test
    public void no_dependencies()
    {
        Log log = newLog();

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void missing_constraint_type()
    {
        Log log = newLog();

        log.warn(UtilMessages.constraintFormat("fred", "foo.barney"));

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("foo.fred", "FRED");
        o.add("foo.barney", "BARNEY", "fred");
        o.add("foo.wilma", "WILMA");
        o.add("foo.betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void unknown_constraint_type()
    {
        Log log = newLog();

        log.warn(UtilMessages.constraintFormat("nearby:fred", "foo.barney"));

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("foo.fred", "FRED");
        o.add("foo.barney", "BARNEY", "nearby:fred");
        o.add("foo.wilma", "WILMA");
        o.add("foo.betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void nulls_not_included_in_result()
    {
        Log log = newLog();

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY");
        o.add("zippo", null);
        o.add("wilma", "WILMA");
        o.add("groucho", null);
        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void duplicate_id()
    {
        Log log = newLog();

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("foo.fred", "FRED");
        o.add("foo.barney", "BARNEY");
        o.add("foo.wilma", "WILMA");

        verify();

        log.warn(UtilMessages.duplicateOrderer("foo.fred"), null);

        replay();

        o.add("foo.fred", "FRED2");

        verify();

        replay();

        o.add("foo.betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void leader()
    {
        Log log = newLog();

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("foo.fred", "FRED");
        o.add("foo.barney", "BARNEY", "before:*.*");
        o.add("foo.wilma", "WILMA");
        o.add("foo.betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("BARNEY", "FRED", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void trailer()
    {
        Log log = newLog();

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("foo.fred", "FRED");
        o.add("foo.barney", "BARNEY", "after:*");
        o.add("foo.wilma", "WILMA");
        o.add("foo.betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered.get(3), "BARNEY");

        verify();
    }

    @Test
    public void prereqs()
    {
        Log log = newLog();

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("foo.fred", "FRED", "after:wilma");
        o.add("foo.barney", "BARNEY", "after:fred,betty");
        o.add("foo.wilma", "WILMA");
        o.add("foo.betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("WILMA", "FRED", "BETTY", "BARNEY"));

        verify();
    }

    @Test
    public void pre_and_post_reqs()
    {
        Log log = newLog();

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("foo.fred", "FRED", "after:wilma");
        o.add("foo.barney", "BARNEY", "after:fred,betty");
        o.add("foo.wilma", "WILMA");
        o.add("foo.betty", "BETTY", "before:wilma");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("BETTY", "WILMA", "FRED", "BARNEY"));

        verify();
    }

    @Test
    public void cross_module()
    {
        Log log = newLog();

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("foo.fred", "FRED", "after:*.wilma");
        o.add("bar.barney", "BARNEY", "after:*.fred,*.betty");
        o.add("baz.wilma", "WILMA");
        o.add("biff.betty", "BETTY", "before:baz.*");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("BETTY", "WILMA", "FRED", "BARNEY"));

        verify();
    }

    @Test
    public void dependency_cycle()
    {
        Log log = newLog();

        log.warn("Unable to add 'foo.barney' as a dependency of 'foo.betty', as that forms a "
                + "dependency cycle ('foo.betty' depends on itself via 'foo.barney'). "
                + "The dependency has been ignored.", null);

        replay();

        Orderer<String> o = new Orderer<String>(log);

        o.add("foo.fred", "FRED", "after:wilma");
        o.add("foo.barney", "BARNEY", "after:fred,betty");
        o.add("foo.wilma", "WILMA");
        o.add("foo.betty", "BETTY", "after:barney", "before:wilma");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("BETTY", "WILMA", "FRED", "BARNEY"));

        verify();
    }

    @Test
    public void toString_Orderable()
    {
        Orderable<String> simple = new Orderable<String>("simple", "SIMPLE");

        assertEquals(simple.toString(), "Orderable[simple SIMPLE]");

        Orderable<String> complex = new Orderable<String>("complex", "COMPLEX", "after:foo",
                "before:bar");

        assertEquals(complex.toString(), "Orderable[complex after:foo before:bar COMPLEX]");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void toString_DependencyNode()
    {
        Log log = newLog();

        replay();

        DependencyNode<String> node1 = new DependencyNode<String>(log, new Orderable("node1",
                "NODE1"));

        assertEquals(node1.toString(), "[node1]");

        DependencyNode<String> node2 = new DependencyNode<String>(log, new Orderable("node2",
                "NODE2"));

        DependencyNode<String> node3 = new DependencyNode<String>(log, new Orderable("node3",
                "NODE3"));

        DependencyNode<String> node4 = new DependencyNode<String>(log, new Orderable("node4",
                "NODE4"));

        DependencyNode<String> node5 = new DependencyNode<String>(log, new Orderable("node5",
                "NODE5"));

        node2.addDependency(node1);
        node1.addDependency(node3);
        node1.addDependency(node4);
        node5.addDependency(node1);

        assertEquals(node5.toString(), "[node5: [node1: [node3], [node4]]]");

        verify();
    }
}
