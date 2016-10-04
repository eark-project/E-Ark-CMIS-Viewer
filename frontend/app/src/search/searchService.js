angular
    .module('eArkPlatform')
    .factory('searchService', searchService);

function searchService($http, AIP_REPOSITORY_URI) {

    var service = {};

    service.aipSearch = function(term){
        return $http.get(AIP_REPOSITORY_URI.serviceProxy +'/select?'+ term).then(function(response){
            //debugger;
            return response.data.response;
        })
    };

    service.objectToQueryString = function (map) {
        // FIXME: need to implement encodeAscii!!
        var enc = encodeURIComponent, pairs = [];
        for (var name in map) {
            var value = map[name];
            var assign = enc(name) + "=";
            if (Array.isArray(value)) {
                for (var i = 0, l = value.length; i < l; ++i) {
                    pairs.push(assign + enc(value[i]));
                }
            } else {
                pairs.push(assign + enc(value));
            }
        }
        return pairs.join("&"); // String
    };

    return service;
}
