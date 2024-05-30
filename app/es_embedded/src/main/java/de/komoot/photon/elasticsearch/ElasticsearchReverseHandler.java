package de.komoot.photon.elasticsearch;

import org.locationtech.jts.geom.Point;
import de.komoot.photon.query.ReverseRequest;
import de.komoot.photon.searcher.PhotonResult;
import de.komoot.photon.searcher.ReverseHandler;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Execute a reverse lookup on a Elasticsearch database.
 */
public class ElasticsearchReverseHandler implements ReverseHandler {
    private Client client;
    private TimeValue queryTimeout;

    public ElasticsearchReverseHandler(Client client, int queryTimeoutSec) {
        this.client = client;
        queryTimeout = TimeValue.timeValueSeconds(queryTimeoutSec);
    }

    @Override
    public List<PhotonResult> reverse(ReverseRequest photonRequest) {
        ReverseQueryBuilder queryBuilder = buildQuery(photonRequest);
        SearchResponse results = search(queryBuilder.buildQuery(), photonRequest.getLimit(), photonRequest.getLocation(),
                photonRequest.getLocationDistanceSort());

        List<PhotonResult> ret = new ArrayList<>((int) results.getHits().getTotalHits());
        for (SearchHit hit : results.getHits()) {
            ret.add(new ElasticResult(hit));
        }

        return ret;
    }

    public String dumpQuery(ReverseRequest photonRequest) {
        return buildQuery(photonRequest).buildQuery().toString();
    }


    private SearchResponse search(QueryBuilder queryBuilder, int limit, Point location, boolean locationDistanceSort) {
        SearchRequestBuilder builder = client.prepareSearch(PhotonIndex.NAME).setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(queryBuilder).setSize(limit).setTimeout(queryTimeout);

        if (locationDistanceSort) {
            builder.addSort(SortBuilders.geoDistanceSort("coordinate", new GeoPoint(location.getY(), location.getX()))
                   .order(SortOrder.ASC));
        }

        return builder.execute().actionGet();
    }

    private ReverseQueryBuilder buildQuery(ReverseRequest photonRequest) {
        return ReverseQueryBuilder.
                builder(photonRequest.getLocation(), photonRequest.getRadius(), photonRequest.getQueryStringFilter(), photonRequest.getLayerFilters()).
                withOsmTagFilters(photonRequest.getOsmTagFilters());
    }
}
