angular
    .module('eArkPlatform.erms')
    .controller('ErmsMenuController', ErmsMenuController);

function ErmsMenuController($scope, ermsService, $state) {
    
    var emc = this;
    emc.modules = ermsService.getModules();
    emc.currently = $state.current.name.substr(5,5);
    
}