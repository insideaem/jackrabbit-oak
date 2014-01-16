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
package org.apache.jackrabbit.oak.jcr.cluster;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.jcr.NodeStoreFixture;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.h2.util.Profiler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * A simple, temporary benchmark for many child nodes.
 */
public class ManyChildrenTest {

    NodeStoreFixture fixture = NodeStoreFixture.MONGO_NS;
//    NodeStoreFixture fixture = NodeStoreFixture.MONGO_MK;
//    NodeStoreFixture fixture = NodeStoreFixture.SEGMENT_MK;
    
    Session session;
    NodeStore nodeStore;
    
    @Before
    public void init() throws RepositoryException {
        
        // to test in a cluster, use:
        // nodeStore = fixture.createNodeStore(1);
        nodeStore = fixture.createNodeStore();
        
        if (nodeStore != null) {
            Repository rep  = new Jcr(nodeStore).createRepository();
            session = rep.login(new SimpleCredentials("admin", "admin".toCharArray()));
        }
    }
    
    @After
    public void logout() {
        if (session != null) {
            session.logout();
            fixture.dispose(nodeStore);
        }
    }
    
    @Test
    public void manyChildrenWithoutIndex() throws Exception {
        if (session == null) {
            return;
        }
        Node index = session.getRootNode().getNode("oak:index");
        if (index.hasNode("nodetype")) {
            index.getNode("nodetype").remove();
            session.save();
        }
        long start = System.currentTimeMillis(), last = start;
        
        // to test with more nodes, use:
        // int count = 1000000;
        int count = 10;
        
        Profiler prof = null;
        String nodeType = "oak:Unstructured";
        if (session.getRootNode().hasNode("many")) {
            session.getRootNode().getNode("many").remove();
            session.save();
        }
        Node many = session.getRootNode().addNode("many", nodeType);
        for (int i = 0; i < count; i++) {
            Node n = many.addNode("test" + i, nodeType);
            n.setProperty("prop", i);
            if (i % 100 == 0) {
                session.save();
            }
            long now = System.currentTimeMillis();
            if (now - last > 5000) {
                int opsPerSecond = (int) (i * 1000 / (now - start));
                System.out.println(i + " ops; " + opsPerSecond + " op/s");
                last = now;
                if (prof != null) {
                    System.out.println(prof.getTop(5));
                }
                if (opsPerSecond < 1000 && i % 100 == 0) {
                    prof = new Profiler();
                    prof.startCollecting();
                }
            }
        }
        
        start = System.currentTimeMillis();
        last = start;

        for (int i = 0; i < count; i++) {
            Node n = many.getNode("test" + i);
            long x = n.getProperty("prop").getValue().getLong();
            assertEquals(i, x);
            long now = System.currentTimeMillis();
            if (now - last > 5000) {
                int opsPerSecond = (int) (i * 1000 / (now - start));
                System.out.println(i + " read ops; " + opsPerSecond + " op/s");
                last = now;
                if (prof != null) {
                    System.out.println(prof.getTop(5));
                }
                if (opsPerSecond < 1000 && i % 100 == 0) {
                    prof = new Profiler();
                    prof.startCollecting();
                }
            }
        }
    }
}