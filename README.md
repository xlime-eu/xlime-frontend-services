Web-service layer for the [xLiMe project](http://xlime.eu) that facilitates building user interfaces.

# Services

## `mediaItem`

 Returns UI friendly representations of xLiMe media-items (i.e. news articles, tv programmes or social-media posts), based on their URI.
 This service returns the main information of the media-items (e.g. title, publisher, description), but not any additional annotations 
 extracted by the xLiMe platform. See also service `annotationsFor. 

 * input parameters:
 ** **url**: one or more URLs of xLiMe media-items. E.g. `http://ijs.si/article/367691329`, `http://zattoo.com/program/111851734` or `http://vico-research.com/social/056eeb12-6a21-38af-b40c-94fbabe8628f`
 * output: json object describing the requested media-items (and/or a list of errors when trying to retrieve the media items).
 
## `latestMediaItems`

 Returns UI friendly representations of xLiMe media-items that have been recently published.
 
 * input parameters:
 ** **minutes**: integer value indicating the number of minutes before the current time to look for media items. Default value is `5`. 
 * output: json object with a list of "recent" media items as indicated by the `minutes` input parameter. This service may impose a limit on the number of media-items returned. Also, if the window of time is very large, it may return media-items within the window, but which are not "recent" anymore. 
 
## `entities`

 Returns UI friendly representations of entities defined in some Knowledge Base (typically DBpedia or Wikidata) used by the xLiMe platform to annotate media-items.
 
 * input parameters:
 ** **url**: one or more URLs of KB entities for which you want to receive a UI friendly representation.
 * output: json object describing the requested KB entities when supported by the xLiMe platform.
  
## `annotationsFor`
 
 Returns UI friendly representations of annotations for a given xLiMe media-item.
 
 * input parameters:
 ** **url**: one or more URLs of xLiMe media-items for which you want to get annotations.
 * output: json object describing the xLiMe annotations of the media-items.

# Running

Until we provide releases, you can run a local instance of this project by executing

  mvn jetty:run

##Notes
Currently this project requires:
* access to credentials to non-public back-end xLiMe services such as the `xLiMe` Sparql endpoint
* `KOntology`, a proprietary library for executing Sparql queries

Furthermore, to work correctly, a custom version of `guava-cache-overflow-extension`


Licensed under the Apache Software License, Version 2.0

