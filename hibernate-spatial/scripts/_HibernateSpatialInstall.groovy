/*
 * Copyright 2011 the original author or authors.
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
 * @author Daniel Henrique Alves Lima
 */

import java.util.regex.Matcher
import java.util.regex.Pattern

if (binding.variables.containsKey("_grails_hibernate_spatial_install_package_called")) {
    return
}

_grails_hibernate_spatial_install_package_called = true

includeTargets << grailsScript("_GrailsEvents")

_hibernateSpatialUpdateConfig = {Map dataSourceOptions = [:] ->
    //println "dataSourceOptions ${dataSourceOptions}"
    
    List replacements = [
    ]
    
    for (optionKey in dataSourceOptions.keySet()) {
        replacement = ["(\\S*\\s*${optionKey}\\s*=\\s*)(.*)", '// $1$2']
        replacement[0] = Pattern.compile(replacement[0].toString())
        replacements << replacement
    }
    
    //println "replacements ${replacements}"
    
    File configDir = new File(grailsSettings.baseDir, '/grails-app/conf/')
    for (fileName in ['DataSource.groovy', 'DataSources.groovy']) {
        File file = new File(configDir, fileName)
        //println "file ${file}"
        
        if (file.exists()) {
            try {
                File newFile = new File(configDir, "${fileName}.tmp")
                newFile.withPrintWriter {writer ->
                    file.eachLine {line ->
                        String newLine = line
                        for (replacement in replacements) {
                            Matcher matcher = replacement[0].matcher(line)
                            //println "matches ${matcher.matches()}"
                            if (matcher.matches()) {
                                newLine = matcher.replaceAll(replacement[1])
                                break
                            }
                        }
                        writer.println(newLine)
                    }
                    
                    writer.println('')
                    writer.println('/* Added by Hibernate Spatial Plugin. */ ')
                    writer.println('dataSource {')
                    for (option in dataSourceOptions.entrySet()) {
                        writer.println("   ${option.key} = ${option.value}")
                    }
                    writer.println('}')
                }
                
                println "Updating ${file}"
                ant.move(file: file, tofile: new File(configDir, "${fileName}.backup"))
                ant.move(file: newFile, tofile: file)
                
            } catch (Exception e) {
                event('StatusError', ["Could not update ${file}: ${e}"])
                throw e
            }
        }
    }
}
