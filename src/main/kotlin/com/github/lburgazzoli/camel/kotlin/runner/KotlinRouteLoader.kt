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

import org.apache.camel.builder.endpoint.EndpointRouteBuilder
import com.github.lburgazzoli.camel.kotlin.runner.dsl.IntegrationConfiguration
import org.apache.camel.support.ResourceHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class KotlinRouteLoader(private val path: String) : EndpointRouteBuilder() {
    companion object {
        val LOGGER : Logger = LoggerFactory.getLogger(KotlinRouteLoader::class.java)
    }

    @Throws(Exception::class)
    override fun configure() {
        val builder = this
        val compiler = JvmScriptCompiler()
        val evaluator = BasicJvmScriptEvaluator()
        val host = BasicJvmScriptingHost(compiler = compiler, evaluator = evaluator)
        val config = createJvmCompilationConfigurationFromTemplate<IntegrationConfiguration>()

        ResourceHelper.resolveMandatoryResourceAsInputStream(context, path).use { `is` ->
            val result = host.eval(
                    InputStreamReader(`is`).readText().toScriptSource(),
                    config,
                    ScriptEvaluationConfiguration {
                        //
                        // Arguments used to initialize the script base class
                        //
                        constructorArgs(builder)
                    }
            )

            for (report in result.reports) {
                when (report.severity) {
                    ScriptDiagnostic.Severity.ERROR -> LOGGER.error("{}", report.message, report.exception)
                    ScriptDiagnostic.Severity.WARNING -> LOGGER.warn("{}", report.message, report.exception)
                    else -> LOGGER.info("{}", report.message)
                }
            }
        }
    }
}
