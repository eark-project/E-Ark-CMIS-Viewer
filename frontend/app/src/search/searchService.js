angular
    .module('eArkPlatform')
    .factory('searchService', searchService);

function searchService($http) {
    var service = {};

    /**
     * Could we just use the live search results and return a concatenation of the results??
     * Thoughts: What about faceting?
     *
     * @param term
     * @returns {*}
     */
    service.search = function (term) {
    };

    /**
     * summary:
     *        takes a name/value mapping object and returns a string representing
     *        a URL-encoded version of that object.
     * example:
     *        this object:
     *    {
         *		blah: "blah",
         *		multi: [
         *			"thud",
         *			"thonk"
         *	    ]
         *	};
     *
     *    yields the following query string: "blah=blah&multi=thud&multi=thonk"
     *
     * credit to alfresco Aikau developers.
     * @param map
     * @returns {string}
     */
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