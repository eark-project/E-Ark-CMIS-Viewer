angular
    .module('eArkPlatform.errors')
    .directive('errorMsg', errorMsg); 

function errorMsg(errorService) {
    
    return {
        restrict: 'E',
        templateUrl: './app/src/errors/view/error.html',
        controller: function(errorService) {
            var ectrl = this;
            
            ectrl.errData = errorService.obj;
            ectrl.closeErrorMsg = function() {
                ectrl.errData.errorVisible = false;
                ectrl.errData.errorMsg = '';
            };
            
        },
        controllerAs: 'ectrl'
    };
    
}