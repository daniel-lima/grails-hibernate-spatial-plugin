import com.vividsolutions.jts.geom.*

/**
 * This test only fails when done as a grails 2 style tests (using @TestFor etc.) rather than 
 * a grails 1 style test (i.e. extending GrailsUnitTest, mockDomain etc).
 */
@TestFor(PointOfInterest)
class PointOfInterestTests
{
    void testFindByLocation()
	{
		Point location = new GeometryFactory().createPoint(new Coordinate(12f, 34f))
		PointOfInterest placeOne = new PointOfInterest(location: location)
		placeOne.save()

		PointOfInterest foundPlace = PointOfInterest.findByLocation(location)
		assertNotNull(foundPlace)

		PointOfInterest notFoundPlace = PointOfInterest.findByLocation(new GeometryFactory().createPoint(new Coordinate(1f, 2f)))
		assertNull(notFoundPlace)
    }
}
