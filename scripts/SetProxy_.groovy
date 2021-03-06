/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Gant script for setting HTTP proxy-settings.
 *
 * @author Sergey Nebolsin
 *
 * @since 0.5.5
 */

includeTargets << grailsScript("_GrailsEvents")
includeTargets << grailsScript("_GrailsProxy")

target ( "default" : "Sets HTTP proxy configuration for Grails") {
   depends(configureProxy)
   setProxy()
}

target(setProxy:"Implementation target") {
    ant.mkdir( dir:"${userHome}/.grails/scripts" )
    def scriptFile = new File("${userHome}/.grails/scripts/ProxyConfig.groovy")
    ant.input(addProperty:"proxy.use", message:"Do you wish to use HTTP proxy?",validargs:'y,n',defaultvalue:'y')
    if( ant.antProject.properties."proxy.use" == 'n' ) {
        scriptFile.delete()
        event("StatusFinal", [ "Grails is configured to not use HTTP proxy"])
    } else {
        def proxyHost = System.getProperty("http.proxyHost") ? System.getProperty("http.proxyHost") : 'localhost'
        def proxyPort = System.getProperty("http.proxyPort") ? System.getProperty("http.proxyPort") : '80'
        def proxyUser = System.getProperty("http.proxyUserName") ? System.getProperty("http.proxyUserName") : ''
        def proxyPassword = System.getProperty("http.proxyPassword") ? System.getProperty("http.proxyPassword") : ''
        ant.input(addProperty:"proxy.host", message:"Enter HTTP proxy host [${proxyHost}]: ",defaultvalue:proxyHost)
        ant.input(addProperty:"proxy.port", message:"Enter HTTP proxy port [${proxyPort}]: ",defaultvalue:proxyPort)
        ant.input(addProperty:"proxy.user", message:"Enter HTTP proxy username [${proxyUser}]: ",defaultvalue:proxyUser)
        ant.input(addProperty:"proxy.password", message:"Enter HTTP proxy password [${proxyPassword}]: ",defaultvalue:proxyPassword)
        scriptFile.delete()
        scriptFile << "// This file is generated automatically with 'grails set-proxy' command\n"
        scriptFile << "proxyConfig = [proxyHost:'${ant.antProject.properties.'proxy.host'}',proxyPort:'${ant.antProject.properties.'proxy.port'}',proxyUser:'${ant.antProject.properties.'proxy.user'}',proxyPassword:'${ant.antProject.properties.'proxy.password'}']"
        event("StatusFinal", [ "Grails is configured to use HTTP proxy"])
    }
}
