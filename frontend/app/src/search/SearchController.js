angular
    .module('eArkPlatform.search')
    .controller('SearchController', SearchController);

/**
 * Main Controller for the Search module
 * @param $scope
 * @constructor
 */
function SearchController($scope, $stateParams, searchService) {
    var sctrl = this;
    sctrl.searchTerm = $stateParams.searchTerm ? $stateParams.searchTerm : "*" ;
    sctrl.selectedFilters = {}; //Keep track of the selected filters
    sctrl.filtersQueryString = ""; // the selected filters as query string
    //sctrl.definedFacets = searchService.getConfiguredFacets();
    sctrl.layout = 'grid';

    /**
     * Executes the main search function
     */
    function executeSearch() {

        var queryObj = {
            maxResults: 0,
            query: "",
            term: sctrl.searchTerm + '*'
        };
        var objQuerified = searchService.objectToQueryString(queryObj);
        getSearchQuery(objQuerified);
    }

    function getSearchQuery(query) {
        searchService.search(query).then(function (response) {
            sctrl.queryResult = response;
            if (response.numberFound > 0) {
                sctrl.fullSearchResults = {};
            }
        });
    }

}