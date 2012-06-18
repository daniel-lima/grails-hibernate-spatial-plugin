eventTestPhaseStart = { phase ->
	if (phase == "unit") {
		event "StatusUpdate", ["configuring JTS support for simple datastore"]
		def marshallerClass = classLoader.loadClass("grails.plugin.hibernatespatial.simpledatastore.SimpleMapJTSMarshaller")
		marshallerClass.initialize()
	}
}
