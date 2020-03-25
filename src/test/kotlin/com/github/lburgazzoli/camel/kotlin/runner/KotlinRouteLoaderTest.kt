/**
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
package com.github.lburgazzoli.camel.kotlin.runner

import org.apache.camel.impl.DefaultCamelContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class KotlinRouteLoaderTest {
    @Test
    fun `load routes with components configuration`() {
        val route ="""            
            camel {
                components {
                    component<org.apache.camel.component.seda.SedaComponent>("seda") {
                        queueSize = 1234
                        concurrentConsumers = 12
                    }            
                    component<org.apache.camel.component.seda.SedaComponent>("mySeda") {
                        queueSize = 4321
                        concurrentConsumers = 21
                    }
                }
            }
        """.trimIndent()

        route.byteInputStream(StandardCharsets.UTF_8).use {
            val context = DefaultCamelContext()

            context.addRoutes(KotlinRouteLoader.load(it))
            context.start()

            val seda = context.getComponent("seda")
            val mySeda = context.getComponent("mySeda")

            assertThat(seda).hasFieldOrPropertyWithValue("queueSize", 1234)
            assertThat(seda).hasFieldOrPropertyWithValue("concurrentConsumers", 12)
            assertThat(mySeda).hasFieldOrPropertyWithValue("queueSize", 4321)
            assertThat(mySeda).hasFieldOrPropertyWithValue("concurrentConsumers", 21)
        }
    }
}