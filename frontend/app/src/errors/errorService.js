angular
    .module('eArkPlatform.errors')
    .factory('errorService', errorService);

function errorService() {
    
    var service = {
        obj: {
            errorVisible: false,
            errorMsg : 'something'
        }    
    };
    
    service.displayErrorMsg = function(msg) {
        service.obj.errorVisible = true;
        service.obj.errorMsg = msg;
    };
    
    return service;

}