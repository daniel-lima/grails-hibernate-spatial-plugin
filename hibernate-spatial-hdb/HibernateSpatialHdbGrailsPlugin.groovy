class HibernateSpatialHdbGrailsPlugin {
    def version = "0.0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [hibernateSpatial: '0.0.1 > *']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        'grails-app/scripts/**/Eclipse.groovy',
            'grails-app/views/**/*'
    ]

    // TODO Fill in these fields
    def author = 'Daniel Henrique Alves Lima'
    def authorEmail = 'email_daniel_h@yahoo.com.br'
    def title = 'Hibernate Spatial H2 Grails Plugin'
    def description = '''\\
Brief description of the plugin.
'''


    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/hibernate-spatial-hdb"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
