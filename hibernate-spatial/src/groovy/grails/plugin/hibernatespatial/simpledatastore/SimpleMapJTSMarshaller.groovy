package grails.plugin.hibernatespatial.simpledatastore

import org.grails.datastore.mapping.engine.types.AbstractMappingAwareCustomTypeMarshaller
import org.grails.datastore.mapping.query.Query
import org.grails.datastore.mapping.simple.query.SimpleMapResultList
import org.grails.datastore.mapping.model.*
import com.vividsolutions.jts.geom.*

/**
 * A marshaller for Joda-Time types usable in the Simple Map datastore.
 * @param < T >
 */
class SimpleMapJTSMarshaller<T> extends AbstractMappingAwareCustomTypeMarshaller<T, Map, SimpleMapResultList> {

    SimpleMapJTSMarshaller(Class<T> targetType) {
        super(targetType)
    }

    @Override
    protected Object writeInternal(PersistentProperty property, String key, T value, Map nativeTarget) {
        nativeTarget[key] = value
    }

    @Override
    protected T readInternal(PersistentProperty property, String key, Map nativeSource) {
        nativeSource[key]
    }

    private static final SUPPORTED_OPERATIONS = [Query.Equals, Query.NotEquals]
    private static final SUPPORTED_OPERATIONS_FOR_COMPARABLE = SUPPORTED_OPERATIONS

    @Override
    protected void queryInternal(PersistentProperty property, String key, Query.PropertyCriterion criterion, SimpleMapResultList nativeQuery) {
        def supportedOperations = Comparable.isAssignableFrom(targetType) ? SUPPORTED_OPERATIONS_FOR_COMPARABLE : SUPPORTED_OPERATIONS
        def op = criterion.getClass()
        if (op in supportedOperations) {
            Closure handler = nativeQuery.query.handlers[op]
            nativeQuery.results << handler.call(criterion, property)
        } else {
            throw new RuntimeException("unsupported query type $criterion for property $property")
        }
    }

    static final Iterable<Class> SUPPORTED_TYPES = [Geometry, GeometryCollection, LineString, Point, Polygon, MultiLineString, MultiPoint, MultiPolygon, LinearRing, Puntal, Lineal, Polygonal]

    static initialize() {
        for (type in SUPPORTED_TYPES) {
            MappingFactory.registerCustomType(new SimpleMapJTSMarshaller(type))
        }
    }
}
