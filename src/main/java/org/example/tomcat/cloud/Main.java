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

package org.example.tomcat.cloud;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.cloud.KubernetesMemberProvider;
import org.apache.tomcat.cloud.MemberProvider;
import org.apache.tomcat.cloud.membership.DynamicMembershipService;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    private static final Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) throws LifecycleException, IOException {
        // Maximum logging
        Enumeration<String> x = LogManager.getLogManager().getLoggerNames();
        while (x.hasMoreElements()) {
            String s = x.nextElement();
            Logger log = LogManager.getLogManager().getLogger(s);
            log.setLevel(Level.FINE);
            for (Handler h : log.getHandlers())
                h.setLevel(Level.FINE);
        }

        // Embedded Tomcat Configuration:

        try {
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(8080); // HTTP port for Tomcat; make sure to set the same value in pom.xml
            tomcat.getConnector();

            // Servlet Configuration:
            File base = new File(System.getProperty("java.io.tmpdir"));

            if(args[0].equals("--war") && args.length > 1) {
                System.out.println("im in !");
                File war = new File(args[1]);
                Context ctx = tomcat.addWebapp("/", war.getAbsolutePath());
                ctx.setDistributable(true);
            }

            // Cluster configuration
            SimpleTcpCluster cluster = new SimpleTcpCluster();
            tomcat.getEngine().setCluster(cluster);

            GroupChannel channel = (GroupChannel) cluster.getChannel();

            // The interesting part: use DynamicMembershipService (with KubernetesMemberProvider)
            MemberProvider provider = new KubernetesMemberProvider();
            MembershipService service = new DynamicMembershipService(provider);
            channel.setMembershipService(service);

            // Start Tomcat
            tomcat.start();
            tomcat.getServer().await();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
