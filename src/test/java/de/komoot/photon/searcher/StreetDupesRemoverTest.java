package de.komoot.photon.searcher;

import de.komoot.photon.Constants;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StreetDupesRemoverTest {

    @Test
    void testDeduplicatesStreets() {
        StreetDupesRemover streetDupesRemover = new StreetDupesRemover("en");
        List<PhotonResult> allResults = new ArrayList<>();
        allResults.add(createDummyResult("99999", "Main Street", "highway", "Unclassified"));
        allResults.add(createDummyResult("99999", "Main Street", "highway", "Unclassified"));

        List<PhotonResult> dedupedResults = streetDupesRemover.execute(allResults);
        assertEquals(1, dedupedResults.size());
    }

    @Test
    void testStreetAndBusStopNotDeduplicated() {
        StreetDupesRemover streetDupesRemover = new StreetDupesRemover("en");
        List<PhotonResult> allResults = new ArrayList<>();
        allResults.add(createDummyResult("99999", "Main Street", "highway", "bus_stop"));
        allResults.add(createDummyResult("99999", "Main Street", "highway", "Unclassified"));

        List<PhotonResult> dedupedResults = streetDupesRemover.execute(allResults);
        assertEquals(2, dedupedResults.size());
    }
    
    private PhotonResult createDummyResult(String postCode, String name, String osmKey,
                    String osmValue) {
        return new MockPhotonResult()
                .put(Constants.POSTCODE, postCode)
                .putLocalized(Constants.NAME, "en", name)
                .put(Constants.OSM_KEY, osmKey)
                .put(Constants.OSM_VALUE, osmValue);
    }

}
