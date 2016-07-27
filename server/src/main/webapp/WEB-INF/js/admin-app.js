(function() {

    var app = angular.module('admin', ['ngRoute', 'ngCookies','angularModalService', 'ngAnimate']);
    var adminAccessToken = null;
    var contextURL = getContextURL();

    app.config(function($routeProvider) {
        $routeProvider.when('/users',{
            templateUrl : 'users.html'
        }).when('/clients',{
            templateUrl : 'clients.html'
        }).when('/approvals',{
            templateUrl : 'approvals.html'
        }).when('/tokens',{
            templateUrl : 'tokens.html'
        }).otherwise({
            templateUrl : 'page0.html'
        }); 
    });

    // ===========================
    // Client Controller
    // ===========================
    app.controller('clientController', function($scope,$http) {
        $scope.clients = [];
        $scope.error = null;
        $scope.wheel = false;

        $scope.getClients = function() {
            $scope.wheel = true;
            var request = {
                method : 'GET',
                url : contextURL + '/admin/clients',
                headers : {'Authorization' : 'Bearer ' + adminAccessToken}
            };
            $http(request).then(
                function(response) {
                    $scope.clients = response.data;
                    $scope.wheel = false;
                    $scope.clients.sort(function(c1,c2){return c1.client_number - c2.client_number;});
                },
                function(response) {
                    $scope.clients = null;
                    $scope.wheel = false;
                    $scope.error = formatError(response);}
            );
        };
        $scope.getClients();
    });

    // ===========================
    // User Controller
    // ===========================
    app.controller('userController', function($scope,$http) {
        $scope.users = [];
        $scope.error = null;
        $scope.wheel = false;
        $scope.getUsers = function() {
            $scope.wheel = true;
            var request = {
                method : 'GET',
                url : contextURL + '/admin/users',
                headers : {'Authorization' : 'Bearer ' + adminAccessToken}
            };
            $http(request).then(
                function(response) {
                    $scope.users = response.data;
                    $scope.wheel = false;
                },
                function(response) {
                    $scope.users = null;
                    $scope.wheel = false;
                    $scope.error = formatError(response);
                }
            );
        };
        $scope.getUsers();
    });

    // ===========================
    // Approval Controller
    // ===========================
    app.controller('approvalController', function($scope,$http) {
        $scope.approvals = null;
        $scope.error = null;
        $scope.wheel = false;

        $scope.getApprovals = function() {
            $scope.wheel = true;
            var request = {
                method : 'GET',
                url : contextURL + '/admin/approvals',
                headers : {'Authorization' : 'Bearer ' + adminAccessToken}
            };
            $http(request).then(
                function(response) {$scope.wheel = false; $scope.approvals = response.data;},
                function(response) {$scope.wheel = false; $scope.approvals = null; $scope.error = formatError(response);}
            );
        };
        $scope.getApprovals();

        $scope.revoke = function(u,c,s) {
            $scope.wheel = true;
            var request = {
                method : 'DELETE',
                url : contextURL + '/admin/approvals?user=' + u + '&client=' + c + '&scope=' + s,
                headers : {'Authorization' : 'Bearer ' + adminAccessToken}
            };
            $http(request).then(
                function(response) {$scope.wheel = false; $scope.getApprovals();},
                function(response) {$scope.wheel = false; $scope.error = formatError(response);}
            );
        };

    });

    // ===========================
    // Token Details Controller
    // ===========================
    app.controller('tokenDetailsController', ['$scope', '$http', 'close', 'token', function($scope, $http, close, token) {
        $scope.close = close;
        $scope.token = token;
        $scope.data = null;
        $scope.wheel = true;
        var request = {
            method : 'GET',
            url : contextURL + '/user',
            headers : {'Authorization' : 'Bearer ' + token}
        };
        $http(request).then(
            function(response) {$scope.wheel = false; $scope.data = response.data;},
            function(response) {$scope.wheel = false; $scope.data = null;}
        );
    }]);

    // ===========================
    // Token Controller
    // ===========================
    app.controller('tokenController', function($scope,$http,ModalService) {
        $scope.tokens = null;
        $scope.error = null;
        $scope.wheel = false;

        $scope.tokenPopup = function(t) {
            ModalService.showModal({
                templateUrl : "tokenDetails.html",
                controller : "tokenDetailsController",
                inputs : {token : t}
            }).then(
                function(modal) {
                    modal.close.then(function(result) {
                    $scope.result = result;});
                }
            );
        };

        $scope.getTokens = function() {
            $scope.wheel = true;
            var request = {
                method : 'GET',
                url : contextURL + '/admin/tokens',
                headers : {'Authorization' : 'Bearer ' + adminAccessToken}
            };
            $http(request).then(
                function(response) {$scope.wheel = false; $scope.tokens = response.data;},
                function(response) {$scope.wheel = false; $scope.tokens = null; $scope.error = formatError(response);}
            );
        };

        $scope.getTokens();
        $scope.removeToken = function(token) {
            $scope.wheel = true;
            var request = {
                method : 'DELETE',
                url : contextURL + '/admin/tokens/' + token,
                headers : {'Authorization' : 'Bearer ' + adminAccessToken}
            };
            $http(request).then(
                function(response) {$scope.wheel = false; $scope.getTokens();},
                function(response) {$scope.wheel = false; $scope.error = formatError(response);}
            );
        };
    });

    // ===========================
    // Page0 Controller
    // ===========================
    app.controller('page0Controller', function($scope,$http,$cookies) {
        $scope.error = false;
        $scope.data = null;
        adminAccessToken = $cookies.get('ADMIN_ACCESS_TOKEN');
        $scope.token = $cookies.get('ADMIN_ACCESS_TOKEN');

        var request = {
            method : 'GET',
            url : contextURL + '/oauth/check_token?token=' + adminAccessToken,
            headers : {'Authorization' : 'Basic Y2xpZW50MDpQQDU1dzByZDA='}
        };
        $http(request).then(
            function(response) {$scope.data = response.data; $scope.error = false;},
            function(response) {$scope.data = response.data; $scope.error = true;}
        );

    });

    // ===========================
    // Other Controller
    // ===========================
})();

// ===========================
// HTTP Management
// ===========================
function getQueryParameter(url,name) {
    var r = new RegExp('[\?&]' + name + '=([^&#]*)').exec(url);
    return r==null ? null : decodeURIComponent(r[1]);
}

function removeQuery(url) {
    var i=url.indexOf('?');
    return (i==-1) ? url : url.substring(0,i);
}

function formatError(response) {
    return 'ERROR ' + response.status + ' ' + response.data.error_description;
}

function getContextURL() {
    // HEROKU
    if (document.domain.endsWith('.herokuapp.com'))
        return 'https://demoa2.herokuapp.com';

    // Tomcat local, No Nginx
    if (document.URL.startsWith('http://localhost:8080/demoa2'))
        return 'http://localhost:8080/demoa2';

    // NGINX local + webapp runner
    if (document.URL.startsWith('https://localhost'))
        return 'https://localhost';

    // webapp runner, no Nginx
    return 'http://localhost:8080' ;
}






