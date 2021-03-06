/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.document.util;

import com.google.common.base.Strings;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.jackrabbit.oak.plugins.document.Revision;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link Utils}.
 */
public class UtilsTest {

    @Test
    public void getPreviousIdFor() {
        Revision r = new Revision(System.currentTimeMillis(), 0, 0);
        assertEquals("2:p/" + r.toString() + "/0",
                Utils.getPreviousIdFor("/", r, 0));
        assertEquals("3:p/test/" + r.toString() + "/1",
                Utils.getPreviousIdFor("/test", r, 1));
        assertEquals("15:p/a/b/c/d/e/f/g/h/i/j/k/l/m/" + r.toString() + "/3",
                Utils.getPreviousIdFor("/a/b/c/d/e/f/g/h/i/j/k/l/m", r, 3));
    }

    @Test
    public void getParentIdFromLowerLimit() throws Exception{
        assertEquals("1:/foo",Utils.getParentIdFromLowerLimit(Utils.getKeyLowerLimit("/foo")));
        assertEquals("1:/foo",Utils.getParentIdFromLowerLimit("2:/foo/bar"));
    }

    @Test
    public void getParentId() throws Exception{
        String longPath = PathUtils.concat("/"+Strings.repeat("p", Utils.PATH_LONG + 1), "foo");
        assertTrue(Utils.isLongPath(longPath));

        assertNull(Utils.getParentId(Utils.getIdFromPath(longPath)));

        assertNull(Utils.getParentId(Utils.getIdFromPath("/")));
        assertEquals("1:/foo",Utils.getParentId("2:/foo/bar"));
    }

    @Ignore("Performance test")
    @Test
    public void performance_getPreviousIdFor() {
        Revision r = new Revision(System.currentTimeMillis(), 0, 0);
        String path = "/some/test/path/foo";
        // warm up
        for (int i = 0; i < 1 * 1000 * 1000; i++) {
            Utils.getPreviousIdFor(path, r, 0);
        }
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10 * 1000 * 1000; i++) {
            Utils.getPreviousIdFor(path, r, 0);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(time);
    }

    @Ignore("Performance test")
    @Test
    public void performance_revisionToString() {
        Revision r = new Revision(System.currentTimeMillis(), 0, 0);
        // warm up
        for (int i = 0; i < 1 * 1000 * 1000; i++) {
            r.toString();
        }
        long time = System.currentTimeMillis();
        for (int i = 0; i < 30 * 1000 * 1000; i++) {
            r.toString();
        }
        time = System.currentTimeMillis() - time;
        System.out.println(time);
    }
}
