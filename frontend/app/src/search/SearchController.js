angular
    .module('eArkPlatform.search')
    .controller('SearchController', SearchController);

/**
 * Main Controller for the Search module
 * @param $scope
 * @constructor
 */

function SearchController($scope, searchService, fileUtilsService, basketService, sessionService, orderService, $state, $mdDialog) {
    
    var sctrl = this;
    
    sctrl.searchStr = '';
    sctrl.initialTerm = '';
    sctrl.searchInputs = [];
    sctrl.searchResults = basketService.currentSearch;
    sctrl.basket = [];
    sctrl.orderHistory = [];
    sctrl.orderBy = '-orderStatus';
    sctrl.filterBy = { title: '', packageId: '' };
    sctrl.state = $state;
    
    sctrl.sortThis = sortThis;
    sctrl.executeSearch = executeSearch;
    sctrl.addToBasket = basketCheck;
    sctrl.goToOrder = goToOrder;
    sctrl.addInput = addInput;
    sctrl.removeInput = removeInput;
    sctrl.helpfulSearchHints = helpfulSearchHints;
    sctrl.fileInfoDiag = fileInfoDiag;
    sctrl.addToBasket = basketCheck;

    function sortThis( $event, sortParameter ) {
        if (sctrl.orderBy === sortParameter) {
            sctrl.orderBy = '-' + sortParameter;
        } else if (sctrl.orderBy === '-' + sortParameter) {
            sctrl.orderBy = '';
        } else {
            sctrl.orderBy = sortParameter;
        }
    };

    function executeSearch() {
        sctrl.searchStr = 'content: ' + sctrl.initialTerm;
        
        for (var i in sctrl.searchInputs) {
            if (sctrl.searchInputs[i].term !== '') {
                sctrl.searchStr = sctrl.searchStr + ' ' + sctrl.searchInputs[i].operator + ' content: ' + sctrl.searchInputs[i].term + '';
            };
        };
    
        sctrl.searchResults = {};
        var queryObj = {
            q: sctrl.searchStr + ' AND path:*/representations/*/data/* AND NOT path:*_mig-*',
            rows: 25,
            start: 0,
            fl: 'package,size,path,confidential,contentType,textCategory,_version_,title,packageId,displaySize', //fields
            filter: 'package,size,path,confidential,contentType,textCategory', //fields
            sort :'package asc',
            wt: 'json'
        };
        var encTerm = searchService.objectToQueryString(queryObj);

        searchService.aipSearch(encTerm).then(function (response) {
            if (response.numFound > 0) {
                basketService.currentSearch = {
                    documents: response.docs, //An array of objects
                    numberFound: response.numFound
                };
                
                //Let's clean up some of the properties. Temporary solution
                basketService.currentSearch.documents.forEach(function (item) {
                    item.title = item.path.substring(item.path.lastIndexOf('/') + 1, item.path.lastIndexOf('.'));
                    if(item.package)
                        item.packageId = item.package.substring(item.package.lastIndexOf(':') + 1);
                    item.thumbnail = fileUtilsService.getFileIconByMimetype(item.contentType, 24);
                    item.displaySize = formatBytes(item.stream_size);
                });
                sctrl.searchResults = basketService.currentSearch;
            }
        });
    }

    function formatBytes(bytes, decimals) {
        if (bytes == 0) return '0 Byte';
        var k = 1000;
        var dm = decimals + 1 || 3;
        var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
        var i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    }

}