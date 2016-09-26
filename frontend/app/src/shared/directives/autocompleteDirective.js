angular
    .module('eArkPlatform')
    .directive('commonAutocomplete', autocomplete);

function autocomplete() {

    return {
        restrict: 'E',
        scope: {
            datasource: '=',
            required: '=',
            name: '@',
            list: '=',
            label: '@',
            selected: '=?'
        },
        controllerAs: 'vm',
        controller: '@',
        name: 'controllerName',
        bindToController: true,
        templateUrl: ''
    };
}