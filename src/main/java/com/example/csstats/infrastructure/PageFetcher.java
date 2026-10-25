package com.example.csstats.infrastructure;

import java.net.URI;

public interface PageFetcher {

    /** Returns the fully rendered HTML of the given page. */
    String fetchHtml(URI url);
}
