angular
    .module('eArkPlatform')
    .factory('userService', userService);

function userService($http) {
    return {
        deleteUser: deletePerson,
        getPerson: getPerson,
        createUser: createUser,
        updateUser: updateUser,
        getPersons: getPersons,
        changePassword: changePassword,
        uploadUsersCSVFile: uploadUsersCSVFile
    };

    function deletePerson(userName) {
        return $http.delete('/webapi/authentication/person/delete/' + userName).then(function (response) {
            return response.data;
        });
    }

    function getPerson(userName) {
        return $http.get('/webapi/authentication/person/' + userName).then(function (response) {
            return response.data;
        });
    }

    function createUser(userObj) {
        return $http.post('/webapi/person',
            userObj
        ).then(function (response) {
                return response.data;
            });
    }

    function updateUser(userObj) {
        return $http.put('/webapi/person?userName=' + encodeURIComponent(userObj.userName), userObj).then(
            function (response) {
                console.log("Return success");
                return response.data;
            }
        );
    }

    function getPersons(roleType) {
        var url = '/webapi/people?selectableType=';
        roleType (roleType && roleType.length > 0) ? url += roleType : url+= '*';
        return $http.get(url).then(function (result) {
            return result.data;
        });
    }

    function changePassword(user) {
        //To be implemented
    }

    function uploadUsersCSVFile(file) {
        //To be implemented if needed
    }

}
