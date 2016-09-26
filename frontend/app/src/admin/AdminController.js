angular
    .module('eArkPlatform.administration')
    .controller('AdminController', AdminController);

/**
 * Main Controller for the Admin module
 * @param $scope
 * @constructor
 */
function AdminController($scope) {
    initTab();

    function initTab() {
        $scope.$on('$stateChangeSuccess', function (event, toState) {
            $scope.currentTab = toState.data.selectedTab;
        });
    }
}