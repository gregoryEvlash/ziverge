# ziverge
Ziverge test app

To run this application you could just run `./run.sh` provide there executable binary file path and input start and end date in ISO Date Time format e.g. `2000-10-31T01:30:00.000Z`

this script start binary file with creation of buffer file for app reading. 

## Data storage
As a data storage is used plain immutable map. Because im not sure of event type range, i assumed it could be any string.
So thats why storage has nested maps structure. Otherwise id make event types as defined sealed objects. 
This would simplify storage and as Word count map id use scala.TrieMap, it is more sufficient for search by string. 
Main disadvantage of TrieMap - its mutable structure. So im unable to guaranteed proper work in case high load and storage structure with not predefined event types and mutable map.

## Rest API

Its possible to fetch information about counted numbers via rest api.

`/count/all`  return JSON with info about whole counted words and types

`/count/$EVENT_TYPE` return json with counted words for particular event type

`/count/$EVENT_TYPE/$WORD` return Int for event type and word combination

## Technical decision
In case of reading event window i choose plain filtration in counting stream. Because its a black box binary and i cant guarantee that, for instance, event with valid for count timestamp won come later, because of network lag (if we receive it via network) or wrong output.
