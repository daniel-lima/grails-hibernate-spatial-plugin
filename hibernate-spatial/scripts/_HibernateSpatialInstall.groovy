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
    _hibernateSpatialUpdateDataSourceConfig(dataSourceOptions)
    
    File configDir = new File(grailsSettings.baseDir, '/grails-app/conf/')
    File configFile = new File(configDir, 'Config.groovy')
    
    if (configFile.exists()) {
        try {
            //File newConfigFile = new File(configDir, "${configFile.name}.tmp")
            File newConfigFile = File.createTempFile(configFile.name, 'tmp', configDir)
            boolean foundDefaultMapping = false
            boolean foundGeometryType = false
            boolean fileChanged = false
            
            Pattern userTypePattern = Pattern.compile('.*["\']user-type["\'].*')
            Pattern geometryTypePattern = Pattern.compile('.*\\W*GeometryUserType\\W*.*')
            newConfigFile.withPrintWriter {writer ->
                configFile.eachLine {line -> 
                    if (userTypePattern.matcher(line).matches()) {
                        foundDefaultMapping = foundDefaultMapping || true
                        if (!foundGeometryType) {
                            if (!geometryTypePattern.matcher(line).matches()) {
                                writer.println('   /* Added by the Hibernate Spatial Plugin. */')
                                writer.println('   \'user-type\'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Geometry)')
                                fileChanged = true
                            }
                            foundGeometryType = true 
                        }                                           
                    } 
                    writer.println(line)
                }
                
                if (!foundDefaultMapping) {
                    writer.println('/* Added by the Hibernate Spatial Plugin. */')
                    writer.println('grails.gorm.default.mapping = {')
                    writer.println('   \'user-type\'(type:org.hibernatespatial.GeometryUserType, class:com.vividsolutions.jts.geom.Geometry)')
                    writer.println('}')
                    fileChanged = true
                }
            }
            
            if (!fileChanged) {
                ant.delete(file: newConfigFile)
            } else {
                event "StatusUpdate", ["Updating ${configFile}"]
                ant.move(file: configFile, tofile: new File(configDir, "${configFile.name}.backup"))
                ant.move(file: newConfigFile, tofile: configFile)
            }
            
        } catch (Exception e) {
            event('StatusError', ["Could not update ${configFile}: ${e}"])
            throw e
        }
    }
}

_hibernateSpatialUpdateDataSourceConfig = {Map dataSourceOptions = [:] ->
    //println "dataSourceOptions ${dataSourceOptions}"
    
    boolean fileChanged = false
    List replacements = []
    Set unreplacedKeys = [] as Set
    
    for (option in dataSourceOptions.entrySet()) {
        Map replacement = [
            option: option,
            pattern: Pattern.compile("(\\S*\\s*${option.key}\\s*=\\s*)(.*)"),
            commentSub: '// $1$2',
            valueSub: '$2'
        ]
                
        replacements << replacement
    }
    
    //println "replacements ${replacements}"
    
    File configDir = new File(grailsSettings.baseDir, '/grails-app/conf/')
    for (fileName in ['DataSource.groovy', 'DataSources.groovy']) {
        File file = new File(configDir, fileName)
        //println "file ${file}"
        
        if (file.exists() && replacements.size() > 0) {
            try {
                //File newFile = new File(configDir, "${fileName}.tmp")
                newFile = File.createTempFile(fileName, 'tmp', configDir)
                newFile.withPrintWriter {writer ->
                    file.eachLine {line ->
                        String newLine = line
                        for (replacement in replacements) {
                            Matcher matcher = replacement.pattern.matcher(line)
                            //println "matches ${matcher.matches()}"
                            if (matcher.matches()) {
                                if (!matcher.replaceAll(replacement.valueSub).equals(replacement.option.value)) {
                                    newLine = matcher.replaceAll(replacement.commentSub)
                                    fileChanged = true                               
                                } else {
                                    unreplacedKeys << replacement.option.key
                                }
                                break
                            }
                        }
                        writer.println(newLine)
                    }
                    
                    if (dataSourceOptions.keySet() != unreplacedKeys) {
                       writer.println('')
                       writer.println('/* Added by the Hibernate Spatial Plugin. */')
                       writer.println('dataSource {')
                       for (option in dataSourceOptions.entrySet()) {
                           if (!unreplacedKeys.contains(option.key)) {
                               writer.println("   ${option.key} = ${option.value}")
                               fileChanged = true
                           }
                       }
                       writer.println('}')
                    }                    
                }
                
                //println "Updating ${file}"
                if (fileChanged) {
                    event "StatusUpdate", ["Updating ${file}"]
                    ant.move(file: file, tofile: new File(configDir, "${fileName}.backup"))
                    ant.move(file: newFile, tofile: file)
                } else {
                    ant.delete(file: newFile)
                }
                
            } catch (Exception e) {
                event('StatusError', ["Could not update ${file}: ${e}"])
                throw e
            }
        }
    }
}
